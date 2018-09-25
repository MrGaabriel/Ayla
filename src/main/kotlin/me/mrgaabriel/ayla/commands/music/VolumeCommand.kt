package me.mrgaabriel.ayla.commands.music

import me.mrgaabriel.ayla.utils.ayla
import me.mrgaabriel.ayla.utils.commands.AbstractCommand
import me.mrgaabriel.ayla.utils.commands.CommandCategory
import me.mrgaabriel.ayla.utils.commands.CommandContext
import me.mrgaabriel.ayla.utils.commands.annotations.Subcommand
import me.mrgaabriel.ayla.utils.commands.annotations.SubcommandPermissions
import net.dv8tion.jda.core.Permission

class VolumeCommand : AbstractCommand("volume", category = CommandCategory.MUSIC, description = "Muda o volume da música que está tocando") {

    @Subcommand
    @SubcommandPermissions([Permission.VOICE_MOVE_OTHERS])
    fun volume(context: CommandContext, volume: Int?) {
        val player = ayla.audioManager.getAudioPlayer(context.guild)

        if (player.playingTrack == null) {
            context.sendMessage(context.getAsMention(true) + "Não há nenhuma música tocando no momento!")
            return
        }

        if (volume == null) {
            context.explain()
            return
        }

        if (volume !in 0..150) {
            context.sendMessage(context.getAsMention(true) + "Você quer ficar surdo? O limite de volume é 150.")
            return
        }

        player.volume = volume
        context.sendMessage(context.getAsMention(true) + "Volume mudado para `$volume`!")
    }
}