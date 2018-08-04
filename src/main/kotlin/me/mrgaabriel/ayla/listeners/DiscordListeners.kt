package me.mrgaabriel.ayla.listeners

import me.mrgaabriel.ayla.utils.*
import net.dv8tion.jda.core.events.message.guild.*
import net.dv8tion.jda.core.hooks.*

class DiscordListeners : ListenerAdapter() {

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
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