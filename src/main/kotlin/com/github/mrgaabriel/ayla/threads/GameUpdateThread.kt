package com.github.mrgaabriel.ayla.threads

import com.github.mrgaabriel.ayla.utils.extensions.ayla
import com.github.mrgaabriel.ayla.utils.logger
import net.dv8tion.jda.core.entities.Game
import java.util.*

class GameUpdateThread : Thread("Game Update Thread") {

    val logger by logger()

    override fun run() {
        while (true) {
            try {
                updateGame()
            } catch (e: Exception) {
                logger.info("Erro ao atualizar o \"Jogando\" do bot", e)
            }

            Thread.sleep(30*1000)
        }
    }

    fun updateGame() {
        val randomGame = ayla.config.games.random()
        val game = Game.of(Game.GameType.valueOf(randomGame.type), randomGame.name, "https://www.twitch.tv/MrGaabriel")

        ayla.shardManager.setGame(game)
    }
}