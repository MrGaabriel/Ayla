package com.github.mrgaabriel.ayla.commands

import com.github.mrgaabriel.ayla.dao.GuildConfig
import com.github.mrgaabriel.ayla.events.AylaMessageEvent
import com.github.mrgaabriel.ayla.tables.GuildConfigs
import com.github.mrgaabriel.ayla.utils.extensions.await
import com.github.mrgaabriel.ayla.utils.extensions.ayla
import com.github.mrgaabriel.ayla.utils.extensions.tag
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageEmbed
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.OffsetDateTime
import javax.imageio.ImageIO

class CommandContext(val event: AylaMessageEvent, val command: AbstractCommand, val args: MutableList<String>) {

    suspend fun sendMessage(content: Any) = event.channel.sendMessage(content.toString()).await()
    suspend fun sendMessage(embed: MessageEmbed, content: Any? = null): Message {
        val message = MessageBuilder()
            .setEmbed(embed)
            .setContent(content?.toString() ?: "")
            .build()

        return event.channel.sendMessage(message).await()
    }

    suspend fun sendFile(image: BufferedImage, name: String, message: String): Message {
        val outputStream = ByteArrayOutputStream()
        outputStream.use {
            ImageIO.write(image, "png", it)
        }
        val inputStream = ByteArrayInputStream(outputStream.toByteArray())

        val message = MessageBuilder()
            .setContent(message)
            .build()

        return event.channel.sendFile(inputStream, name, message).await()
    }

    suspend fun explain(): Message {
        val config = transaction(ayla.database) {
            GuildConfig.find { GuildConfigs.id eq event.guild.id }.first()
        }

        val builder = EmbedBuilder()

        builder.setAuthor(event.author.tag, null, event.author.effectiveAvatarUrl)
        builder.setTitle("\uD83E\uDD14 `${config.prefix}${command.label}`")

        builder.setDescription(command.getDescription())

        if (command.getUsage().isNotEmpty()) {
            builder.addField(
                ":question: Como usar?",
                "${config.prefix}${command.label} ${command.getUsage()}",
                true
            )
        }

        if (command.aliases.isNotEmpty()) {
            builder.addField(
                ":heavy_plus_sign: Alternativas",
                command.aliases.joinToString(", ", transform = { "`${config.prefix}$it`" }),
                true
            )
        }

        if (command.getBotPermissions().isNotEmpty()) {
            builder.addField(
                ":robot: Permissões que eu preciso",
                command.getBotPermissions().joinToString(", ", transform = { "`${it.name}`" }),
                true
            )
        }

        if (command.getMemberPermissions().isNotEmpty()) {
            builder.addField(
                ":information_desk_person: Permissões que você precisa",
                command.getMemberPermissions().joinToString(", ", transform = { "`${it.name}`" }),
                true
            )
        }

        builder.setTimestamp(OffsetDateTime.now())

        builder.setColor(Color.RED)

        return sendMessage(builder.build(), event.author.asMention)
    }
}