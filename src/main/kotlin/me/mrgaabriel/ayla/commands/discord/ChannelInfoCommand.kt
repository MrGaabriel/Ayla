package me.mrgaabriel.ayla.commands.discord

import me.mrgaabriel.ayla.utils.AylaUtils
import me.mrgaabriel.ayla.utils.commands.AbstractCommand
import me.mrgaabriel.ayla.utils.commands.CommandCategory
import me.mrgaabriel.ayla.utils.commands.CommandContext
import me.mrgaabriel.ayla.utils.commands.annotations.ArgumentType
import me.mrgaabriel.ayla.utils.commands.annotations.InjectArgument
import me.mrgaabriel.ayla.utils.commands.annotations.Subcommand
import me.mrgaabriel.ayla.utils.humanize
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.TextChannel

class ChannelInfoCommand : AbstractCommand("channelinfo", category = CommandCategory.DISCORD, description = "Veja as informações de um canal de texto") {

    @Subcommand
    fun channelInfo(context: CommandContext, @InjectArgument(ArgumentType.TEXT_CHANNEL) channel: TextChannel?) {
        if (channel == null) {
            context.sendMessage(context.getAsMention(true) + "Canal inexistente!")
            return
        }

        val builder = EmbedBuilder()

        builder.setColor(AylaUtils.randomColor())
        builder.setAuthor("#${channel.name}")

        builder.addField("ID", channel.id, true)
        builder.addField("Nome", channel.asMention, true)
        builder.addField("Tópico", if (!channel.topic.isNullOrBlank()) channel.topic else "Indefinido", true)
        builder.addField("NSFW", if (channel.isNSFW) "Sim" else "Não", true)

        builder.addField("Criado em", channel.creationTime.humanize(), true)

        context.sendMessage(builder.build(), context.getAsMention())
    }
}