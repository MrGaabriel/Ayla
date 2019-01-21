package com.github.mrgaabriel.ayla.commands.utils

import com.github.mrgaabriel.ayla.commands.AylaCommand
import com.github.mrgaabriel.ayla.commands.CommandContext
import com.github.mrgaabriel.ayla.utils.extensions.await
import net.perfectdreams.commands.annotation.Subcommand

class PingCommand : AylaCommand("ping") {

    @Subcommand
    suspend fun root(context: CommandContext) {
        context.reply("**Pong!** \uD83C\uDFD3\n**Gateway Ping:** `${context.event.jda.ping}ms`")
    }
}