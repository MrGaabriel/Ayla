package me.mrgaabriel.ayla.threads

import me.mrgaabriel.ayla.utils.ayla
import net.dv8tion.jda.core.entities.Game
import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.LoggerFactory
import java.util.*

class GameUpdateThread : Thread("Game Update Thread") {

    val logger = LoggerFactory.getLogger(GameUpdateThread::class.java)

    override fun run() {
        while (true) {
            try {
                updateGame()

                Thread.sleep(30000)
            } catch (e: Exception) {
                logger.error("Erro ao processar o GameUpdateThread")
                logger.error(ExceptionUtils.getStackTrace(e))
            }
        }
    }

    fun updateGame() {
        val random = SplittableRandom()
        val games = ayla.config.games

        val randomGame = games[random.nextInt(games.size)]

        val gameHandle = Game.of(Game.GameType.valueOf(randomGame.type), randomGame.name, "https://www.twitch.tv/MrGaabriel")

        ayla.setGame(gameHandle)
    }
}

class AylaGameWrapper(
        val name: String,
        val type: String
)