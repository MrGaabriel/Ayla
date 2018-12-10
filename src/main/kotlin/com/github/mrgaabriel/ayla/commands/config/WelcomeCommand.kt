package com.github.mrgaabriel.ayla.commands.config

import com.github.mrgaabriel.ayla.commands.AbstractCommand
import com.github.mrgaabriel.ayla.commands.CommandCategory
import com.github.mrgaabriel.ayla.commands.CommandContext
import com.github.mrgaabriel.ayla.dao.GuildConfig
import com.github.mrgaabriel.ayla.tables.GuildConfigs
import com.github.mrgaabriel.ayla.utils.AylaUtils
import com.github.mrgaabriel.ayla.utils.extensions.ayla
import net.dv8tion.jda.core.Permission
import org.jetbrains.exposed.sql.transactions.transaction

class WelcomeCommand : AbstractCommand("welcome", category = CommandCategory.CONFIG) {

    override fun getMemberPermissions(): List<Permission> {
        return listOf(Permission.MANAGE_SERVER)
    }

    override fun getDescription(): String {
        return "Ative ou desative o módulo de boas vindas"
    }

    override fun getUsage(): String {
        return "#canal"
    }

    override suspend fun run(context: CommandContext) {
        if (context.args.isEmpty()) {
            context.explain()
            return
        }

        val config = transaction(ayla.database) {
            GuildConfig.find { GuildConfigs.id eq context.event.guild.id }.first()
        }

        if (context.args[0] == "off") {
            transaction(ayla.database) {
                config.welcomeEnabled = false
                config.welcomeChannelId = null
            }

            context.sendMessage("${context.event.author.asMention} Módulo de boas vindas desativado!")
            return
        }

        val channel = AylaUtils.getTextChannel(context.args[0], context.event.guild)

        if (channel == null) {
            context.sendMessage("${context.event.author.asMention} Canal não encontrado!")
            return
        }


        transaction(ayla.database) {
            config.welcomeEnabled = true
            config.welcomeChannelId = channel.id
        }

        context.sendMessage("${context.event.author.asMention} Módulo de boas vindas ativado no canal ${channel.asMention}!")
    }
}