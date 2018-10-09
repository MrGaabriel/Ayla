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
        val realChannel = channel ?: context.channel

        val builder = EmbedBuilder()

        builder.setColor(AylaUtils.randomColor())
        builder.setAuthor("#${realChannel.name}")

        builder.addField("ID", realChannel.id, true)
        builder.addField("Nome", realChannel.asMention, true)
        builder.addField("Tópico", if (!realChannel.topic.isNullOrBlank()) realChannel.topic else "Indefinido", true)
        builder.addField("NSFW", if (realChannel.isNSFW) "Sim" else "Não", true)

        builder.addField("Criado em", realChannel.creationTime.humanize(), true)

        context.sendMessage(builder.build(), context.getAsMention())
    }
}