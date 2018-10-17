package me.mrgaabriel.ayla.commands.discord

import me.mrgaabriel.ayla.utils.AylaUtils
import me.mrgaabriel.ayla.utils.ayla
import me.mrgaabriel.ayla.utils.commands.AbstractCommand
import me.mrgaabriel.ayla.utils.commands.CommandCategory
import me.mrgaabriel.ayla.utils.commands.CommandContext
import me.mrgaabriel.ayla.utils.commands.annotations.ArgumentType
import me.mrgaabriel.ayla.utils.commands.annotations.InjectArgument
import me.mrgaabriel.ayla.utils.commands.annotations.Subcommand
import me.mrgaabriel.ayla.utils.humanize
import me.mrgaabriel.ayla.utils.tag
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.User

class UserInfoCommand : AbstractCommand("userinfo", category = CommandCategory.DISCORD, description = "Veja as informações de um usuário do Discord") {

    @Subcommand
    fun userinfo(context: CommandContext, @InjectArgument(ArgumentType.USER) user: User?) {
        val realUser = user ?: context.user

        val builder = EmbedBuilder()

        builder.setColor(AylaUtils.randomColor())

        builder.setAuthor(realUser.tag, null, realUser.effectiveAvatarUrl)
        builder.setTitle("Usuário: ${realUser.tag}")

        builder.addField("ID do usuário", realUser.id, true)
        builder.addField("Servidores compartilhados (${ayla.shardManager.getMutualGuilds(realUser).size})", ayla.shardManager.getMutualGuilds(realUser).joinToString(", ", transform = {it.name}), true)
        builder.addField("Data de criação da conta", realUser.creationTime.humanize(), true)

        if (context.guild.isMember(realUser)) {
            val member = context.guild.getMember(realUser)

            if (member.game != null) {
                builder.addField("Jogando", member.game.name, true)
            }

            builder.addField("Data de entrada aqui", member.joinDate.humanize(), true)
            builder.addField("Status", member.onlineStatus.humanize(), true)
            builder.addField("Cargos (${member.roles.size})", member.roles.joinToString(" ", transform = {it.name}), true)
        }

        builder.setThumbnail(realUser.effectiveAvatarUrl)

        context.sendMessage(builder.build(), context.getAsMention())
    }
}