package com.github.mrgaabriel.ayla.commands

import com.github.mrgaabriel.ayla.dao.GuildConfig
import com.github.mrgaabriel.ayla.events.AylaMessageEvent
import com.github.mrgaabriel.ayla.tables.GuildConfigs
import com.github.mrgaabriel.ayla.utils.extensions.await
import com.github.mrgaabriel.ayla.utils.extensions.ayla
import com.github.mrgaabriel.ayla.utils.extensions.tag
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.transactionScope
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.OffsetDateTime
import javax.imageio.ImageIO

class AylaCommandContext(val event: AylaMessageEvent, val command: AylaCommand, val args: MutableList<String>) {

    val message: Message
        get() = event.message

    val author: User
        get() = event.author

    val channel: MessageChannel
        get() = event.channel

    val textChannel: TextChannel
        get() = event.textChannel

    val member: Member
        get() = event.member

    val guild: Guild
        get() = event.guild

    suspend fun sendMessage(content: Any) = event.channel.sendMessage(content.toString()).await()
    suspend fun sendMessage(embed: MessageEmbed, content: Any? = null): Message {
        val message = MessageBuilder()
            .setEmbed(embed)
            .setContent(content?.toString() ?: "")
            .build()

        return event.channel.sendMessage(message).await()
    }

    suspend fun reply(embed: MessageEmbed, content: Any? = null): Message {
        val builder = MessageBuilder()

        builder.setEmbed(embed)
        builder.setContent("${event.author.asMention} ${content ?: ""}")

        return sendMessage(builder.build())
    }

    suspend fun reply(content: Any): Message {
        return sendMessage("${event.author.asMention} $content")
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

        builder.setTitle(":thinking: `${config.prefix}${command.labels.first()}`")
        builder.appendDescription(command.description)

        builder.setColor(Color(114, 137, 218))

        builder.setFooter(command.category.fancyName, null)
        builder.setTimestamp(OffsetDateTime.now())

        return sendMessage(builder.build(), event.author.asMention)
    }
}