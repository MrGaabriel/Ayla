package me.mrgaabriel.ayla.utils

import com.mongodb.client.model.*
import me.mrgaabriel.ayla.*
import me.mrgaabriel.ayla.data.*
import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.events.message.react.*

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