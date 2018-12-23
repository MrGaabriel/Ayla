package com.github.mrgaabriel.ayla.commands

import net.dv8tion.jda.core.Permission
import net.perfectdreams.commands.Command

open class AylaCommand(vararg allLabels: String): Command() {

    open val category = CommandCategory.NONE

    open val description = "Insira descrição do comando aqui"

    open val onlyOwner = false

    open val discordPermissions = listOf<Permission>()
    open val botPermissions = listOf<Permission>()

    open val cooldown = 2500.toLong()

    override val labels = allLabels
}