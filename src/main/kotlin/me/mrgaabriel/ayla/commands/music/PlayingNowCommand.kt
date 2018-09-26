package me.mrgaabriel.ayla.commands.music

import me.mrgaabriel.ayla.utils.AylaUtils
import me.mrgaabriel.ayla.utils.ayla
import me.mrgaabriel.ayla.utils.commands.AbstractCommand
import me.mrgaabriel.ayla.utils.commands.CommandCategory
import me.mrgaabriel.ayla.utils.commands.CommandContext
import me.mrgaabriel.ayla.utils.commands.annotations.Subcommand
import net.dv8tion.jda.core.EmbedBuilder

class PlayingNowCommand : AbstractCommand("playingnow", aliases = listOf("np", "nowplaying", "tocando", "playing"), category = CommandCategory.MUSIC, description = "Veja as informações da música que está tocando") {

    @Subcommand
    fun playingNow(context: CommandContext) {
        val player = ayla.audioManager.getAudioPlayer(context.guild)

        if (player.playingTrack == null) {
            context.sendMessage(context.getAsMention(true) + "Não há nenhuma música tocando!")
            return
        }

        val track = player.playingTrack
        val trackInfo = track.info

        val builder = EmbedBuilder()

        builder.setTitle(":notes: ${trackInfo.title} de ${trackInfo.author}")
        builder.addField("URL", "__[Clique aqui](${trackInfo.uri})__", true)

        builder.setColor(AylaUtils.randomColor())

        context.sendMessage(builder.build(), context.getAsMention())
    }
}