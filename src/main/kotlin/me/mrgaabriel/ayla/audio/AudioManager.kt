package me.mrgaabriel.ayla.audio

import com.github.benmanes.caffeine.cache.Caffeine
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import me.mrgaabriel.ayla.utils.commands.CommandContext
import me.mrgaabriel.ayla.utils.stripCodeMarks
import me.mrgaabriel.ayla.utils.youtube.TemmieYouTube
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.VoiceChannel
import java.util.concurrent.TimeUnit

class AudioManager {

    val playerManager = DefaultAudioPlayerManager()

    val musicPlayers = Caffeine.newBuilder().expireAfterAccess(30, TimeUnit.SECONDS)
            .build<String, MusicPlayer>()
            .asMap()

    init {
        playerManager.frameBufferDuration = 1000
        playerManager.setItemLoaderThreadPoolSize(1000)

        AudioSourceManagers.registerRemoteSources(playerManager)
    }

    fun getMusicPlayer(guild: Guild): MusicPlayer {
        return musicPlayers.getOrPut(guild.id) { MusicPlayer(guild) }
    }

    fun getAudioPlayer(guild: Guild): AudioPlayer {
        return getMusicPlayer(guild).player
    }

    fun loadAndPlay(context: CommandContext, identifier: String, channel: VoiceChannel, override: Boolean = false) {
        val musicPlayer = getMusicPlayer(context.guild)

        playerManager.loadItemOrdered(musicPlayer.player, identifier, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) {
                context.guild.audioManager.openAudioConnection(channel)

                if (override) {
                    musicPlayer.player.playTrack(track)
                } else {
                    musicPlayer.scheduler.queue(track)
                }

                context.sendMessage(context.getAsMention(true) + "Adicionado na fila `${track.info.title.stripCodeMarks()}` de `${track.info.author.stripCodeMarks()}`! :notes:")
            }

            override fun playlistLoaded(playlist: AudioPlaylist) {
                context.guild.audioManager.openAudioConnection(channel)

                playlist.tracks.forEach {
                    musicPlayer.scheduler.queue(it)
                }

                context.sendMessage(context.getAsMention(true) + "Adicionado na fila ${playlist.tracks.size} músicas da playlist `${playlist.name.stripCodeMarks()}`! :notes:")
            }

            override fun noMatches() {
                // Vamos tentar pesquisar no YouTube para ver se ainda há algo...
                val search = TemmieYouTube().searchOnYouTube(identifier)

                if (search.items.isNotEmpty()) {
                    val videos = search.items.filter { it.id.kind == "youtube#video" }

                    loadAndPlay(context, videos.first().id.videoId, channel, override)
                    return
                }

                context.sendMessage(context.getAsMention(true) + "Não encontrei nada relacionado a `${identifier.stripCodeMarks()}`!")
            }

            override fun loadFailed(e: FriendlyException) {
                context.sendMessage(context.getAsMention(true) + "Um erro ocorreu ao tentar reproduzir esta música!\n`${e.message}`")
                e.printStackTrace()
            }
        })
    }
}