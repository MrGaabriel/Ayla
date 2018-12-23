package com.github.mrgaabriel.ayla.commands.utils

import com.github.mrgaabriel.ayla.commands.AylaCommand
import com.github.mrgaabriel.ayla.commands.AylaCommandContext
import com.github.mrgaabriel.ayla.commands.CommandCategory
import com.github.mrgaabriel.ayla.dao.GuildConfig
import com.github.mrgaabriel.ayla.tables.GuildConfigs
import com.github.mrgaabriel.ayla.utils.extensions.await
import com.github.mrgaabriel.ayla.utils.extensions.ayla
import com.github.mrgaabriel.ayla.utils.extensions.tag
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.exceptions.ErrorResponseException
import net.perfectdreams.commands.annotation.Subcommand
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color
import java.time.OffsetDateTime

class HelpCommand : AylaCommand("help", "ajuda", "comandos") {

    override val description: String
        get() = "Veja os comandos disponíveis da Ayla"

    override val category: CommandCategory
        get() = CommandCategory.UTILS

    @Subcommand
    suspend fun root(context: AylaCommandContext) {
        val config = transaction(ayla.database) {
            GuildConfig.find { GuildConfigs.id eq context.event.guild.id }.first()
        }

        val commands = ayla.commandManager.getRegisteredCommands()

        val builder = EmbedBuilder()

        builder.setAuthor(context.event.author.tag, null, context.event.author.effectiveAvatarUrl)
        builder.setTitle("\uD83D\uDCDA Lista de comandos da Ayla")

        builder.setColor(Color(114, 137, 218))

        builder.setFooter("${commands.size} comandos disponíveis", null)
        builder.setTimestamp(OffsetDateTime.now())

        for (category in CommandCategory.values().filter { it.showOnHelp && (commands.filter { cmd -> cmd.category == it }.isNotEmpty()) }) {
            val categoryCommands = commands.filter { it.category == category }

            builder.appendDescription(
                "**${category.fancyName}** - *${category.description}*\n" + categoryCommands.joinToString(
                    "\n",
                    transform = { "**${config.prefix}${it.labels.first()} »** ${it.description}" }) + "\n\n"
            )
        }

        val commandsEmbed = builder.build()

        builder.clear()

        builder.setTitle("\uD83D\uDE4B Me adicione no seu servidor!")
        builder.setDescription("Clique [aqui](https://ayla.space/invite) para me adicionar no seu servidor para desfrutar do que eu posso fazer!")

        builder.setColor(Color(114, 137, 218))

        try {
            val channel = context.event.author.openPrivateChannel().await()

            channel.sendMessage(builder.build()).queue()
            channel.sendMessage(commandsEmbed).queue()

            context.reply("Confira as suas mensagens privadas!")
        } catch (e: ErrorResponseException) {
            if (e.errorCode == 50002) {
                context.reply("As suas mensagens privadas estão desativadas! Considere ativá-las.")
            }
        }
    }

}