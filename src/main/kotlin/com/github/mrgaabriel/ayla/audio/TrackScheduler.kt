package com.github.mrgaabriel.ayla.audio

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import lavalink.client.player.IPlayer
import lavalink.client.player.LavalinkPlayer
import lavalink.client.player.event.PlayerEventListenerAdapter
import net.dv8tion.jda.core.entities.Guild
import java.util.concurrent.LinkedBlockingQueue

class TrackScheduler(val guild: Guild, val player: LavalinkPlayer) : PlayerEventListenerAdapter() {

    val queue = LinkedBlockingQueue<AudioTrack>()

    override fun onTrackEnd(player: IPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        if (endReason.mayStartNext) {
            nextTrack()
        }
    }

    fun queue(track: AudioTrack) {
        if (player.playingTrack != null) {
            queue.offer(track)
        } else {
            if (player.playingTrack != null) {
                player.stopTrack()
            }

            player.playTrack(track)
        }
    }

    fun nextTrack() {
        val next = queue.poll()

        if (next != null) {
            player.playTrack(next)
            return
        }

        player.stopTrack()
    }
}