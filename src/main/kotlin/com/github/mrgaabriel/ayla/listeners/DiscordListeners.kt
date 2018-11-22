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
import java.util.regex.Pattern

class DiscordListeners : ListenerAdapter() {

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.isBot)
            return

        GlobalScope.launch {
            val aylaEvent = AylaMessageEvent(event.message)

            var config = transaction(ayla.database) {
                Guild.find { Guilds.id eq event.guild.idLong }.firstOrNull()
            }

            if (config == null) {
                transaction(ayla.database) {
                    config = Guild.new(event.guild.idLong) {
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

}