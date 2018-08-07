package me.mrgaabriel.ayla.listeners

import com.mongodb.client.model.*
import me.mrgaabriel.ayla.utils.*
import me.mrgaabriel.ayla.utils.eventlog.*
import net.dv8tion.jda.core.events.message.guild.*
import net.dv8tion.jda.core.hooks.*

class DiscordListeners : ListenerAdapter() {

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        ayla.executor.execute {
            if (event.message.contentRaw == "<@${ayla.config.clientId}>" || event.message.contentRaw == "<@!${ayla.config.clientId}>") {
                event.channel.sendMessage("Olá, ${event.author.asMention}! Meu nome é Ayla e o meu prefixo para comandos neste servidor é `${event.guild.config.prefix}`! Para saber o que eu posso fazer, use `${event.guild.config.prefix}help`").complete()
                return@execute
            }

            // Porque guardar mensagens!? Você está me espionando!?
            // - Isso serve para o event-log, já que a API não fornece a mensagem que foi apagada/editada, nós temos que guardar elas para pegar o conteúdo!
            if (event.guild.config.eventLogEnabled) {
                val storedMessage = StoredMessage(
                        event.messageId,
                        event.message.contentRaw,
                        event.message.author.id,
                        event.message.channel.id
                )

                ayla.storedMessagesColl.replaceOne(
                        Filters.eq("_id", event.messageId),
                        storedMessage,
                        Constants.UPDATE_OPTIONS
                )
            }

            ayla.commandMap.forEach {
                if (it.matches(event.message))
                    return@execute
            }
        }
    }

    override fun onGuildMessageUpdate(event: GuildMessageUpdateEvent) {
        ayla.executor.execute {
            if (event.message.contentRaw == "<@${ayla.config.clientId}>" || event.message.contentRaw == "<@!${ayla.config.clientId}>") {
                event.channel.sendMessage("Olá, ${event.author.asMention}! Meu nome é Ayla e o meu prefixo para comandos neste servidor é `${event.guild.config.prefix}`! Para saber o que eu posso fazer, use `${event.guild.config.prefix}help`").complete()
                return@execute
            }

            ayla.commandMap.forEach {
                if (it.matches(event.message))
                    return@execute
            }
        }
    }
}