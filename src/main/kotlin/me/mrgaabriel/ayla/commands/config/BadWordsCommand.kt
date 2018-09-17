package me.mrgaabriel.ayla.commands.config

import me.mrgaabriel.ayla.utils.commands.AbstractCommand
import me.mrgaabriel.ayla.utils.commands.CommandCategory
import me.mrgaabriel.ayla.utils.commands.CommandContext
import me.mrgaabriel.ayla.utils.commands.annotations.ArgumentType
import me.mrgaabriel.ayla.utils.commands.annotations.InjectArgument // Testando
import me.mrgaabriel.ayla.utils.commands.annotations.Subcommand
import me.mrgaabriel.ayla.utils.commands.annotations.SubcommandPermissions
import me.mrgaabriel.ayla.utils.config
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.TextChannel

class BadWordsCommand : AbstractCommand(label = "badwords", category = CommandCategory.CONFIG, description = "Configura o módulo \"bad words\" no seu servidor") {

    @Subcommand
    fun execute(context: CommandContext) {
        context.explain()
    }

    @Subcommand(["on"])
    @SubcommandPermissions([Permission.MANAGE_SERVER])
    fun on(context: CommandContext) {
        val config = context.guild.config

        if (!config.badWordsEnabled) {
            config.badWordsEnabled = true
            context.guild.config = config

            context.sendMessage(context.getAsMention(true) + "Bad-words ativado com sucesso!")
            return
        }

        context.sendMessage(context.getAsMention(true) + "O módulo bad-words já está ativado!")
    }

    @Subcommand(["off"])
    @SubcommandPermissions([Permission.MANAGE_SERVER])
    fun off(context: CommandContext) {
        val config = context.guild.config

        if (config.badWordsEnabled) {
            config.badWordsEnabled = false
            context.guild.config = config

            context.sendMessage(context.getAsMention(true) + "Bad-words desativado com sucesso!")
            return
        }

        context.sendMessage(context.getAsMention(true) + "O módulo bad-words já está desativado!")
    }

    @Subcommand(["add"])
    @SubcommandPermissions([Permission.MANAGE_SERVER])
    fun add(context: CommandContext, word: String?) {
        if (word == null) {
            context.explain()
            return
        }

        val config = context.guild.config

        if (config.badWordsEnabled) {
            if (!config.badWords.contains(word.toLowerCase())) {
                config.badWords.add(word.toLowerCase())
                context.guild.config = config

                context.sendMessage(context.getAsMention(true) + "Agora a palavra `$word` está na lista de palavras bloqueadas!")
                return
            }

            context.sendMessage(context.getAsMention(true) + "A palavra `$word` já está na lista de palavras bloqueadas!")
            return
        }

        context.sendMessage(context.getAsMention(true) + "O módulo bad-words não está ativado!")
    }

    @Subcommand(["remove"])
    @SubcommandPermissions([Permission.MANAGE_SERVER])
    fun remove(context: CommandContext, word: String?) {
        if (word == null) {
            context.explain()
            return
        }

        val config = context.guild.config

        if (config.badWordsEnabled) {
            if (config.badWords.contains(word.toLowerCase())) {
                config.badWords.remove(word.toLowerCase())
                context.guild.config = config

                context.sendMessage(context.getAsMention(true) + "A palavra `$word` foi removida da lista de palavras bloqueadas!")
                return
            }

            context.sendMessage(context.getAsMention(true) + "A palavra `$word` não está na lista de palavras bloqueadas!")
            return
        }

        context.sendMessage(context.getAsMention(true) + "O módulo bad-words não está ativado!")
    }

    @Subcommand(["ignore"])
    @SubcommandPermissions([Permission.MANAGE_SERVER])
    fun ignore(context: CommandContext, @InjectArgument(ArgumentType.TEXT_CHANNEL) channel: TextChannel?) {
        if (channel == null) {
            context.explain()
            return
        }

        val config = context.guild.config

        if (config.badWordsEnabled) {
            if (!config.badWordsIgnoredChannels.contains(channel.id)) {
                config.badWordsIgnoredChannels.add(channel.id)
                context.guild.config = config

                context.sendMessage(context.getAsMention(true) + "Agora o cnaal ${channel.asMention} será ignorado pelo módulo bad-words!")
            } else {
                config.badWordsIgnoredChannels.remove(channel.id)
                context.guild.config = config

                context.sendMessage(context.getAsMention(true) + "Agora o canal ${channel.asMention} não será mais ignorado pelo módulo bad-words!")
            }

            return
        }

        context.sendMessage(context.getAsMention(true) + "O módulo bad-words não está ativado!")
    }
}
