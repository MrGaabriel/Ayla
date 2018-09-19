package me.mrgaabriel.ayla.threads

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.bool
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.nullObj
import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import com.mongodb.client.model.Filters
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import me.mrgaabriel.ayla.data.AylaGuildConfig
import me.mrgaabriel.ayla.utils.Constants
import me.mrgaabriel.ayla.utils.ayla
import me.mrgaabriel.ayla.utils.config
import me.mrgaabriel.ayla.utils.saveConfig
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.exceptions.ErrorResponseException
import org.apache.commons.lang3.exception.ExceptionUtils
import org.bson.Document
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
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

        val redditSubs = mutableMapOf<AylaGuildConfig.SubRedditWrapper, AylaGuildConfig>()

        guilds.forEach {
            val subs = it.redditSubs

            for (sub in subs) {
                redditSubs.put(sub, it)
            }
        }

        async {
            logger.info("Verificando sub-reddits! Atualmente eu estou verificando ${redditSubs.keys.size} sub-reddits!")

            var posts = 0
            redditSubs.forEach { sub, guild ->
                val request = HttpRequest.get("https://reddit.com/r/${sub.subReddit}/new/.json")
                        .userAgent(Constants.USER_AGENT)

                if (!request.ok()) {
                    throw RuntimeException("Request for \"https://reddit.com/r/${sub.subReddit}/new/.json\" is not OK!")
                }

                val payload = JsonParser().parse(request.body())

                val data = payload["data"].obj
                val children = data["children"].array

                val post = children[0]?.get("data")?.obj

                if (post != null) {
                    val lastCachedPostCreation = guild.redditSubsLastPost[sub.subReddit] ?: 0 // Elvis operator
                    val creationTime = post["created_utc"].long

                    if (creationTime > lastCachedPostCreation) {
                        val guildHandle = ayla.getGuildById(guild.id)
                            ?: throw RuntimeException("Guild ID is null") // what the fuck
                        val channel = ayla.getTextChannelById(sub.channelId) ?: return@forEach // :rolling_eyes:

                        if (post["over_18"].bool) { // Não, não e não
                            guild.redditSubsLastPost[sub.subReddit] = creationTime
                            guildHandle.saveConfig(guild)

                            return@forEach
                        }

                        val builder = EmbedBuilder()

                        builder.setAuthor((post["title"].nullString
                            ?: "Sem título") + " - r/${sub.subReddit}", "https://reddit.com" + post["permalink"].nullString, "https://pbs.twimg.com/profile_images/868147475852312577/fjCSPU-a_400x400.jpg") // TODO: Parar de usar os assets do Twitter
                        builder.setColor(Constants.REDDIT_ORANGE_RED)

                        val content = post["selftext"].nullString
                        if (content != null) {
                            val contentSubstringed = if (content.length > 2000) {
                                content.substring(0, 2030) + "... [Leia mais aqui](https;//reddit.com${post["permalink"].string})"
                            } else {
                                content
                            }

                            builder.setDescription(contentSubstringed)
                        }

                        val images = post["preview"]?.obj?.get("images")?.array?.iterator()?.next()

                        val url = post["url"].nullString

                        if (images != null) {
                            val imageContent = images.obj

                            val imageUrl = imageContent["source"].obj["url"].string

                            builder.setImage(imageUrl)
                        } else if (url != null) {
                            builder.setImage(url)
                        }

                        builder.setFooter("u/" + post["author"].string, null)
                        builder.setTimestamp(Instant.ofEpochMilli(creationTime).atZone(ZoneId.systemDefault()))

                        if (!channel.canTalk()) { // :rolling_eyes:
                            return@forEach
                        }

                        channel.sendMessage(builder.build()).queue()

                        guild.redditSubsLastPost[sub.subReddit] = creationTime
                        guildHandle.saveConfig(guild)

                        posts++
                    }
                }
            }

            logger.info("Sucesso! Encontrei e enviei $posts posts!")
        }
    }
}