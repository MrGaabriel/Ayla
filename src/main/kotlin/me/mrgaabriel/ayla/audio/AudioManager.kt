package me.mrgaabriel.ayla.audio

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import me.mrgaabriel.ayla.utils.commands.CommandContext
import me.mrgaabriel.ayla.utils.isValidUrl
import me.mrgaabriel.ayla.utils.youtube.TemmieYouTube
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.VoiceChannel

class AudioManager {

    val playerManager = DefaultAudioPlayerManager()

    val musicPlayers = mutableMapOf<String, MusicPlayer>()
    val players = mutableMapOf<String, AudioPlayer>()

    fun getPlayer(guild: Guild): AudioPlayer {
        val found = players[guild.id]

        if (found != null) {
            return found
        }

        val player = playerManager.createPlayer()
        guild.audioManager.sendingHandler = AudioPlayerSendHandler(player)

        players[guild.id] = player

        return player
    }

    fun getMusicPlayer(guild: Guild): MusicPlayer {
        if (musicPlayers[guild.id] != null) {
            return musicPlayers[guild.id]!!
        }

        val musicPlayer = MusicPlayer(guild)
        musicPlayers[guild.id] = musicPlayer

        return musicPlayer
    }

    fun loadAndPlay(context: CommandContext, identifier: String, channel: VoiceChannel, override: Boolean = false) {
        val musicPlayer = getMusicPlayer(context.guild)

        playerManager.loadItemOrdered(musicPlayer.player, identifier, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) {
                context.guild.audioManager.openAudioConnection(channel)

                musicPlayer.scheduler.queue(track)
                context.sendMessage(context.getAsMention(true) + "Adicionado na fila `${track.info.title}` de `${track.info.author}`! :notes:")
            }

            override fun playlistLoaded(playlist: AudioPlaylist) {
                playlist.tracks.forEach {
                    musicPlayer.scheduler.queue(it)
                }

                context.sendMessage(context.getAsMention(true) + "Adicionado na fila ${playlist.tracks.size} músicas da playlist `${playlist.name}`! :notes:")
            }

            override fun noMatches() {
                // Vamos tentar pesquisar no YouTube para ver se ainda há algo...
                val search = TemmieYouTube().searchOnYouTube(identifier)

                if (search.items.isNotEmpty()) {
                    val videos = search.items.filter { it.id.kind == "youtube#video" }

                    loadAndPlay(context, videos.first().id.videoId, channel, override)
                    return
                }

                context.sendMessage(context.getAsMention(true) + "Não encontrei nada relacionado a `$identifier`!")
            }

            override fun loadFailed(e: FriendlyException) {
                context.sendMessage(context.getAsMention(true) + "Um erro ocorreu ao tentar reproduzir esta música!\n`${e.message}`")
                e.printStackTrace()
            }
        })
    }
}