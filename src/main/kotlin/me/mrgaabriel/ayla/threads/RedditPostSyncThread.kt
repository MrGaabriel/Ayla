package me.mrgaabriel.ayla.threads

import com.github.kevinsawicki.http.*
import com.github.salomonbrys.kotson.*
import com.google.common.flogger.*
import com.google.gson.*
import kotlinx.coroutines.experimental.*
import me.mrgaabriel.ayla.utils.*
import net.dv8tion.jda.core.*
import org.apache.commons.lang3.exception.*
import java.time.*

class RedditPostSyncThread : Thread("Reddit Posts Sync") {

    val logger = FluentLogger.forEnclosingClass()

    override fun run() {
        while (true) {
            try {
                syncRedditPosts()

                Thread.sleep(60 * 1000)
            } catch (e: Exception) {
                logger.atSevere().log("Erro ao sincronizar posts do reddit!")
                logger.atSevere().log(ExceptionUtils.getStackTrace(e))

                Thread.sleep(60 * 1000)
            }
        }
    }

    fun syncRedditPosts() {
        val guilds = ayla.guildsColl.find().filter { it.redditSubs.isNotEmpty() }

        async {
            guilds.forEach {
                it.redditSubs.forEach { subReddit, channelId ->
                    val request = HttpRequest.get("https://reddit.com/r/$subReddit/new/.json")
                            .userAgent(Constants.USER_AGENT)

                    if (request.code() != 200) {
                        throw RuntimeException("Request code for https://reddit.com/r/$subReddit/new/.json is not 200!")
                    }

                    val payload = JsonParser().parse(request.body())
                    val data = payload["data"].obj

                    val children = data["children"].array

                    for (child in children) {
                        val comment = child["data"]

                        if (comment["created"].long > (it.lastRedditPostCreation[subReddit]
                                    ?: "0").toLong()) { // É um post novo!
                            val guildHandle = ayla.getGuildById(it.id) ?: return@forEach

                            val channel = guildHandle.getTextChannelById(channelId) ?: return@forEach

                            val builder = EmbedBuilder()

                            builder.setAuthor((comment["title"].nullString
                                ?: "Sem título") + " - r/$subReddit", "https://reddit.com" + comment["permalink"].nullString, "https://pbs.twimg.com/profile_images/868147475852312577/fjCSPU-a_400x400.jpg")
                            builder.setColor(Constants.REDDIT_ORANGE_RED)

                            val content = comment["selftext"].nullString
                            if (content != null) {
                                val contentSubstringed = if (content.length > 2000) {
                                    content.substring(0, 2030) + "... [Leia mais aqui](https;//reddit.com${comment["permalink"].string})"
                                } else {
                                    content
                                }

                                builder.setDescription(contentSubstringed)
                            }

                            try {
                                val images = comment["preview"].obj["images"].array.iterator().next()
                                val imageContent = images.obj

                                val url = comment["url"].nullString

                                if (images != null) {
                                    val imageUrl = imageContent["source"].obj["url"].string

                                    builder.setImage(imageUrl)
                                } else if (url != null) {
                                    builder.setImage(url)
                                }
                            } catch (e: NoSuchElementException) {
                                // Não tem imagens
                            }

                            builder.setFooter("u/" + comment["author"].string, null)
                            builder.setTimestamp(OffsetDateTime.now())

                            if (!channel.canTalk())
                                continue

                            channel.sendMessage(builder.build()).complete()

                            it.lastRedditPostCreation[subReddit] = comment["created"].long.toString()
                            guildHandle.config = it
                        }

                        break
                    }
                }
            }
        }
    }
}