package com.github.mrgaabriel.ayla.commands.music

import com.github.mrgaabriel.ayla.commands.AbstractCommand
import com.github.mrgaabriel.ayla.commands.CommandContext
import com.github.mrgaabriel.ayla.utils.extensions.ayla

class PlayCommand : AbstractCommand("play", listOf("tocar")) {

    override fun getDescription(): String {
        return "Reproduz uma música em um canal de voz"
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

        ayla.audioManager.loadAndPlay(context, context.args.joinToString(" "), channel)
    }
}