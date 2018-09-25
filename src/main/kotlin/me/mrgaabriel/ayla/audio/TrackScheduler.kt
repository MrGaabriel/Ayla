package me.mrgaabriel.ayla.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import me.mrgaabriel.ayla.utils.ayla
import net.dv8tion.jda.core.entities.Guild
import java.util.concurrent.LinkedBlockingQueue

class TrackScheduler(val player: AudioPlayer, val guild: Guild) : AudioEventAdapter() {

    val queue = LinkedBlockingQueue<AudioTrack>()

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
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

        // Destruir o player se não houver nenhuma música para tocar, já que isto economiza muita CPU
        if (player.playingTrack != null) {
            player.stopTrack()
        }

        player.destroy()
        ayla.audioManager.musicPlayers.remove(guild.id)
    }
}