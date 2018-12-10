package com.github.mrgaabriel.ayla.commands.misc

import com.github.mrgaabriel.ayla.commands.AbstractCommand
import com.github.mrgaabriel.ayla.commands.CommandCategory
import com.github.mrgaabriel.ayla.commands.CommandContext
import com.github.mrgaabriel.ayla.utils.extensions.tag
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color
import java.time.OffsetDateTime

class VideoChatCommand : AbstractCommand("videochat", category = CommandCategory.MISC) {

    override fun getDescription(): String {
        return "Experimental"
    }

    override suspend fun run(context: CommandContext) {
        val channel = context.event.member.voiceState.channel

        if (channel == null) {
            context.sendMessage("${context.event.author.asMention} Você precisa estar em um canal de voz!")
            return
        }

        val link = "https://discordapp.com/channels/${context.event.guild.id}/${channel.id}"
        val builder = EmbedBuilder()

        builder.setDescription("Clique [aqui]($link) para entrar no chat de vídeo do canal `${channel.name}`, lembrando que você precisa estar nele antes.")
        builder.setColor(Color.BLUE)

        builder.setFooter(context.event.author.tag, null)
        builder.setTimestamp(OffsetDateTime.now())

        context.sendMessage(builder.build())
    }
}