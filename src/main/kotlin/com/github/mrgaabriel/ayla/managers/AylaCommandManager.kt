package com.github.mrgaabriel.ayla.managers

import com.github.mrgaabriel.ayla.commands.AylaCommand
import com.github.mrgaabriel.ayla.commands.AylaCommandContext
import com.github.mrgaabriel.ayla.commands.developer.MagicCommand
import com.github.mrgaabriel.ayla.commands.utils.HelpCommand
import com.github.mrgaabriel.ayla.dao.GuildConfig
import com.github.mrgaabriel.ayla.dao.UserProfile
import com.github.mrgaabriel.ayla.events.AylaMessageEvent
import com.github.mrgaabriel.ayla.utils.AylaUtils
import com.github.mrgaabriel.ayla.utils.annotation.InjectParameterType
import com.github.mrgaabriel.ayla.utils.annotation.ParameterType
import com.github.mrgaabriel.ayla.utils.exceptions.CommandException
import com.github.mrgaabriel.ayla.utils.extensions.await
import com.github.mrgaabriel.ayla.utils.extensions.ayla
import com.github.mrgaabriel.ayla.utils.extensions.tag
import com.github.mrgaabriel.ayla.utils.logger
import com.github.mrgaabriel.ayla.utils.t
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.User
import net.perfectdreams.commands.HandlerValueWrapper
import net.perfectdreams.commands.dsl.BaseDSLCommand
import net.perfectdreams.commands.manager.CommandContinuationType
import net.perfectdreams.commands.manager.CommandManager
import java.util.regex.Pattern
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf

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
        registerCommands()

        commandListeners.addParameterListener { context, command, parameter, stack ->
            val annotation = parameter.findAnnotation<InjectParameterType>()

            if (annotation != null) {
                return@addParameterListener when (annotation.type) {
                    ParameterType.ARGUMENT_LIST -> {
                        if (stack.empty()) {
                            return@addParameterListener HandlerValueWrapper(null) // Vamos forçar a retornar null!
                        }

                        stack.reversed().joinToString(" ")
                    }
                }
            }

            return@addParameterListener null
        }

        contextManager.registerContext<User>(
            { clazz -> clazz == User::class || clazz.isSubclassOf(User::class) },
            { context, clazz, stack ->
                val pop = stack.pop()

                AylaUtils.getUser(pop, context.event.guild)
            }
        )

        contextManager.registerContext<TextChannel>(
            { clazz -> clazz == TextChannel::class || clazz.isSubclassOf(TextChannel::class) },
            { context, clazz, stack ->
                val pop = stack.pop()

                AylaUtils.getTextChannel(pop, context.event.guild)
            }
        )

        commandListeners.addThrowableListener { context, command, throwable ->
            if (throwable is CommandException) {
                context.reply(throwable.reason)

                return@addThrowableListener CommandContinuationType.CANCEL
            }

            return@addThrowableListener CommandContinuationType.CONTINUE
        }
    }

    fun registerCommands() {
        // ===[ UTILS ]===
        registerCommand(HelpCommand())

        // ==[ DEVELOPER ]==
        registerCommand(MagicCommand())
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

        val matcher = Pattern.compile("^(<@!?${ayla.config.clientId}>\\s+(?:${config.prefix}\\s*)?|${config.prefix}\\s*)([^\\s]+)")
            .matcher(event.message.contentRaw)

        if (!matcher.find())
            return false

        val label = matcher.group(0)
        val valid = labels.any { (config.prefix + it).equals(label.replace(" ", ""), true) }

        if (!valid)
            return false

        event.channel.sendTyping().await()

        val args = event.message.contentRaw.substring(label.length).split(" ").toMutableList()
        args.removeAt(0)

        val context = AylaCommandContext(event, command, args)

        val start = System.currentTimeMillis()

        if (command.onlyOwner && context.event.author.id !in ayla.config.ownerIds) {
            context.reply("Você não tem permissão para fazer isto!")
            return true
        }

        if (!(command.canHandle.invoke(context))) {
            context.reply("Você não tem permissão para fazer isto!")
            return true
        }

        // TODO: Verificar permissões

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