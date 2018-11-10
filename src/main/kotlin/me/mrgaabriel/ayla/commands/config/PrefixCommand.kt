package me.mrgaabriel.ayla.commands.config

import me.mrgaabriel.ayla.utils.commands.AbstractCommand
import me.mrgaabriel.ayla.utils.commands.CommandCategory
import me.mrgaabriel.ayla.utils.commands.CommandContext
import me.mrgaabriel.ayla.utils.commands.annotations.ArgumentType
import me.mrgaabriel.ayla.utils.commands.annotations.InjectArgument
import me.mrgaabriel.ayla.utils.commands.annotations.Subcommand
import me.mrgaabriel.ayla.utils.commands.annotations.SubcommandPermissions
import me.mrgaabriel.ayla.utils.config
import net.dv8tion.jda.core.Permission

class PrefixCommand : AbstractCommand(
        "prefix",
        CommandCategory.CONFIG,
        "Altere o prefixo usado para os comandos",
        "prefixo",
        listOf("setprefix", "prefixo")
) {

    @Subcommand
    @SubcommandPermissions([Permission.MANAGE_SERVER])
    fun onExecute(context: CommandContext, @InjectArgument(ArgumentType.ARGUMENT_LIST) prefix: String?) {
        if (prefix == null) {
			return context.explain()
		}

        val config = context.guild.config

        config.prefix = prefix
        context.guild.config = config

        context.sendMessage(context.getAsMention(true) + "Prefixo dos comandos alterado para `$prefix`")
    }
}