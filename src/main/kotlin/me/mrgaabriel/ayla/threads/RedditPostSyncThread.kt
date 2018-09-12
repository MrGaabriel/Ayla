package me.mrgaabriel.ayla.threads

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.bool
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import kotlinx.coroutines.experimental.async
import me.mrgaabriel.ayla.utils.Constants
import me.mrgaabriel.ayla.utils.ayla
import me.mrgaabriel.ayla.utils.config
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.exceptions.ErrorResponseException
import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.LoggerFactory
import java.time.OffsetDateTime
import kotlin.collections.filter
import kotlin.collections.forEach
import kotlin.collections.isNotEmpty
import kotlin.collections.set

class RedditPostSyncThread : Thread("Reddit Posts Sync") {

    val logger = LoggerFactory.getLogger(RedditPostSyncThread::class.java)

    override fun run() {
        while (true) {
            try {
                syncRedditPosts()

                Thread.sleep(60 * 1000)
            } catch (e: Exception) {
                logger.error("Erro ao sincronizar posts do reddit!")
                logger.error(ExceptionUtils.getStackTrace(e))

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

                            if (comment["over_18"].bool) { // Se o comentário estiver marcado como NSFW... iremos simplesmente ignorá-lo!
                                it.lastRedditPostCreation[subReddit] = comment["created"].long.toString()
                                return@forEach
                            }

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

                            channel.sendMessage(builder.build()).queue({}, { e ->
                                if (e is ErrorResponseException) {
                                    if (e.errorCode == 400) {
                                        return@queue
                                    }
                                }
                            })

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