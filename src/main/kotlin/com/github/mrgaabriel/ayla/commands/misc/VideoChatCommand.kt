package com.github.mrgaabriel.ayla.commands.misc

import com.github.mrgaabriel.ayla.commands.*
import com.github.mrgaabriel.ayla.utils.extensions.tag
import net.dv8tion.jda.core.EmbedBuilder
import net.perfectdreams.commands.annotation.Subcommand
import java.awt.Color
import java.time.OffsetDateTime

class VideoChatCommand : AylaCommand("videochat") {

    override val description: String
        get() = "Dá o link para o chat de vídeo em um canal de voz (experimental)"

    override val category: CommandCategory
        get() = CommandCategory.MISC

    @Subcommand
    suspend fun videochat(context: AylaCommandContext) {
        val channel = context.member.voiceState.channel

        if (channel == null) {
            context.reply("Você precisa estar em um canal de voz!")
            return
        }

        val link = "https://discordapp.com/channels/${context.guild.id}/${channel.id}"
        val builder = EmbedBuilder()

        builder.setDescription("Clique [aqui]($link) para entrar no chat de vídeo do canal `${channel.name}`, lembrando que você precisa estar nele antes.")
        builder.setColor(Color.BLUE)

        builder.setFooter(context.author.tag, context.author.effectiveAvatarUrl)
        builder.setTimestamp(OffsetDateTime.now())

        context.reply(builder.build())
    }
}