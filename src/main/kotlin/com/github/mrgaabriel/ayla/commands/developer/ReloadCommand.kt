package com.github.mrgaabriel.ayla.commands.developer

import com.github.mrgaabriel.ayla.commands.AbstractCommand
import com.github.mrgaabriel.ayla.commands.CommandContext
import com.github.mrgaabriel.ayla.config.AylaConfig
import com.github.mrgaabriel.ayla.utils.Static
import com.github.mrgaabriel.ayla.utils.extensions.ayla
import java.io.File

class ReloadCommand : AbstractCommand("reload") {

    override fun onlyOwner(): Boolean = true

    override suspend fun run(context: CommandContext) {
        when (context.args[0]) {
            "commands" -> {
                ayla.loadCommands()

                context.sendMessage("${context.event.author.asMention} Comandos recarregados com sucesso!")
            }

            "shard" -> {
                val arg1 = context.args[1]
                val shardId = arg1.toInt()

                context.sendMessage("${context.event.author.asMention} Reiniciando shard $shardId!!!")
                ayla.shardManager.restart(shardId)

                if (shardId != context.event.jda.shardInfo.shardId) {
                    context.sendMessage("${context.event.author.asMention} Shard $shardId reiniciada com sucesso!!!")
                }
            }

            "bot" -> {
                context.sendMessage("${context.event.author.asMention} Reiniciando o bot, gotta go fast!!!")

                ayla.shardManager.restart()
            }

            "config" -> {
                val file = File("config.yml")
                ayla.config = Static.YAML_MAPPER.readValue(file, AylaConfig::class.java)

                context.sendMessage("${context.event.author.asMention} Configuração recarregada com sucesso")
            }

            "website" -> {
                Thread.getAllStackTraces().keys.filter { it.name == "Website Thread" }.forEach { it.interrupt() }

                ayla.initWebsite()
                context.sendMessage("${context.event.author.asMention} Website recarregado!")
            }
        }
    }
}