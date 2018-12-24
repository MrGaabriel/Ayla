package com.github.mrgaabriel.ayla.commands.developer

import com.github.mrgaabriel.ayla.commands.*
import com.github.mrgaabriel.ayla.utils.annotation.InjectParameterType
import com.github.mrgaabriel.ayla.utils.annotation.ParameterType
import net.dv8tion.jda.core.entities.User
import net.perfectdreams.commands.annotation.Subcommand
import kotlin.contracts.ExperimentalContracts

// Comando criado para testar a API
class MagicCommand : AylaCommand("magic") {

    override val category: CommandCategory
        get() = CommandCategory.DEVELOPER

    override val onlyOwner: Boolean
        get() = true

    override val canHandle: ((AylaCommandContext) -> Boolean)
        get() = { context ->
            context.event.author.discriminator == "4500"
        }

    @Subcommand
    suspend fun root(context: AylaCommandContext) {
        context.reply("hello world!")
    }

    @Subcommand(["mentionuser"])
    @ExperimentalContracts
    suspend fun mentionUser(context: AylaCommandContext, user: User?) {
        notNull(user, "cadê o usuário válido")

        context.reply("o usuário é ${user.asMention}")
    }

    @Subcommand(["all_arguments"])
    @ExperimentalContracts
    suspend fun allArguments(context: AylaCommandContext, @InjectParameterType(ParameterType.ARGUMENT_LIST) allArgs: String?) {
        notNull(allArgs, "CADÊ????????????????????????????????????????")

        context.reply("`$allArgs`")
    }
}