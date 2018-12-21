package com.github.mrgaabriel.ayla.managers

import com.github.mrgaabriel.ayla.commands.AylaCommand
import com.github.mrgaabriel.ayla.commands.AylaCommandContext
import com.github.mrgaabriel.ayla.commands.developer.ApiTestCommand
import com.github.mrgaabriel.ayla.dao.GuildConfig
import com.github.mrgaabriel.ayla.dao.UserProfile
import com.github.mrgaabriel.ayla.events.AylaMessageEvent
import com.github.mrgaabriel.ayla.utils.AylaUtils
import com.github.mrgaabriel.ayla.utils.extensions.ayla
import com.github.mrgaabriel.ayla.utils.extensions.tag
import com.github.mrgaabriel.ayla.utils.logger
import com.github.mrgaabriel.ayla.utils.t
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.User
import net.perfectdreams.commands.dsl.BaseDSLCommand
import net.perfectdreams.commands.manager.CommandManager
import java.util.regex.Pattern

class AylaCommandManager : CommandManager<AylaCommandContext, AylaCommand, BaseDSLCommand>() {

    val logger by logger()

    private val commands = mutableListOf<AylaCommand>()

    override fun getRegisteredCommands(): List<AylaCommand> = commands

    override fun registerCommand(command: AylaCommand) {
        commands.add(command)
    }

    override fun unregisterCommand(command: AylaCommand) {
        commands.remove(command)
    }

    init {
        registerCommand(ApiTestCommand())

        contextManager.registerContext<User>(
            { clazz -> clazz == User::class },
            { context, clazz, stack ->
                val pop = stack.pop()

                AylaUtils.getUser(pop, context.event.guild)
            }
        )

        contextManager.registerContext<TextChannel>(
            { clazz -> clazz == TextChannel::class },
            { context, clazz, stack ->
                val pop = stack.pop()

                AylaUtils.getTextChannel(pop, context.event.guild)
            }
        )
    }

    suspend fun dispatch(event: AylaMessageEvent, config: GuildConfig, profile: UserProfile): Boolean {
        val rawMessage = event.message.contentRaw
        val rawArgs = rawMessage.split(" ").toMutableList()

        for (command in getRegisteredCommands()) {
            if (verifyAndDispatch(command, rawArgs, event, config, profile))
                return true
        }

        return false
    }

    suspend fun verifyAndDispatch(command: AylaCommand, rawArgs: MutableList<String>, event: AylaMessageEvent, config: GuildConfig, profile: UserProfile): Boolean {
        for (subCommand in command.subcommands) {
            if (dispatch(subCommand as AylaCommand, rawArgs.drop(1).toMutableList(), event, config, profile, true))
                return true
        }

        if (dispatch(command, rawArgs, event, config, profile))
            return true

        return false
    }

    suspend fun dispatch(command: AylaCommand, rawArgs: MutableList<String>, event: AylaMessageEvent, config: GuildConfig, profile: UserProfile, isSubcommand: Boolean = false): Boolean {
        val labels = command.labels

        var valid = labels.any { rawArgs[0].toLowerCase() == (config.prefix + it).toLowerCase() }
        var byMention = false

        val pattern = Pattern.compile("<@[!]?${ayla.config.clientId}>")

        if (!isSubcommand and pattern.matcher(rawArgs[0]).matches()) {
            valid = labels.any { rawArgs[1].toLowerCase() == it }
            byMention = true
        }

        if (!valid)
            return false

        rawArgs.removeAt(0)
        var args = rawArgs

        if (byMention) {
            rawArgs.removeAt(0)
            args = rawArgs
        }

        val context = AylaCommandContext(event, command, args)

        val start = System.currentTimeMillis()

        logger.info("${t.yellow}[COMMAND EXECUTED]${t.reset} (${context.event.guild.name} -> #${context.event.channel.name}) ${context.event.author.tag}: ${context.event.message.contentRaw}")
        logger.debug("${t.blue}[COMMAND INFO]${t.reset} command.labels: ${command.labels}, args: $args, isSubCommand: $isSubcommand")

        try {
            execute(context, command, args.toTypedArray())

            logger.info("${t.green}[COMMAND EXECUTED]${t.reset} (${context.event.guild.name} -> #${context.event.channel.name}) ${context.event.author.tag}: ${context.event.message.contentRaw} - OK! ${System.currentTimeMillis() - start}ms")
        } catch (e: Exception) {
            logger.error("${t.red}[COMMAND STATUS]${t.reset} (${context.event.guild.name} -> #${context.event.channel.name}) ${context.event.author.tag}: ${context.event.message.contentRaw} - ERROR", e)
        }

        return true
    }
}