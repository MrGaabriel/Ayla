package me.mrgaabriel.ayla.listeners

import com.mongodb.client.model.*
import me.mrgaabriel.ayla.utils.*
import me.mrgaabriel.ayla.utils.eventlog.*
import net.dv8tion.jda.core.*
import net.dv8tion.jda.core.events.guild.*
import net.dv8tion.jda.core.events.guild.member.*
import net.dv8tion.jda.core.events.message.guild.*
import net.dv8tion.jda.core.hooks.*
import org.apache.commons.lang3.exception.*
import org.slf4j.*
import java.awt.*
import java.time.*
import java.time.format.*

class EventLogListeners : ListenerAdapter() {

    val logger = LoggerFactory.getLogger(EventLogListeners::class.java)

    override fun onGenericGuild(event: GenericGuildEvent) {
        try {
            handleEvent(event)
        } catch (e: Exception) {
            logger.error("Erro ao processar \"handleEvent()\" para o evento $event na guild ${event.guild}")
            logger.error(ExceptionUtils.getStackTrace(e))
        }
    }

    fun handleEvent(event: GenericGuildEvent) {
        ayla.executor.execute {
            val guild = event.guild
            val config = guild.config

            if (config.eventLogEnabled) {
                val channel = guild.getTextChannelById(config.eventLogChannel)

                if (channel == null) {
                    config.eventLogEnabled = false
                    return@execute
                }

                if (event is GuildMessageUpdateEvent) {
                    val storedMessage = ayla.storedMessagesColl.find(
                            Filters.eq("_id", event.messageId)
                    ).firstOrNull() ?: return@execute

                    val oldContent = storedMessage.content.replace("`", "")
                    val newContent = event.message.contentDisplay.replace("`", "")

                    val builder = EmbedBuilder()

                    builder.setAuthor(event.author.tag, null, event.author.effectiveAvatarUrl)
                    builder.setDescription("**Uma mensagem foi editada no canal ${event.channel.asMention}**\n\nConteúdo antigo: ```\u200b$oldContent```\n\nConteúdo atual: ```\u200b$newContent```")
                    builder.setColor(Color.RED)

                    builder.setTimestamp(OffsetDateTime.now())
                    builder.setFooter("ID do usuário: ${event.author.id}", null)

                    channel.sendMessage(builder.build()).queue()

                    val newStoredMessage = StoredMessage(
                            event.messageId,
                            event.message.contentRaw,
                            event.author.id,
                            event.channel.id
                    )
                    ayla.storedMessagesColl.replaceOne(
                            Filters.eq("_id", event.messageId),
                            newStoredMessage
                    )
                }

                if (event is GuildMessageDeleteEvent) {
                    val storedMessage = ayla.storedMessagesColl.find(
                            Filters.eq("_id", event.messageId)
                    ).firstOrNull() ?: return@execute

                    val oldContent = storedMessage.content.replace("`", "")

                    val builder = EmbedBuilder()

                    val author = ayla.getUserById(storedMessage.authorId) ?: return@execute
                    builder.setAuthor(author.tag, null, author.effectiveAvatarUrl)
                    builder.setDescription("**Uma mensagem foi apagada no canal ${event.channel.asMention}**\n\nConteúdo: ```\u200b$oldContent```")
                    builder.setColor(Color.RED)

                    builder.setTimestamp(OffsetDateTime.now())
                    builder.setFooter("ID do usuário: ${author.id}", null)

                    channel.sendMessage(builder.build()).queue()

                    ayla.storedMessagesColl.deleteOne(
                            Filters.eq("_id", event.messageId)
                    )
                }

                if (event is GuildMemberJoinEvent) {
                    val user = event.user
                    val timestamp = user.creationTime

                    val now = OffsetDateTime.now()

                    if (now.dayOfYear - timestamp.dayOfYear < 4 && now.year == timestamp.year) {
                        val builder = EmbedBuilder()

                        builder.setAuthor(event.user.tag, null, event.user.effectiveAvatarUrl)
                        builder.setDescription("**Um usuário suspeito acabou de entrar no servidor...\nConta criada há menos de 4 dias!**\n\nUsuário: `${event.user.tag}`")

                        builder.addField("Data de criação da conta", timestamp.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/YYYY HH:mm:ss")), true)
                        builder.setColor(Color.RED)

                        builder.setFooter("ID do usuário: ${event.user.id}", null)

                        channel.sendMessage(builder.build()).queue()
                    }
                }
            }
        }
    }
}