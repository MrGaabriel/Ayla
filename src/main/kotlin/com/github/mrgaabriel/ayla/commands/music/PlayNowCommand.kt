package com.github.mrgaabriel.ayla.commands.music

import com.github.mrgaabriel.ayla.commands.AylaCommand
import com.github.mrgaabriel.ayla.commands.AylaCommandContext
import com.github.mrgaabriel.ayla.commands.CommandCategory
import com.github.mrgaabriel.ayla.utils.annotation.InjectParameterType
import com.github.mrgaabriel.ayla.utils.annotation.ParameterType
import com.github.mrgaabriel.ayla.utils.extensions.ayla
import net.dv8tion.jda.core.Permission
import net.perfectdreams.commands.annotation.Subcommand

class PlayNowCommand : AylaCommand("playnow", "tocaragora", "playskip") {

    override val description: String
        get() = "Reproduz uma música ignorando a fila e o que está tocando"

    override val category: CommandCategory
        get() = CommandCategory.MUSIC

    override val discordPermissions: List<Permission>
        get() = listOf(Permission.VOICE_MUTE_OTHERS)

    override val usage: String
        get() = "música"

    @Subcommand
    suspend fun root(context: AylaCommandContext) {
        context.explain()
    }

    @Subcommand
    suspend fun playnow(context: AylaCommandContext, @InjectParameterType(ParameterType.ARGUMENT_LIST) music: String) {
        val channel = context.event.member.voiceState.channel
        if (channel == null) {
            context.reply("Você precisa estar em um canal de voz!")
            return
        }

        ayla.audioManager.loadAndPlay(context, context.args.joinToString(" "), channel, true)
    }
}