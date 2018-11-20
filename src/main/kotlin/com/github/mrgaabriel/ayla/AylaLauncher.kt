package com.github.mrgaabriel.ayla

import com.github.mrgaabriel.ayla.config.AylaConfig
import com.github.mrgaabriel.ayla.utils.Static
import java.io.File

object AylaLauncher {

    lateinit var ayla: Ayla

    @JvmStatic
    fun main(args: Array<String>) {
        try {
            println("""
            _         _
           / \  _   _| | __ _
          / _ \| | | | |/ _` |
         / ___ \ |_| | | (_| |
        /_/   \_\__, |_|\__,_|
                |___/
            """.trimIndent())
            val file = File("config.yml")

            if (!file.exists()) {
                file.createNewFile()

                Static.YAML_MAPPER.writeValue(file, AylaConfig())

                println("Parece que é a primeira vez que você está rodando a Ayla!")
                println("Configure-a no arquivo \"config.yml\"")
                return
            }

            val logsFolder = File("logs")
            if (!logsFolder.exists())
                logsFolder.mkdir()

            val config = Static.YAML_MAPPER.readValue(file, AylaConfig::class.java)

            ayla = Ayla(config)
            ayla.start()
        } catch (e: Exception) {
            println("Um erro ocorreu!")
            e.printStackTrace()
            System.exit(1)
        }
    }

}