package me.mrgaabriel.ayla.utils.commands.annotations

import net.dv8tion.jda.core.Permission

annotation class SubcommandPermissions(
        val permissions: Array<Permission>,
        val onlyOwner: Boolean = false,
        val botPermissions: Array<Permission> = arrayOf()
)