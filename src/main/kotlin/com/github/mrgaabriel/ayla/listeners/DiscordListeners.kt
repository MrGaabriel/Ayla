package com.github.mrgaabriel.ayla.listeners

import com.github.mrgaabriel.ayla.dao.Giveaway
import com.github.mrgaabriel.ayla.dao.Guild
import com.github.mrgaabriel.ayla.dao.UserProfile
import com.github.mrgaabriel.ayla.events.AylaMessageEvent
import com.github.mrgaabriel.ayla.tables.Giveaways
import com.github.mrgaabriel.ayla.tables.Guilds
import com.github.mrgaabriel.ayla.tables.UserProfiles
import com.github.mrgaabriel.ayla.utils.extensions.ayla
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.core.events.message.guild.GuildMessageUpdateEvent
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionRemoveEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

class DiscordListeners : ListenerAdapter() {

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        if (event.author.isBot)
            return

        GlobalScope.launch(ayla.defaultCoroutineDispatcher) {
            val aylaEvent = AylaMessageEvent(event.message)

            val profile = transaction(ayla.database) {
                UserProfile.find { UserProfiles.id eq event.author.id }.firstOrNull()
            }

            if (profile == null) {
                transaction(ayla.database) {
                    UserProfile.new(event.author.id) {}
                }

                return@launch
            }

            var config = transaction(ayla.database) {
                Guild.find { Guilds.id eq event.guild.id }.firstOrNull()
            }

            if (config == null) {
                transaction(ayla.database) {
                    config = Guild.new(event.guild.id) {}
                }

                return@launch
            }

            val matcher = Pattern.compile("^<@[!]?${ayla.config.clientId}>$")
                .matcher(event.message.contentRaw)

            if (matcher.find()) {
                event.channel.sendMessage("\uD83D\uDD39 **|** ${event.author.asMention} Olá! Meu nome é Ayla e eu sou só mais um bot de terras tupiniquins criado para alegrar seu servidor!\n\uD83D\uDD39 **|** Neste servidor, o prefixo é `${config!!.prefix}`. Se quiser ver o que eu posso fazer, use `${config!!.prefix}help`")
                    .queue()
            }

            ayla.commandMap.forEach {
                if (it.matches(aylaEvent))
                    return@launch
            }
        }
    }

    override fun onGuildMessageUpdate(event: GuildMessageUpdateEvent) {
        GlobalScope.launch(ayla.defaultCoroutineDispatcher) {
            val aylaEvent = AylaMessageEvent(event.message)

            val profile = transaction(ayla.database) {
                UserProfile.find { UserProfiles.id eq event.author.id }.firstOrNull()
            }

            if (profile == null) {
                transaction(ayla.database) {
                    UserProfile.new(event.author.id) {}
                }

                return@launch
            }

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

    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        val config = transaction(ayla.database) {
            Guild.find { Guilds.id eq event.guild.id }.firstOrNull()
        }

        if (config != null && config.welcomeEnabled && config.welcomeChannelId != null) {
            val channel = event.guild.getTextChannelById(config.welcomeChannelId)

            val builder = EmbedBuilder()

            builder.setAuthor("${event.user.name}#${event.user.discriminator}", null, event.user.effectiveAvatarUrl)
            builder.setColor(Color.GREEN)

            builder.setThumbnail(event.user.effectiveAvatarUrl)

            builder.setTitle(":wave: Bem vindo ${event.user.name}!")
            builder.setDescription("Boas vindas ${event.user.asMention} ao servidor! Esperamos que você se divirta!")

            builder.addField(
                "Conta criada em",
                event.user.creationTime.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/YYYY HH:mm:ss")),
                false
            )

            builder.setFooter("ID do usuário: ${event.user.id}", null)

            val message = MessageBuilder()
                .setContent(event.user.asMention)
                .setEmbed(builder.build())
                .build()

            channel.sendMessage(message).queue()
        }
    }

    override fun onGuildMemberLeave(event: GuildMemberLeaveEvent) {
        val config = transaction(ayla.database) {
            Guild.find { Guilds.id eq event.guild.id }.firstOrNull()
        }

        if (config != null && config.welcomeEnabled && config.welcomeChannelId != null) {
            val channel = event.guild.getTextChannelById(config.welcomeChannelId)

            val builder = EmbedBuilder()

            builder.setAuthor("${event.user.name}#${event.user.discriminator}", null, event.user.effectiveAvatarUrl)
            builder.setColor(Color.RED)

            builder.setThumbnail(event.user.effectiveAvatarUrl)

            builder.setTitle(":wave: Adeus ${event.user.name}!")
            builder.setDescription("Adeus ${event.user.name}! Esperamos que você volte em breve!")

            builder.setFooter("ID do usuário: ${event.user.id}", null)

            val message = MessageBuilder()
                .setContent(event.user.asMention)
                .setEmbed(builder.build())
                .build()

            channel.sendMessage(message).queue()
        }
    }

    override fun onGuildVoiceLeave(event: GuildVoiceLeaveEvent) {
        val selfMember = event.guild.selfMember

        val channel = event.channelLeft
        if (channel == selfMember.voiceState.channel) {
            if (channel.members.size == 1) { // Só tem o bot?
                val player = ayla.audioManager.getAudioPlayer(event.guild)

                if (player.playingTrack != null) {
                    player.stopTrack()
                }

                player.link.disconnect()
                player.link.destroy()

                ayla.audioManager.musicPlayers.remove(event.guild.id)
            }
        }
    }
}