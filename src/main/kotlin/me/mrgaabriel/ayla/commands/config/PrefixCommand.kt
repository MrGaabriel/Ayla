package me.mrgaabriel.ayla.commands.config

import me.mrgaabriel.ayla.utils.*
import me.mrgaabriel.ayla.utils.commands.*
import me.mrgaabriel.ayla.utils.commands.annotations.*
import net.dv8tion.jda.core.*

class PrefixCommand : AbstractCommand(
        "prefix",
        CommandCategory.CONFIG,
        "Altere o prefixo usado para os comandos",
        "prefixo",
        listOf("setprefix", "prefixo")
) {

    @Subcommand
    @SubcommandPermissions([Permission.MANAGE_SERVER])
    fun onExecute(context: CommandContext, @InjectArgument(ArgumentType.ARGUMENT_LIST) prefix: String) {
        if (context.args.isEmpty()) {
            context.explain()
            return
        }

        val config = context.guild.config

        config.prefix = prefix
        context.guild.config = config

        context.sendMessage(context.getAsMention(true) + "Prefixo dos comandos alterado para `$prefix`")
    }
}