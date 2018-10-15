package me.mrgaabriel.ayla.commands.utils

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.mrgaabriel.ayla.utils.commands.AbstractCommand
import me.mrgaabriel.ayla.utils.commands.CommandCategory
import me.mrgaabriel.ayla.utils.commands.CommandContext
import me.mrgaabriel.ayla.utils.commands.annotations.Subcommand

class PingCommand : AbstractCommand("ping", CommandCategory.UTILS, "Verifica a conexão do bot com os servidores do Discord") {

    @Subcommand
    fun ping(context: CommandContext) {
        GlobalScope.launch {
            val start = System.currentTimeMillis()
            val message = context.sendMessageAsync("Calculando...")

            message.editMessage("${context.getAsMention()} **Pong!** \uD83C\uDFD3\nWebSocket: `${context.jda.ping}ms` - API: `${System.currentTimeMillis() - start}ms`").queue()
        }
    }
}