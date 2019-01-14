package com.github.mrgaabriel.ayla.commands.config

import com.github.mrgaabriel.ayla.commands.*
import com.github.mrgaabriel.ayla.dao.GuildConfig
import com.github.mrgaabriel.ayla.tables.GuildConfigs
import com.github.mrgaabriel.ayla.utils.annotation.InjectParameterType
import com.github.mrgaabriel.ayla.utils.annotation.ParameterType
import com.github.mrgaabriel.ayla.utils.extensions.ayla
import net.dv8tion.jda.core.Permission
import net.perfectdreams.commands.annotation.Subcommand
import org.jetbrains.exposed.sql.transactions.transaction

class PrefixCommand : AylaCommand("prefix") {

    override val description: String
        get() = "Mude o prefixo dos comandos do servidor"

    override val discordPermissions: List<Permission>
        get() = listOf(Permission.MANAGE_SERVER)

    override val category: CommandCategory
        get() = CommandCategory.CONFIG

    @Subcommand
    suspend fun root(context: AylaCommandContext) {
        context.explain()
    }

    @Subcommand
    suspend fun prefix(context: AylaCommandContext, @InjectParameterType(ParameterType.ARGUMENT_LIST) prefix: String) {
        val config = transaction(ayla.database) {
            GuildConfig.find { GuildConfigs.id eq context.event.guild.id }.first()
        }

        transaction(ayla.database) {
            config.prefix = prefix
        }

        context.reply("O prefixo dos comandos do servidor foi mudado para `$prefix`")
    }
}