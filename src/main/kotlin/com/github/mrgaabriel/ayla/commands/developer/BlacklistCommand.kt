package com.github.mrgaabriel.ayla.commands.developer

import com.github.mrgaabriel.ayla.commands.AbstractCommand
import com.github.mrgaabriel.ayla.commands.CommandContext
import com.github.mrgaabriel.ayla.dao.UserProfile
import com.github.mrgaabriel.ayla.tables.UserProfiles
import com.github.mrgaabriel.ayla.utils.extensions.ayla
import org.jetbrains.exposed.sql.transactions.transaction

class BlacklistCommand : AbstractCommand("blacklist") {

    override fun onlyOwner(): Boolean {
        return true
    }

    override suspend fun run(context: CommandContext) {
        if (context.args[0] == "remove") {
            val userId = context.args[1]

            val profile = transaction(ayla.database) {
                UserProfile.find { UserProfiles.id eq userId }.first()
            }

            transaction(ayla.database) {
                profile.blacklisted = false
                profile.blacklistReason = null
            }

            context.sendMessage("${context.event.author.asMention} Usuário desbanido com sucesso!")
            return
        }

        val userId = context.args[0]
        val reason = context.args.apply { removeAt(0) }.joinToString(" ")

        val profile = transaction(ayla.database) {
            UserProfile.find { UserProfiles.id eq userId }.first()
        }

        transaction(ayla.database) {
            profile.blacklisted = true
            profile.blacklistReason = reason
        }

        context.sendMessage("${context.event.author.asMention} Usuário banido com sucesso!")
    }
}