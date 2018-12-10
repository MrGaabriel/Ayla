package com.github.mrgaabriel.ayla.commands.misc

import com.github.mrgaabriel.ayla.commands.AbstractCommand
import com.github.mrgaabriel.ayla.commands.CommandCategory
import com.github.mrgaabriel.ayla.commands.CommandContext
import com.github.mrgaabriel.ayla.dao.Giveaway
import com.github.mrgaabriel.ayla.utils.GiveawayUtils
import com.github.mrgaabriel.ayla.utils.convertToEpochMillis
import com.github.mrgaabriel.ayla.utils.extensions.ayla
import org.jetbrains.exposed.sql.transactions.transaction

class GiveawayCommand : AbstractCommand("giveaway", listOf("sorteio"), category = CommandCategory.MISC) {

    override fun getDescription(): String {
        return "Crie um sorteio"
    }

    override fun getUsage(): String {
        return "tempo prêmio"
    }

    override suspend fun run(context: CommandContext) {
        if (context.args.size < 2) {
            context.explain()
            return
        }

        val endsIn = context.args[0].convertToEpochMillis()
        if (endsIn == System.currentTimeMillis()) { // incorreto
            context.sendMessage("${context.event.author.asMention} Olha, eu não sei se você está no futuro ou algo do tipo, mas eu não reconheço `${context.args[0]}` como um tempo válido!")
            return
        }

        context.args.removeAt(0)
        val prize = context.args.joinToString(" ")

        val message = context.sendMessage("\u200B")
        message.addReaction("\uD83C\uDF89").queue()

        val giveaway = transaction(ayla.database) {
            Giveaway.new {
                this.guildId = context.event.guild.id
                this.channelId = context.event.channel.id
                this.messageId = message.id
                this.authorId = context.event.author.id

                this.endsAt = endsIn
                this.prize = prize

                this.users = arrayOf()
            }
        }

        GiveawayUtils.spawnTask(giveaway)
    }
}