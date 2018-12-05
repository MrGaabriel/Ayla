package com.github.mrgaabriel.ayla.audio

import com.github.mrgaabriel.ayla.commands.CommandContext
import com.github.mrgaabriel.ayla.utils.extensions.ayla
import com.github.mrgaabriel.ayla.youtube.TemmieYouTube
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import lavalink.client.io.jda.JdaLavalink
import lavalink.client.player.LavalinkPlayer
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.VoiceChannel
import java.net.URI

class AudioManager {

    val playerManager = DefaultAudioPlayerManager()
    val lavalink = JdaLavalink(ayla.config.clientId, ayla.config.shardsCount, { ayla.shardManager.getShardById(it) })

    val musicPlayers = mutableMapOf<String, MusicPlayer>()

    init {
        playerManager.frameBufferDuration = 1000
        playerManager.setItemLoaderThreadPoolSize(1000)

        AudioSourceManagers.registerRemoteSources(playerManager)
        AudioSourceManagers.registerLocalSource(playerManager)

        lavalink.addNode(URI(ayla.config.lavalinkHostname), ayla.config.lavalinkPassword)
    }

    fun getMusicPlayer(guild: Guild): MusicPlayer {
        return musicPlayers.getOrPut(guild.id) { MusicPlayer(guild) }
    }

    fun getAudioPlayer(guild: Guild): LavalinkPlayer {
        return getMusicPlayer(guild).player
    }

    fun loadAndPlay(context: CommandContext, identifier: String, channel: VoiceChannel, override: Boolean = false) {
        context.event.guild.audioManager.isSelfDeafened = true
        context.event.guild.audioManager.isSelfMuted = false

        val musicPlayer = getMusicPlayer(context.event.guild)

        val textChannel = context.event.channel

        playerManager.loadItemOrdered(musicPlayer.player, identifier, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) {
                musicPlayer.link.connect(channel)

                if (override) {
                    musicPlayer.player.playTrack(track)
                } else {
                    musicPlayer.scheduler.queue(track)
                }

                textChannel.sendMessage("${context.event.author.asMention} Adicionado na fila `${track.info.title.replace("`", "")}` de `${track.info.author.replace("`", "")}`! :notes:").queue()
            }

            override fun playlistLoaded(playlist: AudioPlaylist) {
                musicPlayer.link.connect(channel)

                playlist.tracks.forEach {
                    musicPlayer.scheduler.queue(it)
                }

                textChannel.sendMessage("${context.event.author.asMention} Adicionado na fila ${playlist.tracks.size} músicas da playlist `${playlist.name.replace("`", "")}`! :notes:").queue()
            }

            override fun noMatches() {
                // Vamos tentar pesquisar no YouTube para ver se ainda há algo...
                val search = TemmieYouTube.searchOnYouTube(identifier)

                if (search.items.isNotEmpty()) {
                    val videos = search.items.filter { it.id.kind == "youtube#video" }

                    loadAndPlay(context, videos.first().id.videoId, channel, override)
                    return
                }

                textChannel.sendMessage("${context.event.author.asMention} Não encontrei nada relacionado a `${identifier.replace("`", "")}`!").queue()
            }

            override fun loadFailed(e: FriendlyException) {
                textChannel.sendMessage("${context.event.author.asMention} Um erro ocorreu ao tentar reproduzir esta música!\n`${e.message}`").queue()
                e.printStackTrace()
            }
        })
    }
}
