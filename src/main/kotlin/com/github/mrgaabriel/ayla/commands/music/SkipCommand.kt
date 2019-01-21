package com.github.mrgaabriel.ayla.commands.music

import com.github.mrgaabriel.ayla.commands.*
import com.github.mrgaabriel.ayla.utils.extensions.ayla
import net.dv8tion.jda.core.Permission
import net.perfectdreams.commands.annotation.Subcommand

class SkipCommand : AylaCommand("skip", "pular") {

    override val description: String
        get() = "Pula a música que está reproduzindo atualmente"

    override val category: CommandCategory
        get() = CommandCategory.MUSIC

    override val discordPermissions: List<Permission>
        get() = listOf(Permission.VOICE_MUTE_OTHERS)

    @Subcommand
    suspend fun skip(context: AylaCommandContext) {
        val playing = ayla.audioManager.getAudioPlayer(context.event.guild).playingTrack

        if (playing == null) {
            context.reply("Nenhuma música está sendo reproduzida neste momento!")
            return
        }

        ayla.audioManager.getMusicPlayer(context.event.guild).scheduler.nextTrack()
        context.reply("Música pulada!")
    }
}