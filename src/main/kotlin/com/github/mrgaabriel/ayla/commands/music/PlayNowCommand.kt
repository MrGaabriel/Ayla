package com.github.mrgaabriel.ayla.commands.music

import com.github.mrgaabriel.ayla.commands.AbstractCommand
import com.github.mrgaabriel.ayla.commands.CommandContext
import com.github.mrgaabriel.ayla.utils.extensions.ayla
import net.dv8tion.jda.core.Permission

class PlayNowCommand : AbstractCommand("playnow", listOf("tocaragora", "playskip")) {

    override fun getDescription(): String {
        return "Reproduz uma música ignorando a fila e a que está tocando"
    }

    override fun getMemberPermissions(): List<Permission> {
        return listOf(Permission.VOICE_MOVE_OTHERS)
    }

    override fun getUsage(): String {
        return "música"
    }

    override suspend fun run(context: CommandContext) {
        if (context.args.isEmpty()) {
            context.explain()
            return
        }

        val channel = context.event.member.voiceState.channel
        if (channel == null) {
            context.sendMessage("${context.event.author.asMention} Você precisa estar em um canal de voz!")
            return
        }

        ayla.audioManager.loadAndPlay(context, context.args.joinToString(" "), channel, true)
    }
}