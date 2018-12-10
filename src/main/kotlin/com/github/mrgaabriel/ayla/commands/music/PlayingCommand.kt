package com.github.mrgaabriel.ayla.commands.music

import com.github.mrgaabriel.ayla.commands.AbstractCommand
import com.github.mrgaabriel.ayla.commands.CommandCategory
import com.github.mrgaabriel.ayla.commands.CommandContext
import com.github.mrgaabriel.ayla.utils.AylaUtils
import com.github.mrgaabriel.ayla.utils.Constants
import com.github.mrgaabriel.ayla.utils.extensions.ayla
import net.dv8tion.jda.core.EmbedBuilder
import java.time.OffsetDateTime

class PlayingCommand : AbstractCommand("playing", listOf("playingnow", "np", "tocando", "tocandoagora"), category = CommandCategory.MUSIC) {

    override fun getDescription(): String {
        return "Vê as informações da música que está tocando"
    }

    override suspend fun run(context: CommandContext) {
        val playing = ayla.audioManager.getAudioPlayer(context.event.guild).playingTrack

        if (playing == null) {
            context.sendMessage("${context.event.author.asMention} Nenhuma música está sendo reproduzida neste momento!")
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

        context.sendMessage(builder.build(), context.event.author.asMention)
    }

}