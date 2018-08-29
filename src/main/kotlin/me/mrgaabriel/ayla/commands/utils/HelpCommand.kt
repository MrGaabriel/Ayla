package me.mrgaabriel.ayla.commands.utils

import me.mrgaabriel.ayla.utils.*
import me.mrgaabriel.ayla.utils.commands.*
import me.mrgaabriel.ayla.utils.commands.annotations.*
import net.dv8tion.jda.core.*
import java.time.*

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
                val dummyContext = CommandContext(context.message, context.args, found)

                dummyContext.explain()
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