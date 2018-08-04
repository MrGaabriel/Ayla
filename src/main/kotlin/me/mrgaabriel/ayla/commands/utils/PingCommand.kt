package me.mrgaabriel.ayla.commands.utils

import me.mrgaabriel.ayla.commands.*

class PingCommand : AbstractCommand() {

    init {
        this.label = "ping"
    }

    override fun execute(context: CommandContext) {
        context.sendMessage(context.getAsMention(true) + "Pong! :ping_pong: `${context.message.jda.ping} ms` (Shard `${context.message.jda.shardInfo.shardId}/${context.message.jda.shardInfo.shardTotal}`)")
    }
}