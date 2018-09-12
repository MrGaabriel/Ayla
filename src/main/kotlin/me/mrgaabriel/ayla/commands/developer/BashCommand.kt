package me.mrgaabriel.ayla.commands.developer

import me.mrgaabriel.ayla.utils.commands.AbstractCommand
import me.mrgaabriel.ayla.utils.commands.CommandCategory
import me.mrgaabriel.ayla.utils.commands.CommandContext
import me.mrgaabriel.ayla.utils.commands.annotations.ArgumentType
import me.mrgaabriel.ayla.utils.commands.annotations.InjectArgument
import me.mrgaabriel.ayla.utils.commands.annotations.Subcommand
import me.mrgaabriel.ayla.utils.commands.annotations.SubcommandPermissions

class BashCommand : AbstractCommand(
        "bash",
        CommandCategory.DEVELOPER,
        "Executa comandos"
) {

    @Subcommand
    @SubcommandPermissions([], onlyOwner = true)
    fun onExecute(context: CommandContext, @InjectArgument(ArgumentType.ARGUMENT_LIST) command: String?) {
        if (command == null) {
            context.explain()
            return
        }

        try {
            val process = Runtime.getRuntime().exec(command).also { it.waitFor() }
            process.inputStream.reader(Charsets.UTF_8).use {
                context.sendMessage("```\n\u200B${it.readText()}\nOK! Finalizado com codigo ${process.exitValue()}```")
            }
        } catch (e: Exception) {
            context.sendMessage("Erro! ```${e.message}```")
        }
    }
}