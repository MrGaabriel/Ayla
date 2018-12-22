package com.github.mrgaabriel.ayla.commands.developer

import com.github.mrgaabriel.ayla.commands.AbstractCommand
import com.github.mrgaabriel.ayla.commands.CommandCategory
import com.github.mrgaabriel.ayla.commands.CommandContext

class BashCommand : AbstractCommand("bash", category = CommandCategory.DEVELOPER) {

    override suspend fun run(context: CommandContext) {
        // TODO: Temporário
        if (context.event.author.id != "163401801121529856")
            return

        val runtime = Runtime.getRuntime()

        val process = runtime.exec(context.args.joinToString(" "))
        process.inputStream.use {
            context.reply("````${it.readBytes()}```")
        }
    }
}