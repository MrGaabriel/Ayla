package com.github.mrgaabriel.ayla.commands.utils

import com.github.mrgaabriel.ayla.commands.AbstractCommand
import com.github.mrgaabriel.ayla.commands.CommandContext
import com.github.mrgaabriel.ayla.utils.extensions.await

class PingCommand : AbstractCommand("ping") {

    override suspend fun run(context: CommandContext) {
        val start = System.currentTimeMillis()
        val message =
            context.sendMessage("${context.event.author.asMention} **Pong!** :ping_pong:\nGateway Ping: `${context.event.jda.ping}ms`")

        message.editMessage("${context.event.author.asMention} **Pong!** :ping_pong:\nGateway Ping: `${context.event.jda.ping}ms`\nAPI Ping: `${System.currentTimeMillis() - start}ms`")
            .await()
    }
}