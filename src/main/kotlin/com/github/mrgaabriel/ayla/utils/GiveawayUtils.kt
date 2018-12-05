package com.github.mrgaabriel.ayla.utils

import com.github.mrgaabriel.ayla.dao.Giveaway
import com.github.mrgaabriel.ayla.tables.Giveaways
import com.github.mrgaabriel.ayla.utils.extensions.await
import com.github.mrgaabriel.ayla.utils.extensions.ayla
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.JDA
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color
import java.time.Instant
import java.time.OffsetDateTime

object GiveawayUtils {

    val logger by logger()

    fun spawnTasks() {
        val giveaways = transaction(ayla.database) {
            Giveaway.all().toList()
        }

        for (giveaway in giveaways) {
            spawnTask(giveaway)
        }
    }

    fun spawnTask(giveaway: Giveaway) {
        GlobalScope.launch(ayla.giveawayTasksDispatcher) {
            logger.info("Criando giveaway task para o término do sorteio na guild ${giveaway.guildId} - channelId: ${giveaway.channelId} - messageId: ${giveaway.messageId}")
            val delay = giveaway.endsAt - System.currentTimeMillis()
            delay(delay)

            val channel = ayla.shardManager.getTextChannelById(giveaway.channelId)

            if (channel == null) {
                transaction(ayla.database) {
                    giveaway.delete()
                }

                return@launch
            }

            val message = channel.getMessageById(giveaway.messageId).await()

            if (message == null) {
                transaction(ayla.database) {
                    giveaway.delete()
                }

                return@launch
            }

            val giveaway = transaction(ayla.database) {
                Giveaway.find { Giveaways.messageId eq giveaway.messageId }.first()
            }

            val participating = giveaway.users.toList()

            if (participating.isEmpty()) {
                val builder = EmbedBuilder()

                builder.setTitle(":tada: SORTEIO!!! :tada:")

                builder.setTimestamp(OffsetDateTime.now())
                builder.setFooter("Terminou", null)

                builder.setDescription("Corra para participar do sorteio! Clique no :tada:!")

                builder.addField("Prêmio", giveaway.prize, true)
                builder.addField("Termina em", "Já terminou! :tada:", true)
                builder.addField("Ganhador", "Ninguém ¯\\_(ツ)_/¯", false)

                builder.setColor(Color.BLACK)

                message.editMessage(builder.build()).queue()
                channel.sendMessage("Ninguém ganhou `${giveaway.prize}` porque ninguém está participando!").queue()

                transaction(ayla.database) {
                    giveaway.delete()
                }

                return@launch
            }

            val chosenUser = participating.random()

            val builder = EmbedBuilder()

            builder.setTitle(":tada: SORTEIO!!! :tada:")

            builder.setTimestamp(OffsetDateTime.now())
            builder.setFooter("Terminou", null)

            builder.setDescription("Corra para participar do sorteio! Clique no :tada:!")

            builder.addField("Prêmio", giveaway.prize, true)
            builder.addField("Termina em", "Já terminou! :tada:", true)
            builder.addField("Ganhador", "<@$chosenUser>", false)

            builder.setColor(Color.BLACK)

            message.editMessage(builder.build()).queue()
            channel.sendMessage("Parabéns <@$chosenUser> por ter ganhado `${giveaway.prize}` no sorteio!").queue()

            transaction(ayla.database) {
                giveaway.delete()
            }
        }

        GlobalScope.launch(ayla.giveawayTasksDispatcher) {
            while (giveaway.endsAt > System.currentTimeMillis() && ayla.shardManager.shards.all { it.status == JDA.Status.CONNECTED }) {
                val channel = ayla.shardManager.getTextChannelById(giveaway.channelId)

                if (channel == null) {
                    transaction(ayla.database) {
                        giveaway.delete()
                    }

                    return@launch
                }

                val message = channel.getMessageById(giveaway.messageId).await()

                if (message == null) {
                    transaction(ayla.database) {
                        giveaway.delete()
                    }

                    return@launch
                }

                val builder = EmbedBuilder()

                builder.setTitle(":tada: SORTEIO!!! :tada:")

                builder.setTimestamp(Instant.ofEpochMilli(giveaway.endsAt))
                builder.setFooter("Terminará", null)

                builder.setDescription("Corra para participar do sorteio! Clique no :tada:!")

                builder.addField("Prêmio", giveaway.prize, true)
                builder.addField("Termina em", DateUtils.formatDateDiff(giveaway.endsAt), true)
                builder.addField("Ganhador", "Ninguém ¯\\_(ツ)_/¯ (ainda!)", false)

                builder.setColor(Color.BLUE)

                message.editMessage(builder.build()).queue()

                delay(30 * 1000)
            }
        }
    }
}