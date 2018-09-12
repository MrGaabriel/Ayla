package me.mrgaabriel.ayla.threads

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.jsonObject
import me.mrgaabriel.ayla.utils.Constants
import me.mrgaabriel.ayla.utils.ayla
import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.LoggerFactory

class UpdateBotStatsThread : Thread("Update Bot Stats") {

    val logger = LoggerFactory.getLogger(UpdateBotStatsThread::class.java)

    override fun run() {
        while (true) {
            try {
                updateStats()

                Thread.sleep(600*1000)
            } catch (e: Exception) {
                logger.error("Erro ao atualizar os dados do bot")
                logger.error(ExceptionUtils.getStackTrace(e))
            }
        }
    }

    fun updateStats() {
        logger.info("Atualizando os dados do bot...")

        // Vespertine Bots List (https://bots.perfectdreams.net)
        val vespertine = HttpRequest.put("https://bots.perfectdreams.net/api/v1/bot/${ayla.config.clientId}/stats")
                .userAgent(Constants.USER_AGENT)
                .authorization(ayla.config.vespertineBotsToken)
                .acceptJson()
                .contentType("application/json")
                .send(jsonObject(
                        "guildCount" to ayla.shards.sumBy { it.guilds.size }
                ).toString())
        logger.info("Vespertine Bots List -> Código ${vespertine.code()}")
    }
}