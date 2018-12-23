package com.github.mrgaabriel.ayla.commands.developer

import com.github.mrgaabriel.ayla.commands.AylaCommand
import com.github.mrgaabriel.ayla.commands.AylaCommandContext
import com.github.mrgaabriel.ayla.commands.CommandCategory
import com.github.mrgaabriel.ayla.commands.CommandContext
import net.dv8tion.jda.core.entities.User
import net.perfectdreams.commands.annotation.Subcommand

// Comando criado para testar a API
class MagicCommand : AylaCommand("magic") {

    override val category: CommandCategory
        get() = CommandCategory.DEVELOPER

    override val onlyOwner: Boolean
        get() = true

    @Subcommand
    suspend fun root(context: AylaCommandContext) {
        context.reply("hello world!")
    }

    @Subcommand(["mentionuser"])
    suspend fun mentionUser(context: AylaCommandContext, user: User) {
        context.reply("o usuário é ${user.asMention}")
    }
}