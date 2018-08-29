package me.mrgaabriel.ayla.commands.config

import me.mrgaabriel.ayla.utils.*
import me.mrgaabriel.ayla.utils.commands.*
import me.mrgaabriel.ayla.utils.commands.annotations.*
import net.dv8tion.jda.core.*

class WelcomeCommand : AbstractCommand(
        "welcome",
        CommandCategory.CONFIG,
        "Configura o módulo de mensagens de boas vindas",
        "(canal/off)"
) {

    @Subcommand
    @SubcommandPermissions([Permission.MANAGE_SERVER])
    fun onExecute(context: CommandContext, channel: String) {
        val config = context.guild.config

        val channelId = channel.replace("<", "")
                .replace("#", "")
                .replace(">", "")

        val textChannel = context.guild.getTextChannelById(channelId)
        if (textChannel == null) {
            context.sendMessage(context.getAsMention(true) + "Canal não encontrado!")
            return
        }

        if (!textChannel.canTalk(context.guild.selfMember)) {
            context.sendMessage(context.getAsMention(true) + "Eu não consigo falar no canal <#$channelId>, por favor me dê permissão, obrigada!")
            return
        }

        config.welcomeEnabled = true
        config.welcomeChannel = textChannel.id
        context.guild.config = config

        context.sendMessage(context.getAsMention(true) + "Módulo de boas vindas ativado com sucesso no canal <#$channelId>!")
    }

    @Subcommand(["off"])
    @SubcommandPermissions([Permission.MANAGE_SERVER])
    fun off(context: CommandContext) {
        val config = context.guild.config

        if (!config.welcomeEnabled) {
            context.sendMessage(context.getAsMention(true) + "O módulo de boas vindas não está ativado!")
            return
        }

        config.welcomeEnabled = false
        context.guild.config = config
        context.sendMessage(context.getAsMention(true) + "Módulo de boas vindas desativado com sucesso!")
    }
}