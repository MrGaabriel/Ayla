package me.mrgaabriel.ayla.commands.config

import me.mrgaabriel.ayla.utils.*
import me.mrgaabriel.ayla.utils.commands.*
import me.mrgaabriel.ayla.utils.commands.annotations.*
import net.dv8tion.jda.core.*

class EventLogCommand : AbstractCommand(
        "eventlog",
        CommandCategory.CONFIG,
        "Configura o módulo event-log no seu servidor") {


    @Subcommand
    @SubcommandPermissions([Permission.MANAGE_SERVER])
    fun onExecute(context: CommandContext, channel: String) {
        if (context.args.isEmpty()) {
            context.explain()
            return
        }

        val config = context.guild.config

        val channelId = channel.replace("<", "")
                .replace("#", "")
                .replace(">", "")

        if (config.eventLogEnabled) {
            context.sendMessage(context.getAsMention(true) + "O event-log já está ativado!")
            return
        }

        val textChannel = context.guild.getTextChannelById(channelId)
        if (textChannel == null) {
            context.sendMessage(context.getAsMention(true) + "Canal não encontrado!")
            return
        }

        if (!textChannel.canTalk(context.guild.selfMember)) {
            context.sendMessage(context.getAsMention(true) + "Eu não consigo falar no canal <#$channelId>, por favor me dê permissão, obrigada!")
            return
        }

        config.eventLogEnabled = true
        config.eventLogChannel = textChannel.id
        context.guild.config = config

        context.sendMessage(context.getAsMention(true) + "Event-log ativado com sucesso no canal <#$channelId>!")
    }

    @Subcommand(["off"])
    @SubcommandPermissions([Permission.MANAGE_SERVER])
    fun onOff(context: CommandContext) {
        val config = context.guild.config

        if (!config.eventLogEnabled) {
            context.sendMessage(context.getAsMention(true) + "O event-log não está ativado!")
            return
        }

        config.eventLogEnabled = false
        context.guild.config = config
        context.sendMessage(context.getAsMention(true) + "Event-log desabilitado com sucesso!")
    }
}