package me.mrgaabriel.ayla.commands.music

import me.mrgaabriel.ayla.utils.ayla
import me.mrgaabriel.ayla.utils.commands.AbstractCommand
import me.mrgaabriel.ayla.utils.commands.CommandCategory
import me.mrgaabriel.ayla.utils.commands.CommandContext
import me.mrgaabriel.ayla.utils.commands.annotations.ArgumentType
import me.mrgaabriel.ayla.utils.commands.annotations.InjectArgument
import me.mrgaabriel.ayla.utils.commands.annotations.Subcommand
import me.mrgaabriel.ayla.utils.commands.annotations.SubcommandPermissions
import net.dv8tion.jda.core.Permission

class PlayNowCommand : AbstractCommand("playnow", aliases = listOf("tocaragora", "playskip"), description = "Toque uma música, pulando a atual", category = CommandCategory.MUSIC) {

    @Subcommand
    @SubcommandPermissions([Permission.VOICE_MOVE_OTHERS])
    fun playNow(context: CommandContext, @InjectArgument(ArgumentType.ARGUMENT_LIST) music: String) {
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

        ayla.audioManager.loadAndPlay(context, music, member.voiceState.channel, true)

    }
}