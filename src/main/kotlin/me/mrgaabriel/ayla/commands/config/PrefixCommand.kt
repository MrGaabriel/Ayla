package me.mrgaabriel.ayla.commands.config

import me.mrgaabriel.ayla.commands.*
import me.mrgaabriel.ayla.utils.*
import net.dv8tion.jda.core.*

class PrefixCommand : AbstractCommand() {

    init {
        this.label = "prefix"
        this.usage = "prefixo"
        this.description = "Altere o prefixo usado para os comandos"

        this.memberPermissions = mutableListOf(Permission.MANAGE_SERVER)
        this.aliases = mutableListOf("setprefix", "prefixo")

        this.category = CommandCategory.CONFIG
    }

    override fun execute(context: CommandContext) {
        if (context.args.isEmpty()) {
            context.explain()
            return
        }

        val prefix = context.args.joinToString(" ") // Permitir definir prefixos com espaços

        val config = context.guild.config

        config.prefix = prefix
        context.guild.config = config

        context.sendMessage(context.getAsMention(true) + "Prefixo dos comandos alterado para `$prefix`")
    }
}