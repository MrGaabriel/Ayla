package me.mrgaabriel.ayla.commands.config

import me.mrgaabriel.ayla.commands.*
import me.mrgaabriel.ayla.utils.*
import net.dv8tion.jda.core.*

class EventLogCommand : AbstractCommand() {

    init {
        this.label = "eventlog"
        this.description = "Controla o módulo event-log no seu servidor"
        this.category = CommandCategory.CONFIG

        this.memberPermissions = mutableListOf(Permission.MANAGE_SERVER)
    }

    override fun execute(context: CommandContext) {
        if (context.args.isEmpty()) {
            context.explain()
            return
        }

        val config = context.guild.config

        if (context.args[0].toLowerCase() == "off") {
            if (!config.eventLogEnabled) {
                context.sendMessage(context.getAsMention(true) + "O event-log não está ativado!")
                return
            }

            config.eventLogEnabled = false
            context.guild.config = config
            context.sendMessage(context.getAsMention(true) + "Event-log desativado com sucesso!")
            return
        }

        if (!config.eventLogEnabled) {
            val channelId = context.args[0].replace("<", "")
                    .replace("#", "")
                    .replace(">", "")

            val channel = context.guild.getTextChannelById(channelId)
            if (channel == null) {
                context.sendMessage(context.getAsMention(true) + "Canal não encontrado!")
                return
            }

            if (!channel.canTalk(context.guild.selfMember)) {
                context.sendMessage(context.getAsMention(true) + "Eu não consigo falar no canal <#$channelId>, por favor me dê permissão, obrigada!")
                return
            }

            config.eventLogEnabled = true
            config.eventLogChannel = channel.id
            context.guild.config = config

            context.sendMessage(context.getAsMention(true) + "Event-log ativado com sucesso no canal <#$channelId>!")
        } else {
            context.sendMessage(context.getAsMention(true) + "O event-log já está ativado, use `${context.guild.config.prefix}eventlog off` para desativar!")
        }
    }
}