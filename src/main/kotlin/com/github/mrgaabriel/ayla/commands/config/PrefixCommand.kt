package com.github.mrgaabriel.ayla.commands.config

import com.github.mrgaabriel.ayla.commands.AbstractCommand
import com.github.mrgaabriel.ayla.commands.CommandContext
import com.github.mrgaabriel.ayla.dao.GuildConfig
import com.github.mrgaabriel.ayla.tables.GuildConfigs
import com.github.mrgaabriel.ayla.utils.extensions.ayla
import net.dv8tion.jda.core.Permission
import org.jetbrains.exposed.sql.transactions.transaction

class PrefixCommand : AbstractCommand("prefix") {

    override fun getDescription(): String {
        return "Mude o prefixo dos comandos do servidor"
    }

    override fun getMemberPermissions(): List<Permission> {
        return listOf(Permission.MANAGE_SERVER)
    }

    override suspend fun run(context: CommandContext) {
        if (context.args.isEmpty()) {
            context.explain()
            return
        }

        val config = transaction(ayla.database) {
            GuildConfig.find { GuildConfigs.id eq context.event.guild.id }.first()
        }

        val prefix = context.args[0]

        transaction(ayla.database) {
            config.prefix = prefix
        }

        context.sendMessage("${context.event.author.asMention} O prefixo dos comandos do servidor foi mudado para `$prefix`")
    }
}