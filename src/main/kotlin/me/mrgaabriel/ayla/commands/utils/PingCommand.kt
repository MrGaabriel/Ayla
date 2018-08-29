package me.mrgaabriel.ayla.commands.utils

import me.mrgaabriel.ayla.utils.commands.*
import me.mrgaabriel.ayla.utils.commands.annotations.*

class PingCommand : AbstractCommand(
        "ping",
        CommandCategory.UTILS,
        "Verifica a conexão do bot com os servidores do Discord"
) {

    @Subcommand
    fun onExecute(context: CommandContext) {
        context.sendMessage(context.getAsMention(true) + "Pong! :ping_pong: `${context.message.jda.ping} ms` (Shard `${context.message.jda.shardInfo.shardId}/${context.message.jda.shardInfo.shardTotal}`)")
    }
}