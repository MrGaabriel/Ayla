package com.github.mrgaabriel.ayla.listeners

import com.github.mrgaabriel.ayla.dao.Giveaway
import com.github.mrgaabriel.ayla.dao.Guild
import com.github.mrgaabriel.ayla.events.AylaMessageEvent
import com.github.mrgaabriel.ayla.tables.Giveaways
import com.github.mrgaabriel.ayla.tables.Guilds
import com.github.mrgaabriel.ayla.utils.extensions.ayla
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.MessageUpdateEvent
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionRemoveEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.regex.Pattern

class DiscordListeners : ListenerAdapter() {

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.isBot)
            return

        GlobalScope.launch {
            val aylaEvent = AylaMessageEvent(event.message)

            var config = transaction(ayla.database) {
                Guild.find { Guilds.id eq event.guild.id }.firstOrNull()
            }

            if (config == null) {
                transaction(ayla.database) {
                    config = Guild.new(event.guild.id) {
                        this.prefix = ".."
                    }
                }
            }

            val matcher = Pattern.compile("^<@[!]?${ayla.config.clientId}>$")
                .matcher(event.message.contentRaw)

            if (matcher.find()) {
                event.channel.sendMessage("\uD83D\uDD39 **|** ${event.author.asMention} Olá! Meu nome é Ayla e eu sou só mais um bot de terras tupiniquins criado para alegrar seu servidor!\n\uD83D\uDD39 **|** Neste servidor, o prefixo é `${config!!.prefix}`. Se quiser ver o que eu posso fazer, use `${config!!.prefix}help`").queue()
            }

            ayla.commandMap.forEach {
                if (it.matches(aylaEvent))
                    return@launch
            }
        }
    }

    override fun onMessageUpdate(event: MessageUpdateEvent) {
        GlobalScope.launch {
            val aylaEvent = AylaMessageEvent(event.message)

            ayla.commandMap.forEach {
                if (it.matches(aylaEvent))
                    return@launch
            }
        }
    }

    override fun onGuildMessageReactionAdd(event: GuildMessageReactionAddEvent) {
        if (event.user.isBot)
            return

        if (event.reactionEmote.name == "\uD83C\uDF89") {
            val giveaway = transaction(ayla.database) {
                Giveaway.find { Giveaways.messageId eq event.messageId }.firstOrNull()
            }

            if (giveaway != null) {
                val participating = giveaway.users.toMutableList()
                participating.add(event.user.id)

                transaction(ayla.database) {
                    giveaway.users = participating.toTypedArray()
                }
            }
        }
    }

    override fun onGuildMessageReactionRemove(event: GuildMessageReactionRemoveEvent) {
        if (event.user.isBot)
            return

        if (event.reactionEmote.name == "\uD83C\uDF89") {
            val giveaway = transaction(ayla.database) {
                Giveaway.find { Giveaways.messageId eq event.messageId }.firstOrNull()
            }

            if (giveaway != null) {
                val participating = giveaway.users.toMutableList()
                participating.remove(event.user.id)

                transaction(ayla.database) {
                    giveaway.users = participating.toTypedArray()
                }
            }
        }
    }

}