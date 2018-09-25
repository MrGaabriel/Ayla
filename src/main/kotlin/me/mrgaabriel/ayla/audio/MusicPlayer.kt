package me.mrgaabriel.ayla.audio

import me.mrgaabriel.ayla.utils.ayla
import net.dv8tion.jda.core.entities.Guild

class MusicPlayer(val guild: Guild) {

    val player = ayla.audioManager.playerManager.createPlayer()
    val scheduler = TrackScheduler(player, guild)

    init {
        val sendingHandler = AudioPlayerSendHandler(player)
        guild.audioManager.sendingHandler = sendingHandler

        player.addListener(scheduler)
    }
}