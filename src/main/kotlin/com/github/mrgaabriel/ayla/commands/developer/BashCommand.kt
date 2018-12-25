package com.github.mrgaabriel.ayla.commands.developer

import com.github.mrgaabriel.ayla.commands.AylaCommand
import com.github.mrgaabriel.ayla.commands.AylaCommandContext
import com.github.mrgaabriel.ayla.commands.CommandCategory
import com.github.mrgaabriel.ayla.commands.notNull
import com.github.mrgaabriel.ayla.utils.annotation.InjectParameterType
import com.github.mrgaabriel.ayla.utils.annotation.ParameterType
import net.perfectdreams.commands.annotation.Subcommand
import kotlin.contracts.ExperimentalContracts

class BashCommand : AylaCommand("bash") {

    override val category: CommandCategory
        get() = CommandCategory.DEVELOPER

    override val onlyOwner: Boolean
        get() = true

    @Subcommand
    @ExperimentalContracts
    suspend fun bash(context: AylaCommandContext, @InjectParameterType(ParameterType.ARGUMENT_LIST) command: String?) {
        notNull(command, "Me dê um comando para ser executado!")

        val message = context.reply("Executando...")

        val runtime = Runtime.getRuntime()
        val process = runtime.exec(command).apply { waitFor() }

        process.inputStream.use {
            message.editMessage("```\u200B${it.reader().readText().replace("`", "")}```").queue()
        }
    }

}