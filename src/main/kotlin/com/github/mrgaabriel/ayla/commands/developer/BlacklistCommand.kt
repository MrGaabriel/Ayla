package com.github.mrgaabriel.ayla.commands.developer

import com.github.mrgaabriel.ayla.commands.AylaCommand
import com.github.mrgaabriel.ayla.commands.AylaCommandContext
import com.github.mrgaabriel.ayla.commands.CommandCategory
import com.github.mrgaabriel.ayla.commands.notNull
import com.github.mrgaabriel.ayla.dao.UserProfile
import com.github.mrgaabriel.ayla.tables.UserProfiles
import com.github.mrgaabriel.ayla.utils.annotation.InjectParameterType
import com.github.mrgaabriel.ayla.utils.annotation.ParameterType
import com.github.mrgaabriel.ayla.utils.extensions.ayla
import com.github.mrgaabriel.ayla.utils.extensions.tag
import net.dv8tion.jda.core.entities.User
import net.perfectdreams.commands.annotation.Subcommand
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.contracts.ExperimentalContracts

class BlacklistCommand : AylaCommand("blacklist") {

    override val category: CommandCategory
        get() = CommandCategory.DEVELOPER

    override val onlyOwner: Boolean
        get() = true

    @Subcommand
    suspend fun root(context: AylaCommandContext) {
        context.explain()
    }

    @Subcommand(["add"])
    @ExperimentalContracts
    suspend fun add(context: AylaCommandContext, user: User?, @InjectParameterType(ParameterType.ARGUMENT_LIST) reason: String?) {
        notNull(user, "Usuário inválido!")
        notNull(reason, "Me dê um motivo!")

        val profile = transaction(ayla.database) {
            UserProfile.find { UserProfiles.id eq user.id }.firstOrNull() ?: UserProfile.new(user.id) {}
        }

        transaction(ayla.database) {
            profile.blacklisted = true
            profile.blacklistReason = reason
        }

        context.reply("Usuário ${user.tag} (`${user.id}`) banido com sucesso pelo motivo `$reason`!")
    }

    @Subcommand(["remove"])
    @ExperimentalContracts
    suspend fun remove(context: AylaCommandContext, user: User?) {
        notNull(user, "Usuário inválido!")

        val profile = transaction(ayla.database) {
            UserProfile.find { UserProfiles.id eq user.id }.firstOrNull() ?: UserProfile.new(user.id) {}
        }

        transaction(ayla.database) {
            profile.blacklisted = false
            profile.blacklistReason = null
        }

        context.reply("Usuário ${user.tag} (`${user.id}`) desbanido com sucesso!")
    }

}