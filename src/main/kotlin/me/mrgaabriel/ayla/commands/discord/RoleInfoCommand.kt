package me.mrgaabriel.ayla.commands.discord

import me.mrgaabriel.ayla.utils.commands.AbstractCommand
import me.mrgaabriel.ayla.utils.commands.CommandCategory
import me.mrgaabriel.ayla.utils.commands.CommandContext
import me.mrgaabriel.ayla.utils.commands.annotations.Subcommand
import me.mrgaabriel.ayla.utils.isValidSnowflake
import me.mrgaabriel.ayla.utils.tag
import net.dv8tion.jda.core.EmbedBuilder

class RoleInfoCommand : AbstractCommand("roleinfo", category = CommandCategory.DISCORD) {

    @Subcommand
    fun roleInfo(context: CommandContext, input: String?) {
        if (input == null) {
            context.explain()
            return
        }

        val role = context.guild.getRolesByName(input, true).firstOrNull()
            ?: if (input.isValidSnowflake()) context.guild.getRoleById(input) else null
            ?: return context.sendMessage(context.getAsMention() + "Cargo não encontrado! Tente colocar o nome de algum cargo ou o ID")

        val builder = EmbedBuilder()

        builder.setColor(role.color)

        builder.setAuthor("${role.name} - ${role.id}")

        builder.addField("ID do cargo", role.id, true)

        val members = context.guild.getMembersWithRoles(role)
        builder.addField("Membros com este cargo (${members.size})", "```${members.joinToString(", ", transform = {it.user.tag})}```", true)
        builder.addField("Cor (decimal)", "${role.colorRaw}", true)
        builder.addField("Permissões", "```${role.permissions.joinToString(", ", transform = {it.name})}```", true)

        context.sendMessage(builder.build(), context.getAsMention())
    }
}