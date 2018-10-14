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
import net.dv8tion.jda.core.requests.RestAction
import net.dv8tion.jda.core.utils.MiscUtil
import java.net.MalformedURLException
import java.net.URL
import java.time.Month
import java.time.OffsetDateTime
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

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

// https://github.com/LorittaBot/Loritta
val TIME_PATTERN = "(([01]\\d|2[0-3]):([0-5]\\d)(:([0-5]\\d))?) ?(am|pm)?".toPattern()
val DATE_PATTERN = "(0[1-9]|[12][0-9]|3[01])[- /.](0[1-9]|1[012])[- /.]([0-9]+)".toPattern()

fun String.convertToEpochMillis(): Long {
    val content = this.toLowerCase()
    val calendar = Calendar.getInstance()

    if (content.contains(":")) { // horário
        val matcher = TIME_PATTERN.matcher(content)

        if (matcher.find()) { // Se encontrar...
            val hour = matcher.group(2).toIntOrNull() ?: 0
            val minute = matcher.group(3).toIntOrNull() ?: 0
            val seconds = try {
                matcher.group(5).toIntOrNull() ?: 0
            } catch (e: IllegalStateException) {
                0
            }

            var meridiem = try {
                matcher.group(6)
            } catch (e: IllegalStateException) {
                null
            }

            // Horários que usam o meridiem
            if (meridiem != null) {
                meridiem = meridiem.replace(".", "").replace(" ", "")
                if (meridiem.equals("pm", true)) { // Se for PM, aumente +12
                    calendar[Calendar.HOUR_OF_DAY] = (hour % 12) + 12
                } else { // Se for AM, mantenha do jeito atual
                    calendar[Calendar.HOUR_OF_DAY] = (hour % 12)
                }
            } else {
                calendar[Calendar.HOUR_OF_DAY] = hour
            }
            calendar[Calendar.MINUTE] = minute
            calendar[Calendar.SECOND] = seconds
        }
    }

    if (content.contains("/")) { // data
        val matcher = DATE_PATTERN.matcher(content)

        if (matcher.find()) { // Se encontrar...
            val day = matcher.group(1).toIntOrNull() ?: 1
            val month = matcher.group(2).toIntOrNull() ?: 1
            val year = matcher.group(3).toIntOrNull() ?: 1999

            calendar[Calendar.DAY_OF_MONTH] = day
            calendar[Calendar.MONTH] = month - 1
            calendar[Calendar.YEAR] = year
        }
    }

    val yearsMatcher = "([0-9]+) ?(y|a)".toPattern().matcher(content)
    if (yearsMatcher.find()) {
        val addYears = yearsMatcher.group(1).toIntOrNull() ?: 0
        calendar[Calendar.YEAR] += addYears
    }
    val monthMatcher = "([0-9]+) ?(month(s)?|m(e|ê)s(es?))".toPattern().matcher(content)
    if (monthMatcher.find()) {
        val addMonths = monthMatcher.group(1).toIntOrNull() ?: 0
        calendar[Calendar.MONTH] += addMonths
    }
    val weekMatcher = "([0-9]+) ?(w)".toPattern().matcher(content)
    if (weekMatcher.find()) {
        val addWeeks = weekMatcher.group(1).toIntOrNull() ?: 0
        calendar[Calendar.WEEK_OF_YEAR] += addWeeks
    }
    val dayMatcher = "([0-9]+) ?(d)".toPattern().matcher(content)
    if (dayMatcher.find()) {
        val addDays = dayMatcher.group(1).toIntOrNull() ?: 0
        calendar[Calendar.DAY_OF_YEAR] += addDays
    }
    val hourMatcher = "([0-9]+) ?(h)".toPattern().matcher(content)
    if (hourMatcher.find()) {
        val addHours = hourMatcher.group(1).toIntOrNull() ?: 0
        calendar[Calendar.HOUR_OF_DAY] += addHours
    }
    val minuteMatcher = "([0-9]+) ?(m)".toPattern().matcher(content)
    if (minuteMatcher.find()) {
        val addMinutes = minuteMatcher.group(1).toIntOrNull() ?: 0
        calendar[Calendar.MINUTE] += addMinutes
    }
    val secondsMatcher = "([0-9]+) ?(s)".toPattern().matcher(content)
    if (secondsMatcher.find()) {
        val addSeconds = secondsMatcher.group(1).toIntOrNull() ?: 0
        calendar[Calendar.SECOND] += addSeconds
    }

    return calendar.timeInMillis
}

fun <T> Array<T>.random(): T {
    val random = SplittableRandom()

    return this[random.nextInt(this.size)]
}

fun <T> List<T>.random(): T {
    val random = SplittableRandom()

    return this[random.nextInt(this.size)]
}

suspend fun <T> RestAction<T>.await(): T {
    return suspendCoroutine { cont ->
        this.queue({ cont.resume(it) }, { cont.resumeWithException(it) })
    }
}