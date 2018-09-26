package me.mrgaabriel.ayla.commands.utils

import me.mrgaabriel.ayla.utils.Constants
import me.mrgaabriel.ayla.utils.ayla
import me.mrgaabriel.ayla.utils.commands.AbstractCommand
import me.mrgaabriel.ayla.utils.commands.CommandCategory
import me.mrgaabriel.ayla.utils.commands.CommandContext
import me.mrgaabriel.ayla.utils.commands.annotations.Subcommand
import me.mrgaabriel.ayla.utils.config
import me.mrgaabriel.ayla.utils.tag
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.MessageBuilder
import java.time.OffsetDateTime

class HelpCommand : AbstractCommand(
        label = "help",
        category = CommandCategory.UTILS,
        description = "Consiga a descrição dos comandos disponíveis",
        aliases = listOf("ajuda", "comandos", "commands")
) {

    @Subcommand
    fun onExecute(context: CommandContext, cmd: String?) {
        if (cmd != null) {
            val found = ayla.commandMap
                    .filter { it.category != CommandCategory.DEVELOPER && it !is HelpCommand }
                    .firstOrNull { it.label == cmd.toLowerCase() || it.aliases.any { it == cmd.toLowerCase() } }

            if (found != null) {
                // TODO: Remover esta gambiarra
                context.sendMessage(context.guild.config.prefix + found.label + " (ignore)", { message ->
                    message.delete().queue()

                    val dummyContext = CommandContext(message, context.args, found)
                    dummyContext.explain()
                })
            } else {
                context.sendMessage(context.getAsMention(true) + "Comando não encontrado!")
            }

            return
        }

        val commands = ayla.commandMap.filter { it.category != CommandCategory.DEVELOPER && it !is HelpCommand }

        val builder = EmbedBuilder()

        builder.setFooter("Atualmente há ${commands.size} comandos disponíveis!", null)
        builder.setTimestamp(OffsetDateTime.now())

        builder.setColor(Constants.REDDIT_ORANGE_RED)

        builder.setAuthor(context.user.tag, null, context.user.effectiveAvatarUrl)
        builder.setDescription("**Lista de comandos disponíveis:\n\n${commands.joinToString(", ", transform = { "`${it.label}`" })}**\nCaso queira saber mais sobre um comando digite `${context.guild.config.prefix}help NomeDoComando`")

        context.sendMessage(builder.build(), context.getAsMention())
    }
}