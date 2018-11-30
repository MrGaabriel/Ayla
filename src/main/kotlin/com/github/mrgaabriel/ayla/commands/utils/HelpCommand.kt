package com.github.mrgaabriel.ayla.commands.utils

import com.github.mrgaabriel.ayla.commands.AbstractCommand
import com.github.mrgaabriel.ayla.commands.CommandContext
import com.github.mrgaabriel.ayla.dao.Guild
import com.github.mrgaabriel.ayla.tables.Guilds
import com.github.mrgaabriel.ayla.utils.extensions.ayla
import org.jetbrains.exposed.sql.transactions.transaction

class HelpCommand : AbstractCommand("help", aliases = listOf("ajuda", "comandos", "commands")) {

    override fun getDescription(): String {
        return "Veja os comandos que a Ayla tem"
    }

    override suspend fun run(context: CommandContext) {
        val config = transaction(ayla.database) {
            Guild.find { Guilds.id eq context.event.guild.id }.first()
        }

        val arg0 = context.args.getOrNull(0)
        if (arg0 != null) {
            val command = ayla.commandMap.firstOrNull { it.label == arg0 || arg0 in it.aliases }

            if (command != null) {
                val dummyContext = CommandContext(context.event, command, context.args)

                dummyContext.explain()
            } else {
                context.sendMessage("${context.event.author.asMention} Comando não encontrado!")
            }

            return
        }

        context.sendMessage(
            """
            ```Lista de comandos```
            Use `${config.prefix}help [comando]` para saber mais sobre um comando.
            **Comandos disponíveis (${ayla.commandMap.size}):**

            ${ayla.commandMap.joinToString(" ", transform = { "`${it.label}`" })}
            ```Lembre-se de usar o prefixo (${config.prefix}) antes dos comandos!```
        """.trimIndent()
        )
    }
}