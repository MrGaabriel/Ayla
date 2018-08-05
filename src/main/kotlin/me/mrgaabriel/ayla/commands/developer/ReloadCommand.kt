package me.mrgaabriel.ayla.commands.developer

import me.mrgaabriel.ayla.commands.*
import me.mrgaabriel.ayla.utils.*

class ReloadCommand : AbstractCommand() {

    init {
        this.label = "reload"
        this.description = "Recarrega o Barry"
        this.usage = "função"

        this.onlyOwner = true
    }

    override fun execute(context: CommandContext) {
        if (context.args.isEmpty()) {
            context.explain()
            return
        }

        if (context.args[0].toLowerCase() == "commands") {
            val oldCommandMap = ayla.commandMap

            ayla.loadCommands()
            context.sendMessage(context.getAsMention(true) + "Comandos recarregados! ${ayla.commandMap.size} comandos recarregados & ${ayla.commandMap.size - oldCommandMap.size} comandos adicionados")
        } else {
            context.explain()
        }
    }
}