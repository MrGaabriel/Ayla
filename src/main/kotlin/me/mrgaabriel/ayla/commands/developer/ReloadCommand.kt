package me.mrgaabriel.ayla.commands.developer

import com.google.gson.Gson
import me.mrgaabriel.ayla.data.AylaConfig
import me.mrgaabriel.ayla.listeners.DiscordListeners
import me.mrgaabriel.ayla.listeners.EventLogListeners
import me.mrgaabriel.ayla.utils.ayla
import me.mrgaabriel.ayla.utils.commands.AbstractCommand
import me.mrgaabriel.ayla.utils.commands.CommandCategory
import me.mrgaabriel.ayla.utils.commands.CommandContext
import me.mrgaabriel.ayla.utils.commands.annotations.Subcommand
import me.mrgaabriel.ayla.utils.commands.annotations.SubcommandPermissions
import java.io.File

class ReloadCommand : AbstractCommand(
        "reload",
        CommandCategory.DEVELOPER,
        "Recarrega a Ayla",
        "função"
) {

    @Subcommand
    @SubcommandPermissions([], true)
    fun onExecute(context: CommandContext, function: String) {
        if (context.args.isEmpty()) {
            context.explain()
            return
        }

        when (function) {
            "commands" -> {
                val oldCommandMap = ayla.commandMap

                ayla.loadCommands()
                context.sendMessage(context.getAsMention(true) + "Comandos recarregados! ${ayla.commandMap.size} comandos recarregados & ${ayla.commandMap.size - oldCommandMap.size} comandos adicionados")
            }

            "throw_runtime_exception" -> {
                throw RuntimeException("..reload throw_runtime_exception")
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