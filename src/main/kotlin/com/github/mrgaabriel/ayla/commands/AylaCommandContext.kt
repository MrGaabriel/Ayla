package com.github.mrgaabriel.ayla.commands

import com.github.mrgaabriel.ayla.events.AylaMessageEvent
import com.github.mrgaabriel.ayla.utils.extensions.await
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageEmbed
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

class AylaCommandContext(val event: AylaMessageEvent, val command: AylaCommand, val args: MutableList<String>) {

    suspend fun sendMessage(content: Any) = event.channel.sendMessage(content.toString()).await()
    suspend fun sendMessage(embed: MessageEmbed, content: Any? = null): Message {
        val message = MessageBuilder()
            .setEmbed(embed)
            .setContent(content?.toString() ?: "")
            .build()

        return event.channel.sendMessage(message).await()
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
}