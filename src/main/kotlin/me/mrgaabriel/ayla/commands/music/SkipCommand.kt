package me.mrgaabriel.ayla.commands.music

import me.mrgaabriel.ayla.utils.ayla
import me.mrgaabriel.ayla.utils.commands.AbstractCommand
import me.mrgaabriel.ayla.utils.commands.CommandCategory
import me.mrgaabriel.ayla.utils.commands.CommandContext
import me.mrgaabriel.ayla.utils.commands.annotations.Subcommand
import me.mrgaabriel.ayla.utils.commands.annotations.SubcommandPermissions
import net.dv8tion.jda.core.Permission

class SkipCommand : AbstractCommand("skip", description = "Pula a música que está tocando", aliases = listOf("pular"), category = CommandCategory.MUSIC) {

    @Subcommand
    @SubcommandPermissions([Permission.VOICE_MOVE_OTHERS])
    fun skip(context: CommandContext) {
        val player = ayla.audioManager.getPlayer(context.guild)

        if (player.playingTrack == null) {
            context.sendMessage(context.getAsMention(true) + "Não há nenhuma música tocando!")
            return
        }

        ayla.audioManager.getMusicPlayer(context.guild).scheduler.nextTrack()
        context.sendMessage(context.getAsMention(true) + "Música pulada!")
    }
}