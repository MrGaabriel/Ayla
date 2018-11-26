package com.github.mrgaabriel.ayla.utils

import com.github.mrgaabriel.ayla.dao.Giveaway
import com.github.mrgaabriel.ayla.tables.Giveaways
import com.github.mrgaabriel.ayla.utils.extensions.await
import com.github.mrgaabriel.ayla.utils.extensions.ayla
import com.github.mrgaabriel.ayla.utils.extensions.tag
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import net.dv8tion.jda.core.EmbedBuilder
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color
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
        GlobalScope.launch {
            logger.info("Criando giveaway task para o término do sorteio na guild ${giveaway.guildId} - channelId: ${giveaway.channelId} - messageId: ${giveaway.messageId}")
            val delay = giveaway.endsAt - System.currentTimeMillis()
            delay(delay)

            val channel = ayla.shardManager.getTextChannelById(giveaway.channelId)
            val message = channel.getMessageById(giveaway.messageId).await()
            val author = ayla.shardManager.retrieveUserById(giveaway.authorId).await()

            val giveaway = transaction(ayla.database) {
                Giveaway.find { Giveaways.messageId eq giveaway.messageId }.first()
            }

            val participating = giveaway.users.toList()

            if (participating.isEmpty()) {
                val builder = EmbedBuilder()

                builder.setTitle(":tada: SORTEIO!!! :tada:")

                builder.setTimestamp(OffsetDateTime.now())
                builder.setFooter(author.tag, author.effectiveAvatarUrl)

                builder.setDescription("Corra para participar do sorteio! Clique no :tada:!")

                builder.addField("Prêmio", giveaway.prize, true)
                builder.addField("Termina em", "Já terminou! :tada:", true)
                builder.addField("Ganhador", "Ninguém ¯\\_(ツ)_/¯", false)

                builder.setColor(Color.BLACK)

                message.editMessage(builder.build()).queue()
                channel.sendMessage("Ninguém ganhou `${giveaway.prize}` porque ninguém está participando!").queue()

                transaction(ayla.database) {
                    Giveaways.deleteWhere { Giveaways.messageId eq giveaway.messageId }
                }

                return@launch
            }

            val chosenUser = participating.random()

            val builder = EmbedBuilder()

            builder.setTitle(":tada: SORTEIO!!! :tada:")

            builder.setTimestamp(OffsetDateTime.now())
            builder.setFooter(author.tag, author.effectiveAvatarUrl)

            builder.setDescription("Corra para participar do sorteio! Clique no :tada:!")

            builder.addField("Prêmio", giveaway.prize, true)
            builder.addField("Termina em", "Já terminou! :tada:", true)
            builder.addField("Ganhador", "<@$chosenUser>", false)

            builder.setColor(Color.BLACK)

            message.editMessage(builder.build()).queue()
            channel.sendMessage("Parabéns <@$chosenUser> por ter ganhado `${giveaway.prize}` no sorteio!").queue()

            transaction(ayla.database) {
                Giveaways.deleteWhere { Giveaways.messageId eq giveaway.messageId }
            }
        }

        GlobalScope.launch {
            while (giveaway.endsAt > System.currentTimeMillis()) {
                val channel = ayla.shardManager.getTextChannelById(giveaway.channelId)
                val message = channel.getMessageById(giveaway.messageId).await()
                val author = ayla.shardManager.retrieveUserById(giveaway.authorId).await()

                val builder = EmbedBuilder()

                builder.setTitle(":tada: SORTEIO!!! :tada:")

                builder.setTimestamp(OffsetDateTime.now())
                builder.setFooter(author.tag, author.effectiveAvatarUrl)

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