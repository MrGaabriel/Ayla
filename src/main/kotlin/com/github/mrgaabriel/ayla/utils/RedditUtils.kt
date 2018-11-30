package com.github.mrgaabriel.ayla.utils

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.kevinsawicki.http.HttpRequest
import com.github.mrgaabriel.ayla.dao.SubReddit
import com.github.mrgaabriel.ayla.tables.SubReddits
import com.github.mrgaabriel.ayla.utils.extensions.ayla
import com.github.salomonbrys.kotson.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.core.EmbedBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.OffsetDateTime

object RedditUtils {

    val logger by logger()

    // val postCreationCache = Caffeine.newBuilder().build<SubReddit, Long>().asMap()

    fun spawnTasks() {
        val subs = transaction(ayla.database) {
            SubReddit.all().toMutableList()
        }

        for (sub in subs) {
            spawnTask(sub)
        }
    }

    fun spawnTask(sub: SubReddit) {
        GlobalScope.launch(ayla.redditTasksDispatcher) {
            logger.info("Inicializando task para verificar os posts do sub-reddit /r/${sub.name}")

            while (true) {
                val sub = transaction(ayla.database) {
                    SubReddit.find { SubReddits.name eq sub.name }.first()
                }

                try {
                    val request = HttpRequest.get("https://reddit.com/r/${sub.name}/new/.json")
                        .userAgent(Constants.USER_AGENT)

                    if (!request.ok()) {
                        logger.error("Erro ao fazer request para \"https://reddit.com/r/${sub.name}/new/.json\" - Response code: ${request.code()}")
                        continue
                    }

                    val payload = Static.JSON_PARSER.parse(request.body()).obj
                    val data = payload["data"].obj
                    val children = data["children"].array

                    val comment = children.first().obj
                    val commentData = comment["data"].obj

                    val creation = commentData["created_utc"].long
                    val lastCreation = sub.lastCommentCreation

                    if (lastCreation == creation)
                        continue

                    val nsfw = commentData["over_18"].bool

                    if (!nsfw) {
                        val title = "${commentData["title"].string} (/r/${sub.name})"
                        val author = "u/${commentData["author"].string}"

                        val link = "https://reddit.com${commentData["permalink"].string}"
                        val description = commentData["selftext"].string

                        var imageUrl: String? = null

                        val url = commentData["url"].nullString
                        if (url != null && imageUrl == null) {
                            imageUrl = url
                        }

                        val builder = EmbedBuilder()

                        builder.setAuthor(title, link, "https://cdn.discordapp.com/emojis/517750327798267905.png?v=1")
                        builder.setColor(Constants.REDDIT_ORANGE_RED)

                        builder.setDescription(if (description.length > 2000) "${description.substring(0..1970)}... [Leia mais aqui]($link)" else description)

                        builder.setFooter(author, null)
                        builder.setTimestamp(OffsetDateTime.now())

                        if (imageUrl != null) {
                            builder.setImage(imageUrl)
                        }

                        for (channelId in sub.channels) {
                            val channel = ayla.shardManager.getTextChannelById(channelId)

                            channel.sendMessage(builder.build()).queue({}, {})
                        }

                        transaction(ayla.database) {
                            sub.lastCommentCreation = creation
                        }
                    }
                } catch (e: Exception) {
                    logger.error("Erro ao sincronizar posts do sub-reddit ${sub.name}!", e)
                }

                delay(60 * 1000)
            }
        }
    }

    fun getOrCreateSubReddit(name: String): SubReddit {
        val found = transaction(ayla.database) {
            SubReddit.find { SubReddits.name eq name }.firstOrNull()
        }

        return if (found != null) {
            found
        } else {
            val sub = transaction(ayla.database) {
                SubReddit.new {
                    this.name = name
                    this.channels = arrayOf()
                }
            }

            spawnTask(sub)
            return sub
        }
    }
}