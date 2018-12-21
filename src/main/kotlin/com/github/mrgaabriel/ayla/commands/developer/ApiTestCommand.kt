package com.github.mrgaabriel.ayla.commands.developer

import com.github.mrgaabriel.ayla.commands.AylaCommand
import com.github.mrgaabriel.ayla.commands.CommandContext
import net.dv8tion.jda.core.entities.User
import net.perfectdreams.commands.annotation.Subcommand

class ApiTestCommand : AylaCommand("apitest", "testando") {

    @Subcommand
    suspend fun apiTest(context: CommandContext, user: User? = null) {
        if (user == null) {
            context.reply("eae men, é só um comando para testar mesmo")
            return
        }

        context.reply("o usuário é ${user.asMention}")
    }
}