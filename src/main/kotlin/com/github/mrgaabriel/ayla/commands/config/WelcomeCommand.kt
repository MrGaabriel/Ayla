package com.github.mrgaabriel.ayla.commands.config

import com.github.mrgaabriel.ayla.commands.AylaCommand
import com.github.mrgaabriel.ayla.commands.AylaCommandContext
import com.github.mrgaabriel.ayla.commands.CommandContext
import com.github.mrgaabriel.ayla.commands.notNull
import com.github.mrgaabriel.ayla.dao.GuildConfig
import com.github.mrgaabriel.ayla.tables.GuildConfigs
import com.github.mrgaabriel.ayla.utils.AylaUtils
import com.github.mrgaabriel.ayla.utils.extensions.ayla
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.TextChannel
import net.perfectdreams.commands.annotation.Subcommand
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.contracts.ExperimentalContracts

class WelcomeCommand : AylaCommand("welcome") {

    override val description: String
        get() = "Ative ou desative o módulo de boas vindas"

    override val usage: String
        get() = "(channel|off)"

    override val discordPermissions: List<Permission>
        get() = listOf(Permission.MANAGE_SERVER)

    @Subcommand
    suspend fun root(context: AylaCommandContext) {
        context.explain()
    }

    @Subcommand
    @ExperimentalContracts
    suspend fun welcome(context: AylaCommandContext, channel: TextChannel?) {
        notNull(channel, "Canal não encontrado!")

        val config = transaction(ayla.database) {
            GuildConfig.find { GuildConfigs.id eq context.event.guild.id }.first()
        }

        transaction(ayla.database) {
            config.welcomeEnabled = true
            config.welcomeChannelId = channel.id
        }

        context.reply("Módulo de boas vindas ativado no canal ${channel.asMention}!")
    }

    @Subcommand(["off"])
    suspend fun off(context: AylaCommandContext) {
        val config = transaction(ayla.database) {
            GuildConfig.find { GuildConfigs.id eq context.event.guild.id }.first()
        }

        transaction(ayla.database) {
            config.welcomeEnabled = false
            config.welcomeChannelId = null
        }

        context.reply("Móduo de boas vindas desativado!")
    }
}