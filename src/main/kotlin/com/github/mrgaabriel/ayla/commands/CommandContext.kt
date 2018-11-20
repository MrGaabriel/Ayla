package com.github.mrgaabriel.ayla.commands

import com.github.mrgaabriel.ayla.dao.Guild
import com.github.mrgaabriel.ayla.events.AylaMessageEvent
import com.github.mrgaabriel.ayla.tables.Guilds
import com.github.mrgaabriel.ayla.utils.extensions.await
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageEmbed
import org.jetbrains.exposed.sql.transactions.transaction

class CommandContext(val event: AylaMessageEvent, val command: AbstractCommand, val args: MutableList<String>) {

    suspend fun sendMessage(content: Any) = event.channel.sendMessage(content.toString()).await()
    suspend fun sendMessage(embed: MessageEmbed, content: Any? = null): Message {
        val message = MessageBuilder()
            .setEmbed(embed)
            .setContent(content?.toString() ?: "")
            .build()

        return event.channel.sendMessage(message).await()
    }

    suspend fun explain(): Message {
        val builder = EmbedBuilder()

        return sendMessage(builder.build(), event.author.asMention)
    }
}