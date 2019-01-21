package com.github.mrgaabriel.ayla.managers

import com.github.mrgaabriel.ayla.commands.AylaCommand
import com.github.mrgaabriel.ayla.commands.AylaCommandContext
import com.github.mrgaabriel.ayla.commands.config.PrefixCommand
import com.github.mrgaabriel.ayla.commands.config.RedditCommand
import com.github.mrgaabriel.ayla.commands.config.WelcomeCommand
import com.github.mrgaabriel.ayla.commands.developer.*
import com.github.mrgaabriel.ayla.commands.discord.InviteInfoCommand
import com.github.mrgaabriel.ayla.commands.images.IsThisCommand
import com.github.mrgaabriel.ayla.commands.utils.HelpCommand
import com.github.mrgaabriel.ayla.commands.utils.PingCommand
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
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.exceptions.ErrorResponseException
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
        registerCommand(PingCommand())

        // ==[ DEVELOPER ]==
        registerCommand(MagicCommand())
        registerCommand(BashCommand())
        registerCommand(BlacklistCommand())
        registerCommand(EvalCommand())
        registerCommand(ReloadCommand())

        // ==[ CONFIG ]==
        registerCommand(PrefixCommand())
        registerCommand(RedditCommand())
        registerCommand(WelcomeCommand())

        // ==[ DISCORD ]==
        registerCommand(InviteInfoCommand())

        // ==[ IMAGES ]==
        registerCommand(IsThisCommand())
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

        if (profile.blacklisted) {
            try {
                val channel = event.author.openPrivateChannel().await()

                channel.sendMessage("${event.author.asMention} Você foi bloqueado de usar os comandos da Ayla **permanentemente**!\n\nMotivo: `${profile.blacklistReason}`\nSe achou este banimento injusto (duvido que foi injusto, 2bj) contate o `MrGaabriel#4500` via DM").queue()
            } catch (e: ErrorResponseException) { }

            return true
        }

        if (command.onlyOwner && context.event.author.id !in ayla.config.ownerIds) {
            context.reply("Você não tem permissão para fazer isto!")
            return true
        }

        if (!(command.canHandle.invoke(context))) {
            context.reply("Você não tem permissão para fazer isto!")
            return true
        }

        val missingMemberPermissions = command.discordPermissions.filter { !event.member.hasPermission(event.textChannel, it) }

        if (missingMemberPermissions.isNotEmpty()) {
            context.reply("Você não tem permissão para fazer isto!")
            return true
        }

        val allBotPermissions = mutableListOf(
            Permission.MESSAGE_WRITE,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MESSAGE_EMBED_LINKS,
            Permission.MESSAGE_ATTACH_FILES,
            Permission.MESSAGE_ADD_REACTION,
            Permission.MESSAGE_HISTORY
        )

        allBotPermissions.addAll(command.botPermissions)

        val missingBotPermissions = allBotPermissions.filter { !event.guild.selfMember.hasPermission(event.textChannel, it) }

        if (missingBotPermissions.isNotEmpty()) {
            if (Permission.MESSAGE_WRITE in missingBotPermissions) {
                try {
                    val channel = event.author.openPrivateChannel().await()

                    channel.sendMessage("${event.author.asMention} ...eu não tenho permissão para falar no canal de texto ${event.textChannel.asMention}! Peça a algum administrador ou moderador para me dar permissão! Desculpe pela incoveniência. :sob:").queue()
                } catch (e: ErrorResponseException) { }

                return true
            }

            context.reply("Eu queria muito executar este comando, mas eu não tenho as permissões necessárias! Peça a algum administrador ou moderador para me dar as permissões corretas (`${missingBotPermissions.joinToString(", ", transform = { it.name })}`)")
            return true
        }

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