package com.github.mrgaabriel.ayla.commands

import com.github.mrgaabriel.ayla.utils.exceptions.CommandException
import net.dv8tion.jda.core.Permission
import net.perfectdreams.commands.Command
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

open class AylaCommand(vararg allLabels: String): Command() {

    open val category = CommandCategory.NONE

    open val description = "Insira descrição do comando aqui"

    open val onlyOwner = false

    open val discordPermissions = listOf<Permission>()
    open val botPermissions = listOf<Permission>()

    open val cooldown = 2500.toLong()

    open val canHandle: ((AylaCommandContext) -> Boolean)? = null

    override val labels = allLabels
}

@ExperimentalContracts
fun <T> AylaCommand.notNull(value: T?, message: String) { // Contracts precisam ser fora de uma classe, então...
    contract {
        returns() implies (value != null)
    }
    if(value == null) throw CommandException(message)
}