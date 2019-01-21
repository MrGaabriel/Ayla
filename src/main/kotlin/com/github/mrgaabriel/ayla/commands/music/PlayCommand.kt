package com.github.mrgaabriel.ayla.commands.music

import com.github.mrgaabriel.ayla.commands.*
import com.github.mrgaabriel.ayla.utils.annotation.InjectParameterType
import com.github.mrgaabriel.ayla.utils.annotation.ParameterType
import com.github.mrgaabriel.ayla.utils.extensions.ayla
import net.perfectdreams.commands.annotation.Subcommand

class PlayCommand : AylaCommand("play", "tocar") {

    override val description: String
        get() = "Reproduz uma música em um canal de voz"

    override val category: CommandCategory
        get() = CommandCategory.MUSIC

    override val usage: String
        get() = "música"

    @Subcommand
    suspend fun root(context: AylaCommandContext) {
        context.explain()
    }

    @Subcommand
    suspend fun play(context: AylaCommandContext, @InjectParameterType(ParameterType.ARGUMENT_LIST) music: String) {
        val channel = context.event.member.voiceState.channel
        if (channel == null) {
            context.reply("Você precisa estar em um canal de voz!")
            return
        }

        ayla.audioManager.loadAndPlay(context, context.args.joinToString(" "), channel)
    }
}