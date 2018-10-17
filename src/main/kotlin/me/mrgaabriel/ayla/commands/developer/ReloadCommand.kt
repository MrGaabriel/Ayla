package me.mrgaabriel.ayla.commands.developer

import com.google.gson.Gson
import me.mrgaabriel.ayla.data.AylaConfig
import me.mrgaabriel.ayla.utils.ayla
import me.mrgaabriel.ayla.utils.commands.AbstractCommand
import me.mrgaabriel.ayla.utils.commands.CommandCategory
import me.mrgaabriel.ayla.utils.commands.CommandContext
import me.mrgaabriel.ayla.utils.commands.annotations.Subcommand
import me.mrgaabriel.ayla.utils.commands.annotations.SubcommandPermissions
import java.io.File

class ReloadCommand : AbstractCommand("reload", CommandCategory.DEVELOPER, "Recarrega a Ayla", "função") {

    @Subcommand
    @SubcommandPermissions([], true)
    fun onExecute(context: CommandContext, function: String) {
        if (context.args.isEmpty()) {
            context.explain()
            return
        }

        when (function) {
            "commands" -> {
                ayla.loadCommands()
                
                context.sendMessage(context.getAsMention(true) + "Comandos recarregados! ${ayla.commandMap.size} comandos recarregados")
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

            "mongo" -> {
                ayla.loadMongo()

                context.sendMessage(context.getAsMention(true) + "MongoDB recarregado com sucesso!")
            }

            "shard" -> {
                val shardId = context.args[1].toInt()

                context.sendMessage("${context.getAsMention()} Reiniciando shard $shardId!")
                ayla.shardManager.restart(shardId)

                if (shardId != context.jda.shardInfo.shardId) {
                    context.sendMessage("${context.getAsMention()} OK! Shard $shardId reiniciada com sucesso!")
                }
            }

            "bot" -> {
                context.sendMessage("${context.getAsMention()} Reiniciando todas as shards... espero que nada de errado aconteça!")

                ayla.shardManager.shutdown()
                ayla.start()
            }

             else -> {
                 context.explain()
             }
        }
    }
}