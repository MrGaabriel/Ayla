package me.mrgaabriel.ayla.commands.discord

import me.mrgaabriel.ayla.utils.AylaUtils
import me.mrgaabriel.ayla.utils.ayla
import me.mrgaabriel.ayla.utils.commands.AbstractCommand
import me.mrgaabriel.ayla.utils.commands.CommandCategory
import me.mrgaabriel.ayla.utils.commands.CommandContext
import me.mrgaabriel.ayla.utils.commands.annotations.Subcommand
import me.mrgaabriel.ayla.utils.humanize
import me.mrgaabriel.ayla.utils.isValidSnowflake
import net.dv8tion.jda.core.EmbedBuilder

class ServerInfoCommand : AbstractCommand("serverinfo", category = CommandCategory.DISCORD, description = "Veja as informações de um servidor do Discord", aliases = listOf("guildinfo")) {

    @Subcommand
    fun serverInfo(context: CommandContext, input: String?) {
        val guild = if (input != null) {
            if (input.isValidSnowflake())
                ayla.shardManager.getGuildById(input) ?: context.guild
            else
                context.guild
        } else {
            context.guild
        }

        val builder = EmbedBuilder()

        builder.setAuthor(guild.name, null, guild.iconUrl)
        builder.setTitle("Servidor: ${guild.name}")

        builder.setColor(AylaUtils.randomColor())

        builder.addField("Criado em", guild.creationTime.humanize(), true)
        builder.addField("Entrei em", guild.selfMember.joinDate.humanize(), true)
        builder.addField("Membros", "${guild.members.size}", true)
        builder.addField("Dono",  guild.owner.asMention, true)

        context.sendMessage(builder.build(), context.getAsMention())
    }
}