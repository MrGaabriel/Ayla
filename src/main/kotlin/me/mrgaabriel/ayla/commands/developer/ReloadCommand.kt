package me.mrgaabriel.ayla.commands.developer

import com.google.gson.*
import me.mrgaabriel.ayla.commands.*
import me.mrgaabriel.ayla.data.*
import me.mrgaabriel.ayla.listeners.*
import me.mrgaabriel.ayla.utils.*
import java.io.*

class ReloadCommand : AbstractCommand() {

    init {
        this.label = "reload"
        this.description = "Recarrega a Ayla"
        this.usage = "função"

        this.category = CommandCategory.DEVELOPER
        this.onlyOwner = true
    }

    override fun execute(context: CommandContext) {
        if (context.args.isEmpty()) {
            context.explain()
            return
        }

        when (context.args[0].toLowerCase()) {
            "commands" -> {
                val oldCommandMap = ayla.commandMap

                ayla.loadCommands()
                context.sendMessage(context.getAsMention(true) + "Comandos recarregados! ${ayla.commandMap.size} comandos recarregados & ${ayla.commandMap.size - oldCommandMap.size} comandos adicionados")
            }

            "config" -> {
                val file = File("config.json")
                val config = Gson().fromJson(file.readText(), AylaConfig::class.java)

                ayla.config = config
                context.sendMessage(context.getAsMention(true) + "Configuração recarregada!")
            }

            "listeners" -> {
                ayla.shards.forEach { shard ->
                    shard.registeredListeners.forEach {
                        shard.removeEventListener(it)
                    }
                }

                ayla.shards.forEach { shard ->
                    shard.addEventListener(DiscordListeners())
                    shard.addEventListener(EventLogListeners())
                }

                context.sendMessage(context.getAsMention(true) + "Listeners recarregados com sucesso!")
            }

            "mongo" -> {
                ayla.loadMongo()

                context.sendMessage(context.getAsMention(true) + "MongoDB recarregado com sucesso!")
            }

             else -> {
                 context.explain()
             }
        }
    }
}