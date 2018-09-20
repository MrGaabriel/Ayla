package me.mrgaabriel.ayla.commands.music

import me.mrgaabriel.ayla.utils.ayla
import me.mrgaabriel.ayla.utils.commands.AbstractCommand
import me.mrgaabriel.ayla.utils.commands.CommandCategory
import me.mrgaabriel.ayla.utils.commands.CommandContext
import me.mrgaabriel.ayla.utils.commands.annotations.ArgumentType
import me.mrgaabriel.ayla.utils.commands.annotations.InjectArgument
import me.mrgaabriel.ayla.utils.commands.annotations.Subcommand

class PlayCommand : AbstractCommand("play", aliases = listOf("tocar"), description = "Toca uma música em algum canal", category = CommandCategory.MUSIC) {

    @Subcommand
    fun play(context: CommandContext, @InjectArgument(ArgumentType.ARGUMENT_LIST) music: String) {
        if (context.args.isEmpty()) {
            context.explain()
            return
        }

        val member = context.guild.getMember(context.user)

        if (member.voiceState.channel == null) {
            context.sendMessage(context.getAsMention(true) + "Você não está em nenhum canal de voz!")
            return
        }

        val player = ayla.audioManager.getPlayer(context.guild)

        if (member.voiceState.channel != context.guild.selfMember.voiceState.channel && player.playingTrack != null) {
            context.sendMessage(context.getAsMention(true) + "Você não está no mesmo canal que o bot!")
            return
        }

        ayla.audioManager.loadAndPlay(context, music, member.voiceState.channel)
    }
}