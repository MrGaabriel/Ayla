package com.github.mrgaabriel.ayla.audio

import com.github.mrgaabriel.ayla.utils.extensions.ayla
import net.dv8tion.jda.core.entities.Guild

class MusicPlayer(val guild: Guild) {

    val link = ayla.audioManager.lavalink.getLink(guild)
    val player = link.player

    val scheduler = TrackScheduler(guild, player)

    init {
        player.addListener(scheduler)
    }
}