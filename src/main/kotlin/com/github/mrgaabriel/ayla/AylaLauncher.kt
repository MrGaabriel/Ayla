package com.github.mrgaabriel.ayla

import com.github.mrgaabriel.ayla.config.AylaConfig
import com.github.mrgaabriel.ayla.utils.Static
import java.io.File
import java.nio.file.Paths
import java.util.jar.Attributes
import java.util.jar.JarFile

object AylaLauncher {

    lateinit var ayla: Ayla

    @JvmStatic
    fun main(args: Array<String>) {
        try {
            // https://www.reddit.com/r/Kotlin/comments/8qdd4x/kotlin_script_engine_and_your_classpaths_what/
            val path = this::class.java.protectionDomain.codeSource.location.path
            val jar = JarFile(path)
            val mf = jar.manifest
            val mattr = mf.mainAttributes
            // Yes, you SHOULD USE Attributes.Name.CLASS_PATH! Don't try using "Class-Path", it won't work!
            val manifestClassPath = mattr[Attributes.Name.CLASS_PATH] as String

            // The format within the Class-Path attribute is different than the one expected by the property, so let's fix it!
            // By the way, don't forget to append your original JAR at the end of the string!
            val clazz = AylaLauncher::class.java
            val protectionDomain = clazz.protectionDomain
            val propClassPath = manifestClassPath.replace(" ", ":") + ":${Paths.get(protectionDomain.codeSource.location.toURI()).fileName}"

            // Now we set it to our own classpath
            System.setProperty("kotlin.script.classpath", propClassPath)

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