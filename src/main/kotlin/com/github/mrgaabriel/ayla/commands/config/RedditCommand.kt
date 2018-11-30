package com.github.mrgaabriel.ayla.commands.config

import com.github.mrgaabriel.ayla.commands.AbstractCommand
import com.github.mrgaabriel.ayla.commands.CommandContext
import com.github.mrgaabriel.ayla.utils.AylaUtils
import com.github.mrgaabriel.ayla.utils.RedditUtils
import com.github.mrgaabriel.ayla.utils.extensions.ayla
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