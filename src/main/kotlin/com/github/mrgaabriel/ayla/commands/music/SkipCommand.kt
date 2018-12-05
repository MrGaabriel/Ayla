package com.github.mrgaabriel.ayla.commands.music

import com.github.mrgaabriel.ayla.commands.AbstractCommand
import com.github.mrgaabriel.ayla.commands.CommandContext
import com.github.mrgaabriel.ayla.utils.extensions.ayla
import net.dv8tion.jda.core.Permission

class SkipCommand : AbstractCommand("skip", listOf("pular")) {

    override fun getDescription(): String {
        return "Pula a música que está reproduzindo"
    }

    override fun getMemberPermissions(): List<Permission> {
        return listOf(Permission.VOICE_MOVE_OTHERS)
    }

    override suspend fun run(context: CommandContext) {
        val playing = ayla.audioManager.getAudioPlayer(context.event.guild).playingTrack

        if (playing == null) {
            context.sendMessage("${context.event.author.asMention} Nenhuma música está sendo reproduzida neste momento!")
            return
        }

        ayla.audioManager.getMusicPlayer(context.event.guild).scheduler.nextTrack()
        context.sendMessage("${context.event.author.asMention} Música pulada!")
    }
}