package com.github.mrgaabriel.ayla.commands

import net.perfectdreams.commands.Command

open class AylaCommand(vararg allLabels: String) : Command() {

    override val labels = allLabels
}