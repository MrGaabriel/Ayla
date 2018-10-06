package me.mrgaabriel.ayla.commands.moderation

import me.mrgaabriel.ayla.utils.commands.AbstractCommand
import me.mrgaabriel.ayla.utils.commands.CommandCategory
import me.mrgaabriel.ayla.utils.commands.CommandContext
import me.mrgaabriel.ayla.utils.commands.annotations.ArgumentType
import me.mrgaabriel.ayla.utils.commands.annotations.InjectArgument
import me.mrgaabriel.ayla.utils.commands.annotations.Subcommand
import me.mrgaabriel.ayla.utils.commands.annotations.SubcommandPermissions
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.User

class ClearCommand : AbstractCommand("clear", category = CommandCategory.MODERATION, description = "Limpa mensagens do canal de texto", aliases = listOf("clean", "limpar", "chatclean")) {

    @Subcommand
    @SubcommandPermissions(permissions = [Permission.MESSAGE_MANAGE], botPermissions = [Permission.MESSAGE_MANAGE])
    fun clear(context: CommandContext, quantity: Int?, @InjectArgument(ArgumentType.USER) user: User?) {
        if (quantity == null) {
            return context.explain()
        }

        if (quantity !in 2..100) {
            return context.sendMessage(context.getAsMention(true) + "Eu só posso apagar de 2 a 100 mensagens!")
        }

        context.channel.getHistoryBefore(context.message, quantity).queue {
            var messages = it.retrievedHistory
                    .filter { (System.currentTimeMillis() - 1209600000) < it.creationTime.toInstant().toEpochMilli() }

            if (user != null) {
                messages = messages.filter { it.author.id == user.id }
            }

            val messagesIds = mutableListOf<String>()
                    .apply { messages.forEach { this.add(it.id) } }

            context.channel.deleteMessagesByIds(messagesIds).queue()

            val failed = it.retrievedHistory.size - messages.size
            if (failed != 0) {
                return@queue context.sendMessage(context.getAsMention(true) + "Chat limpo com sucesso, porém não consegui limpar $failed mensagens pelo fato delas terem sido enviadas há mais de duas semanas!")
            }
            
            context.sendMessage(context.getAsMention(true) + "Chat limpo com sucesso!")
        }
    }
}
