package com.github.mrgaabriel.ayla.listeners

import com.github.mrgaabriel.ayla.dao.Guild
import com.github.mrgaabriel.ayla.events.AylaMessageEvent
import com.github.mrgaabriel.ayla.tables.Guilds
import com.github.mrgaabriel.ayla.utils.extensions.ayla
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.MessageUpdateEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import org.jetbrains.exposed.sql.transactions.transaction

class DiscordListeners : ListenerAdapter() {

    override fun onMessageReceived(event: MessageReceivedEvent) {
        GlobalScope.launch {
            val aylaEvent = AylaMessageEvent(event.message)

            val config = transaction(ayla.database) {
                Guild.find { Guilds.id eq event.guild.idLong }.firstOrNull()
            }

            if (config == null) {
                transaction(ayla.database) {
                    Guild.new(event.guild.idLong) {
                        this.prefix = ".."
                    }
                }
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

}