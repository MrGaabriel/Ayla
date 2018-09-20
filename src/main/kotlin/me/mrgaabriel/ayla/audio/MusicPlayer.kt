package me.mrgaabriel.ayla.audio

import me.mrgaabriel.ayla.utils.ayla
import net.dv8tion.jda.core.entities.Guild

class MusicPlayer(val guild: Guild) {

    val player = ayla.audioManager.getPlayer(guild)
    val scheduler = TrackScheduler(player, guild)

    init {
        player.addListener(scheduler)
    }
}