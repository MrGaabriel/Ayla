package me.mrgaabriel.ayla.threads

import com.google.common.flogger.*
import me.mrgaabriel.ayla.utils.*
import net.dv8tion.jda.core.entities.*
import org.apache.commons.lang3.exception.*
import java.util.*
import kotlin.concurrent.*

class GameUpdateThread {

    val logger = FluentLogger.forEnclosingClass()

    fun start() {
        thread(name="Game Update Thread") {
            while (true) {
                try {
                    updateGame()

                    Thread.sleep(30000)
                } catch (e: Exception) {
                    logger.atWarning().log("Erro ao processar o GameUpdateThread")
                    logger.atWarning().log(ExceptionUtils.getStackTrace(e))
                }
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