package me.mrgaabriel.ayla.commands.music

import me.mrgaabriel.ayla.utils.ayla
import me.mrgaabriel.ayla.utils.commands.AbstractCommand
import me.mrgaabriel.ayla.utils.commands.CommandCategory
import me.mrgaabriel.ayla.utils.commands.CommandContext
import me.mrgaabriel.ayla.utils.commands.annotations.Subcommand
import me.mrgaabriel.ayla.utils.commands.annotations.SubcommandPermissions
import net.dv8tion.jda.core.Permission

class PauseCommand : AbstractCommand("pause", aliases = listOf("pausar"), category = CommandCategory.MUSIC, description = "Pause a música que está tocando") {

    @Subcommand
    @SubcommandPermissions([Permission.VOICE_MOVE_OTHERS])
    fun pause(context: CommandContext) {
        val player = ayla.audioManager.getPlayer(context.guild)

        if (player.playingTrack == null) {
            context.sendMessage(context.getAsMention(true) + "Não há nenhuma música tocando!")
            return
        }

        player.isPaused = true
        context.sendMessage(context.getAsMention(true) + "Música pausada!")
    }
}