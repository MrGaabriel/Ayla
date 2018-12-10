package com.github.mrgaabriel.ayla.commands.music

import com.github.mrgaabriel.ayla.commands.AbstractCommand
import com.github.mrgaabriel.ayla.commands.CommandCategory
import com.github.mrgaabriel.ayla.commands.CommandContext
import com.github.mrgaabriel.ayla.utils.extensions.ayla
import net.dv8tion.jda.core.Permission

class VolumeCommand : AbstractCommand("volume", category = CommandCategory.MUSIC) {

    override fun getDescription(): String {
        return "Muda o volume da música que está sendo reproduzida"
    }

    override fun getMemberPermissions(): List<Permission> {
        return listOf(Permission.VOICE_MOVE_OTHERS)
    }

    override fun getUsage(): String {
        return "volume"
    }

    override suspend fun run(context: CommandContext) {
        if (context.args.isEmpty()) {
            context.explain()
            return
        }

        val playing = ayla.audioManager.getAudioPlayer(context.event.guild).playingTrack

        if (playing == null) {
            context.sendMessage("${context.event.author.asMention} Nenhuma música está sendo reproduzida neste momento!")
            return
        }

        val volume = context.args[0].toIntOrNull()
        if (volume == null) {
            context.explain()
            return
        }

        if (volume > 150) {
            context.sendMessage("${context.event.author.asMention} Você quer ficar surdo? Eu só posso colocar o volume até `150`!!")
            return
        }

        ayla.audioManager.getAudioPlayer(context.event.guild).volume = volume
        context.sendMessage("${context.event.author.asMention} Volume da música mudado para `$volume`")
    }
}