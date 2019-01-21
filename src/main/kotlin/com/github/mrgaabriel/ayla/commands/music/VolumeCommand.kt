package com.github.mrgaabriel.ayla.commands.music

import com.github.mrgaabriel.ayla.commands.*
import com.github.mrgaabriel.ayla.utils.extensions.ayla
import net.dv8tion.jda.core.Permission
import net.perfectdreams.commands.annotation.Subcommand

class VolumeCommand : AylaCommand("volume") {

    override val description: String
        get() = "Muda o volume da música que está sendo reproduzida atualmente"

    override val discordPermissions: List<Permission>
        get() = listOf(Permission.VOICE_MUTE_OTHERS)

    override val category: CommandCategory
        get() = CommandCategory.MUSIC

    override val usage: String
        get() = "volume"

    @Subcommand
    suspend fun root(context: AylaCommandContext) {
        context.explain()
    }

    @Subcommand
    suspend fun volume(context: AylaCommandContext, volume: Int) {
        val playing = ayla.audioManager.getAudioPlayer(context.event.guild).playingTrack

        if (playing == null) {
            context.reply("Nenhuma música está sendo reproduzida neste momento!")
            return
        }

        if (volume > 150) {
            context.reply("Você quer ficar surdo? Eu só posso colocar o volume até `150`!")
            return
        }

        ayla.audioManager.getAudioPlayer(context.event.guild).volume = volume
        context.reply("Volume da música mudado para `$volume`")
    }
}