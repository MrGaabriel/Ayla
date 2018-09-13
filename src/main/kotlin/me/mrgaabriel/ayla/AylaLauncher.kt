package me.mrgaabriel.ayla

import com.google.gson.Gson
import me.mrgaabriel.ayla.data.AylaConfig
import org.slf4j.LoggerFactory
import java.io.File

object AylaLauncher {

    lateinit var ayla: Ayla

    val logger = LoggerFactory.getLogger(AylaLauncher::class.java)

    @JvmStatic
    fun main(args: Array<String>) {
        val file = File("config.json")

        if (!file.exists()) {
            file.createNewFile()
            file.writeText(Gson().toJson(AylaConfig()))

            println("Parece que é a primeira vez que você está rodando a Ayla!")
            println("Configure-a no arquivo \"config.json\"")

            System.exit(1)
        }

        logger.info("Inicializando a Ayla...")

        val config = Gson().fromJson(file.readText(), AylaConfig::class.java)
        ayla = Ayla(config)

        ayla.start()
    }
}