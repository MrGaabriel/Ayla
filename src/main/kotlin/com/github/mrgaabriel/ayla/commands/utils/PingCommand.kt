package com.github.mrgaabriel.ayla.commands.utils

import com.github.mrgaabriel.ayla.commands.AbstractCommand
import com.github.mrgaabriel.ayla.commands.CommandContext

class PingCommand : AbstractCommand("ping") {

    override suspend fun run(context: CommandContext) {
        context.sendMessage("${context.event.author.asMention} **Pong!** `${context.event.jda.ping}ms` :ping_pong:")
    }
}