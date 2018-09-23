package me.mrgaabriel.ayla.utils

import com.mongodb.client.model.Filters
import com.mongodb.client.model.UpdateOptions
import me.mrgaabriel.ayla.AylaLauncher
import me.mrgaabriel.ayla.data.AylaGuildConfig
import me.mrgaabriel.ayla.data.AylaUser
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveEvent
import net.dv8tion.jda.core.events.user.update.UserUpdateOnlineStatusEvent
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

    if (found != null) {
        return found
    } else {
        ayla.usersColl.insertOne(
                AylaUser(this.id)
        )

        return AylaUser(this.id)
    }
} set(user) {
    val conf = user

    val options = UpdateOptions().upsert(true)
    ayla.usersColl.replaceOne(
            Filters.eq("_id", this.id),
            conf,
            options
    )
}

var Guild.config: AylaGuildConfig get() {
    val found = ayla.guildsColl.find(
            Filters.eq("_id", this.id)
    ).firstOrNull()

    if (found != null) {
        return found
    } else {
        ayla.guildsColl.insertOne(
                AylaGuildConfig(this.id)
        )

        return AylaGuildConfig(this.id)
    }
} set(config) {
    val conf = config

    val options = UpdateOptions().upsert(true)
    ayla.guildsColl.replaceOne(
            Filters.eq("_id", this.id),
            conf,
            options
    )
}

val String.fancy: String get() = this.substring(0, 1).toUpperCase() + this.substring(1).toLowerCase()

val ayla = AylaLauncher.ayla

fun Message.collectReactionAdd(removeWhenExecuted: Boolean = false, function: (MessageReactionAddEvent) -> Unit) {
    val wrapper = ayla.messageInteractionCache.getOrPut(this.id) { MessageInteractionWrapper(this.id, removeWhenExecuted) }

    wrapper.onReactionAdd = function
}

fun Message.collectReactionRemove(removeWhenExecuted: Boolean = false, function: (MessageReactionRemoveEvent) -> Unit) {
    val wrapper = ayla.messageInteractionCache.getOrPut(this.id) { MessageInteractionWrapper(this.id, removeWhenExecuted) }

    wrapper.onReactionRemove = function
}

class MessageInteractionWrapper(
        val messageId: String,
        val removeWhenExecuted: Boolean
) {

    var onReactionAdd: ((MessageReactionAddEvent) -> Unit)? = null
    var onReactionRemove: ((MessageReactionRemoveEvent) -> Unit)? = null
}

fun String.isValidSnowflake(): Boolean {
    try {
        MiscUtil.parseSnowflake(this)

        return true
    } catch (e: NumberFormatException) {
        return false
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

    return "${this.dayOfMonth} de $month de ${this.year} às ${this.hour}:${this.minute}:${this.second}"
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