package me.mrgaabriel.ayla.utils

import com.mongodb.client.model.Filters
import com.mongodb.client.model.UpdateOptions
import me.mrgaabriel.ayla.AylaLauncher
import me.mrgaabriel.ayla.data.AylaGuildConfig
import me.mrgaabriel.ayla.data.AylaUser
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveEvent
import net.dv8tion.jda.core.utils.MiscUtil
import java.net.MalformedURLException
import java.net.URL
import java.time.Month
import java.time.OffsetDateTime

val User.tag: String get() = "${this.name}#${this.discriminator}"

var User.aylaUser: AylaUser get() {
    val found = ayla.usersColl.find(
            Filters.eq("_id", this.id)
    ).firstOrNull()

    return if (found != null) {
        found
    } else {
        ayla.usersColl.insertOne(
                AylaUser(this.id)
        )

        AylaUser(this.id)
    }
} set(user) {

    val options = UpdateOptions().upsert(true)
    ayla.usersColl.replaceOne(
            Filters.eq("_id", this.id),
            user,
            options
    )
}

var Guild.config: AylaGuildConfig get() {
    val found = ayla.guildsColl.find(
            Filters.eq("_id", this.id)
    ).firstOrNull()

    return if (found != null) {
        found
    } else {
        ayla.guildsColl.insertOne(
                AylaGuildConfig(this.id)
        )

        AylaGuildConfig(this.id)
    }
} set(config) {

    val options = UpdateOptions().upsert(true)
    ayla.guildsColl.replaceOne(
            Filters.eq("_id", this.id),
            config,
            options
    )
}

val String.fancy: String get() = this.substring(0, 1).toUpperCase() + this.substring(1).toLowerCase()

val ayla = AylaLauncher.ayla

fun Message.onReactionAdd(remove: Boolean = false, function: (MessageReactionAddEvent) -> Unit) {
    val interaction = ayla.messageInteractionCache.getOrPut(this.id) { MessageInteraction(this.id, remove) }

    interaction.onReactionAdd = function
}

fun Message.onReactionRemove(remove: Boolean = false, function: (MessageReactionRemoveEvent) -> Unit) {
    val interaction = ayla.messageInteractionCache.getOrPut(this.id) { MessageInteraction(this.id, remove) }

    interaction.onReactionRemove = function
}

fun TextChannel.onMessage(remove: Boolean = false, function: (MessageReceivedEvent) -> Unit) {
    val interaction = ayla.messageInteractionCache.getOrPut(this.id) { MessageInteraction(this.id, remove) }

    interaction.onResponse = function
}

class MessageInteraction(val id: String, val remove: Boolean) {
    var onReactionAdd: ((MessageReactionAddEvent) -> Unit)? = null
    var onReactionRemove: ((MessageReactionRemoveEvent) -> Unit)? = null
    var onResponse: ((MessageReceivedEvent) -> Unit)? = null
}

fun String.isValidSnowflake(): Boolean {
    return try {
        MiscUtil.parseSnowflake(this)

        true
    } catch (e: NumberFormatException) {
        false
    }
}

fun String.isValidUrl(): Boolean {
    try {
        val url = URL(this)

        return true
    } catch (e: MalformedURLException) {
        return false
    }
}

fun User.saveProfile(aylaUser: AylaUser) {
    this.aylaUser = aylaUser
}

fun Guild.saveConfig(config: AylaGuildConfig) {
    this.config = config
}

fun OffsetDateTime.humanize(): String {
    val month = when (this.month) {
        Month.JANUARY -> "Janeiro"
        Month.FEBRUARY -> "Fevereiro"
        Month.MARCH -> "Março"
        Month.APRIL -> "Abril"
        Month.MAY -> "Maio"
        Month.JUNE -> "Junho"
        Month.JULY -> "Julho"
        Month.AUGUST -> "Agosto"
        Month.SEPTEMBER -> "Setembro"
        Month.OCTOBER -> "Outubro"
        Month.NOVEMBER -> "Novembro"
        Month.DECEMBER -> "Dezembro"

        else -> "Irineu, você não sabe e nem eu!"
    }

    return "${this.dayOfMonth} de $month de ${this.year} às ${this.hour.toString().padStart(2, '0')}:${this.minute.toString().padStart(2, '0')}:${this.second.toString().padStart(2, '0')}"
}

fun OnlineStatus.humanize(): String {
    return when(this) {
        OnlineStatus.ONLINE -> "Online"
        OnlineStatus.DO_NOT_DISTURB -> "Não incomode"
        OnlineStatus.IDLE -> "Ausente"
        OnlineStatus.INVISIBLE -> "Invisível"
        OnlineStatus.OFFLINE -> "Offline"

        OnlineStatus.UNKNOWN -> "Irineu, você não sabe e nem eu!"
        else -> "Irineu, você não sabe e nem eu!"
    }
}

fun String.escapeMentions(): String {
    return this.replace("@", "@\u200B")
}

fun String.stripCodeMarks(): String {
    return this.replace("`", "")
}