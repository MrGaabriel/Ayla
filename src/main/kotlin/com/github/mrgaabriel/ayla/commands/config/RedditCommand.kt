package com.github.mrgaabriel.ayla.commands.config

import com.github.kevinsawicki.http.HttpRequest
import com.github.mrgaabriel.ayla.commands.*
import com.github.mrgaabriel.ayla.utils.AylaUtils
import com.github.mrgaabriel.ayla.utils.Constants
import com.github.mrgaabriel.ayla.utils.RedditUtils
import com.github.mrgaabriel.ayla.utils.Static
import com.github.mrgaabriel.ayla.utils.extensions.ayla
import com.github.salomonbrys.kotson.bool
import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.obj
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.TextChannel
import net.perfectdreams.commands.annotation.Subcommand
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.contracts.ExperimentalContracts

class RedditCommand : AylaCommand("reddit") {

    override val category: CommandCategory
        get() = CommandCategory.CONFIG

    override val description: String
        get() = "Gerencia o módulo de sincronização de posts do Reddit"

    override val discordPermissions: List<Permission>
        get() = listOf(Permission.MANAGE_SERVER)

    override val usage: String
        get() = "(add subreddit channel|remove subreddit channel)"

    @Subcommand
    suspend fun root(context: AylaCommandContext) {
        context.explain()
    }

    @Subcommand(["add"])
    @ExperimentalContracts
    suspend fun add(context: AylaCommandContext, subReddit: String, channel: TextChannel?) {
        notNull(channel, "Canal não encontrado!")

        val body = HttpRequest.get("https://www.reddit.com/subreddits/search.json?q=$subReddit")
            .userAgent(Constants.USER_AGENT)
            .body()

        val payload = Static.JSON_PARSER.parse(body).obj
        val data = payload["data"].obj

        val dist = data["dist"].int

        if (dist == 0) {
            context.reply("Este sub-reddit não existe!")
            return
        }

        val about = HttpRequest.get("https://reddit.com/r/$subReddit/about.json")
            .userAgent(Constants.USER_AGENT)
            .body()

        val aboutPayload = Static.JSON_PARSER.parse(about).obj
        val aboutData = aboutPayload["data"].obj

        val nsfw = aboutData["over18"].bool
        if (nsfw) {
            context.reply("O sub-reddit `/r/$subReddit` está marcado como NSFW... e eu não quero ficar postando coisas desse tipo aqui. :rolling_eyes:")
            return
        }

        val sub = RedditUtils.getOrCreateSubReddit(subReddit)

        val channels = sub.channels.toMutableList()
        channels.add(channel.id)

        transaction(ayla.database) {
            sub.channels = channels.toTypedArray()
        }

        context.reply("Agora eu mandarei notificações de posts do `/r/${sub.name}` no canal ${channel.asMention}!")
    }

    @Subcommand(["remove"])
    @ExperimentalContracts
    suspend fun remove(context: AylaCommandContext, subReddit: String, channel: TextChannel?) {
        notNull(channel, "Canal não encontrado!")

        val body = HttpRequest.get("https://www.reddit.com/subreddits/search.json?q=$subReddit")
            .userAgent(Constants.USER_AGENT)
            .body()

        val payload = Static.JSON_PARSER.parse(body).obj
        val data = payload["data"].obj

        val dist = data["dist"].int

        if (dist == 0) {
            context.reply("Este sub-reddit não existe!")
            return
        }

        val about = HttpRequest.get("https://reddit.com/r/$subReddit/about.json")
            .userAgent(Constants.USER_AGENT)
            .body()

        val aboutPayload = Static.JSON_PARSER.parse(about).obj
        val aboutData = aboutPayload["data"].obj

        val nsfw = aboutData["over18"].bool
        if (nsfw) {
            context.reply("O sub-reddit `/r/$subReddit` está marcado como NSFW... e eu não quero ficar postando coisas desse tipo aqui. :rolling_eyes:")
            return
        }

        val sub = RedditUtils.getOrCreateSubReddit(subReddit)

        val channels = sub.channels.toMutableList()
        channels.remove(channel.id)

        transaction(ayla.database) {
            sub.channels = channels.toTypedArray()
        }

        context.reply("Agora eu pararei de mandar notificações de posts do `/r/${sub.name}` no canal ${channel.asMention}!")
    }
}