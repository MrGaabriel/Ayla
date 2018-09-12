package me.mrgaabriel.ayla.commands.config

import me.mrgaabriel.ayla.utils.commands.AbstractCommand
import me.mrgaabriel.ayla.utils.commands.CommandCategory
import me.mrgaabriel.ayla.utils.commands.CommandContext
import me.mrgaabriel.ayla.utils.commands.annotations.ArgumentType
import me.mrgaabriel.ayla.utils.commands.annotations.InjectArgument
import me.mrgaabriel.ayla.utils.commands.annotations.Subcommand
import me.mrgaabriel.ayla.utils.commands.annotations.SubcommandPermissions
import me.mrgaabriel.ayla.utils.config
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.TextChannel

class WelcomeCommand : AbstractCommand(
        "welcome",
        CommandCategory.CONFIG,
        "Configura o módulo de mensagens de boas vindas",
        "(canal/off)"
) {

    @Subcommand
    @SubcommandPermissions([Permission.MANAGE_SERVER])
    fun onExecute(context: CommandContext, @InjectArgument(ArgumentType.TEXT_CHANNEL) textChannel: TextChannel?) {
        val config = context.guild.config

        if (textChannel == null) {
            context.sendMessage(context.getAsMention(true) + "Canal não encontrado!")
            return
        }

        if (!textChannel.canTalk(context.guild.selfMember)) {
            context.sendMessage(context.getAsMention(true) + "Eu não consigo falar no canal ${textChannel.asMention}, por favor me dê permissão, obrigada!")
            return
        }

        config.welcomeEnabled = true
        config.welcomeChannel = textChannel.id
        context.guild.config = config

        context.sendMessage(context.getAsMention(true) + "Módulo de boas vindas ativado com sucesso no canal ${textChannel.asMention}!")
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