package me.mrgaabriel.ayla.commands.config

import me.mrgaabriel.ayla.commands.*
import me.mrgaabriel.ayla.utils.*
import net.dv8tion.jda.core.*

class WelcomeCommand : AbstractCommand() {

    init {
        this.label = "welcome"
        this.description = "Configura o módulo de mensagens de boas vindas"
        this.usage = "canal/off"

        this.memberPermissions = mutableListOf(Permission.MANAGE_SERVER)
        this.category = CommandCategory.CONFIG
    }

    override fun execute(context: CommandContext) {
        if (context.args.isEmpty()) {
            context.explain()
            return
        }

        val config = context.guild.config

        if (context.args[0].toLowerCase() == "off") {
            if (!config.welcomeEnabled) {
                context.sendMessage(context.getAsMention(true) + "O módulo de boas vindas não está ativado!")
                return
            }

            config.welcomeEnabled = false
            context.guild.config = config
            context.sendMessage(context.getAsMention(true) + "Módulo de boas vindas desativado com sucesso!")
            return
        }

        if (!config.welcomeEnabled) {
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

            config.welcomeEnabled = true
            config.welcomeChannel = channel.id
            context.guild.config = config

            context.sendMessage(context.getAsMention(true) + "Módulo de boas vindas ativado com sucesso no canal <#$channelId>!")
        } else {
            context.sendMessage(context.getAsMention(true) + "Módulo de boas vindas já está ativado, use `${context.guild.config.prefix}welcome off` para desativar!")
        }
    }
}