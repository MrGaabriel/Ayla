package com.github.mrgaabriel.ayla.commands.music

import com.github.mrgaabriel.ayla.commands.AylaCommand
import com.github.mrgaabriel.ayla.commands.AylaCommandContext
import com.github.mrgaabriel.ayla.commands.CommandCategory
import com.github.mrgaabriel.ayla.commands.CommandContext
import com.github.mrgaabriel.ayla.utils.AylaUtils
import com.github.mrgaabriel.ayla.utils.Constants
import com.github.mrgaabriel.ayla.utils.extensions.ayla
import net.dv8tion.jda.core.EmbedBuilder
import net.perfectdreams.commands.annotation.Subcommand
import java.time.OffsetDateTime

class PlayingCommand : AylaCommand("playing", "playingnow", "np", "tocando", "tocandoagora") {

    override val description: String
        get() = "Vê as informações da música que está tocando atualmente"

    override val category: CommandCategory
        get() = CommandCategory.MUSIC

    @Subcommand
    suspend fun playing(context: AylaCommandContext) {
        val playing = ayla.audioManager.getAudioPlayer(context.event.guild).playingTrack

        if (playing == null) {
            context.reply("Nenhuma música está sendo reproduzida neste momento!")
            return
        }

        val info = playing.info

        val builder = EmbedBuilder()

        builder.setTitle("\uD83C\uDFB6 ${info.title}")
        builder.setDescription("""
            **Autor:** ${info.author}
            **Duração:** `${AylaUtils.getTimestamp(playing.position)}/${AylaUtils.getTimestamp(playing.duration)}`

            **Link:** [clique aqui](${info.uri})
        """.trimIndent())
        builder.setImage("http://i3.ytimg.com/vi/${info.identifier}/maxresdefault.jpg")

        builder.setFooter("${playing.state}", null)
        builder.setTimestamp(OffsetDateTime.now())

        builder.setColor(Constants.REDDIT_ORANGE_RED)

        context.reply(builder.build())
    }
}