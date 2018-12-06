package com.github.mrgaabriel.ayla.commands.config

import com.github.kevinsawicki.http.HttpRequest
import com.github.mrgaabriel.ayla.commands.AbstractCommand
import com.github.mrgaabriel.ayla.commands.CommandContext
import com.github.mrgaabriel.ayla.utils.AylaUtils
import com.github.mrgaabriel.ayla.utils.Constants
import com.github.mrgaabriel.ayla.utils.RedditUtils
import com.github.mrgaabriel.ayla.utils.Static
import com.github.mrgaabriel.ayla.utils.extensions.ayla
import com.github.salomonbrys.kotson.bool
import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.obj
import net.dv8tion.jda.core.Permission
import org.jetbrains.exposed.sql.transactions.transaction

class RedditCommand : AbstractCommand("reddit") {

    override fun getDescription(): String {
        return "Gerencia o módulo de sincronização de posts do Reddit"
    }

    override fun getMemberPermissions(): List<Permission> {
        return listOf(Permission.MANAGE_SERVER)
    }

    override fun getUsage(): String {
        return "<subreddit> <canal|off>"
    }

    override suspend fun run(context: CommandContext) {
        if (context.args.size != 2) {
            context.explain()
            return
        }

        val arg0 = context.args[0]

        val body = HttpRequest.get("https://www.reddit.com/subreddits/search.json?q=$arg0")
            .userAgent(Constants.USER_AGENT)
            .body()

        val payload = Static.JSON_PARSER.parse(body).obj
        val data = payload["data"].obj

        val dist = data["dist"].int

        if (dist == 0) {
            context.sendMessage("${context.event.author.asMention} Este sub-reddit não existe!")
            return
        }

        val about = HttpRequest.get("https://reddit.com/r/$arg0/about.json")
            .userAgent(Constants.USER_AGENT)
            .body()

        val aboutPayload = Static.JSON_PARSER.parse(about).obj
        val aboutData = aboutPayload["data"].obj

        val nsfw = aboutData["over18"].bool
        if (nsfw) {
            context.sendMessage("${context.event.author.asMention} O sub-reddit `/r/$arg0` está marcado como NSFW... e eu não quero ficar postando coisas desse tipo aqui. :rolling_eyes:")
            return
        }

        val sub = RedditUtils.getOrCreateSubReddit(arg0.toLowerCase())

        val arg1 = context.args[1]
        if (arg1 == "off") {
            val list = sub.channels.toMutableList()

            list.removeAll(context.event.guild.textChannels.map { it.id })
            transaction(ayla.database) {
                sub.channels = list.toTypedArray()
            }

            context.sendMessage("${context.event.author.asMention} Agora eu não enviarei mais notificações do sub-reddit `/r/${sub.name}`")
            return
        }

        val channel = AylaUtils.getTextChannel(arg1, context.event.guild)
        if (channel == null) {
            context.sendMessage("${context.event.author.asMention} Canal não encontrado!")
            return
        }

        val list = sub.channels.toMutableList()
        list.add(channel.id)

        transaction(ayla.database) {
            sub.channels = list.toTypedArray()
        }

        context.sendMessage("${context.event.author.asMention} Agora eu notificarei as novidades do sub-reddit `/r/${sub.name}` no canal ${channel.asMention}")
    }
}