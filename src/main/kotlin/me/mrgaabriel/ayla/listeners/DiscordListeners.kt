package me.mrgaabriel.ayla.listeners

import com.mongodb.client.model.Filters
import me.mrgaabriel.ayla.modules.BadWordModule
import me.mrgaabriel.ayla.utils.Constants
import me.mrgaabriel.ayla.utils.ayla
import me.mrgaabriel.ayla.utils.config
import me.mrgaabriel.ayla.utils.eventlog.StoredMessage
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.core.events.message.guild.GuildMessageUpdateEvent
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.LoggerFactory
import java.awt.Color
import java.time.format.DateTimeFormatter

class DiscordListeners : ListenerAdapter() {

    val logger = LoggerFactory.getLogger(DiscordListeners::class.java)

    override fun onGenericMessageReaction(event: GenericMessageReactionEvent) {
        if (event is MessageReactionAddEvent) {
            ayla.messageInteractionCache.forEach { id, wrapper ->
                if (event.messageId == id) {
                    if (wrapper.onReactionAdd != null) {
                        ayla.executor.execute {
                            try {
                                wrapper.onReactionAdd!!.invoke(event)

                                if (wrapper.removeWhenExecuted) {
                                    ayla.messageInteractionCache.remove(id)
                                }
                            } catch (e: Exception) {
                                logger.error("Erro ao processar onReactionAdd para a mensagem ${event.messageId}")
                                logger.error(ExceptionUtils.getStackTrace(e))
                            }
                        }
                    }
                }
            }
        } else if (event is MessageReactionRemoveEvent) {
            ayla.messageInteractionCache.forEach { id, wrapper ->
                if (event.messageId == id) {
                    if (wrapper.onReactionRemove != null) {
                        ayla.executor.execute {
                            try {
                                wrapper.onReactionRemove!!.invoke(event)

                                if (wrapper.removeWhenExecuted) {
                                    ayla.messageInteractionCache.remove(id)
                                }
                            } catch (e: Exception) {
                                logger.error("Erro ao processar onReactionRemove para a mensagem ${event.messageId}")
                                logger.error(ExceptionUtils.getStackTrace(e))
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onGuildVoiceLeave(event: GuildVoiceLeaveEvent) {
        val selfMember = event.guild.selfMember

        val channel = event.channelLeft
        if (channel == selfMember.voiceState.channel) {
            if (channel.members.size == 1) { // Só tem o bot?
                val player = ayla.audioManager.getPlayer(event.guild)

                if (player.playingTrack != null) {
                    player.stopTrack()
                }
                
                event.guild.audioManager.closeAudioConnection()
            }
        }
    }

    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        ayla.executor.execute {
            val config = event.guild.config

            if (config.welcomeEnabled && config.welcomeChannel.isNotEmpty()) {
                val channelId = config.welcomeChannel
                val channelHandle = event.guild.getTextChannelById(channelId)

                if (channelHandle == null) {
                    config.welcomeEnabled = false
                    event.guild.config = config

                    return@execute
                }

                val builder = EmbedBuilder()

                builder.setAuthor("${event.user.name}#${event.user.discriminator}", null, event.user.effectiveAvatarUrl)
                builder.setColor(Color.GREEN)

                builder.setThumbnail(event.user.effectiveAvatarUrl)

                builder.setTitle(":wave: Bem vindo ${event.user.name}!")
                builder.setDescription("Boas vindas ${event.user.asMention} ao servidor! Esperamos que você se divirta!")

                builder.addField("Conta criada em", event.user.creationTime.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/YYYY HH:mm:ss")), false)

                builder.setFooter("ID do usuário: ${event.user.id}", null)

                val message = MessageBuilder()
                        .setContent(event.user.asMention)
                        .setEmbed(builder.build())
                        .build()

                channelHandle.sendMessage(message).queue()
            }
        }
    }

    override fun onGuildMemberLeave(event: GuildMemberLeaveEvent) {
        ayla.executor.execute {
            val config = event.guild.config

            if (config.welcomeEnabled && config.welcomeChannel.isNotEmpty()) {
                val channelId = config.welcomeChannel
                val channelHandle = event.guild.getTextChannelById(channelId)

                if (channelHandle == null) {
                    config.welcomeEnabled = false
                    event.guild.config = config

                    return@execute
                }

                val builder = EmbedBuilder()

                builder.setAuthor("${event.user.name}#${event.user.discriminator}", null, event.user.effectiveAvatarUrl)
                builder.setColor(Color.RED)

                builder.setThumbnail(event.user.effectiveAvatarUrl)

                builder.setTitle(":wave: Adeus ${event.user.name}!")
                builder.setDescription("Adeus ${event.user.name}! Esperamos que você volte em breve!")

                builder.setFooter("ID do usuário: ${event.user.id}", null)

                val message = MessageBuilder()
                        .setEmbed(builder.build())
                        .build()

                channelHandle.sendMessage(message).queue()
            }
        }
    }

    override fun onGuildLeave(event: GuildLeaveEvent) {
        ayla.executor.execute {
            ayla.guildsColl.deleteOne(
                    Filters.eq("_id", event.guild.id)
            )
        }
    }

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        ayla.executor.execute {
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

            if (event.author.isBot)
                return@execute
            
            if (event.message.contentRaw == "<@${ayla.config.clientId}>" || event.message.contentRaw == "<@!${ayla.config.clientId}>") {
                event.channel.sendMessage("Olá, ${event.author.asMention}! Meu nome é Ayla e o meu prefixo para comandos neste servidor é `${event.guild.config.prefix}`! Para saber o que eu posso fazer, use `${event.guild.config.prefix}help`").queue()
                return@execute
            }

            ayla.commandMap.forEach {
                if (it.matches(event.message))
                    return@execute
            }

            BadWordModule.handleMessage(event.message)
        }
    }

    override fun onGuildMessageUpdate(event: GuildMessageUpdateEvent) {
        ayla.executor.execute {
            if (event.message.contentRaw == "<@${ayla.config.clientId}>" || event.message.contentRaw == "<@!${ayla.config.clientId}>") {
                event.channel.sendMessage("Olá, ${event.author.asMention}! Meu nome é Ayla e o meu prefixo para comandos neste servidor é `${event.guild.config.prefix}`! Para saber o que eu posso fazer, use `${event.guild.config.prefix}help`").queue()
                return@execute
            }

            ayla.commandMap.forEach {
                if (it.matches(event.message))
                    return@execute
            }

            BadWordModule.handleMessage(event.message)
        }
    }
}
