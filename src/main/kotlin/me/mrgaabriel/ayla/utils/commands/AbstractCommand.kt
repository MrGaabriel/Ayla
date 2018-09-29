package me.mrgaabriel.ayla.utils.commands

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.set
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import me.mrgaabriel.ayla.utils.AylaUtils
import me.mrgaabriel.ayla.utils.ConsoleColors
import me.mrgaabriel.ayla.utils.Constants
import me.mrgaabriel.ayla.utils.ayla
import me.mrgaabriel.ayla.utils.commands.annotations.ArgumentType
import me.mrgaabriel.ayla.utils.commands.annotations.InjectArgument
import me.mrgaabriel.ayla.utils.commands.annotations.Subcommand
import me.mrgaabriel.ayla.utils.commands.annotations.SubcommandPermissions
import me.mrgaabriel.ayla.utils.config
import me.mrgaabriel.ayla.utils.tag
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.User
import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.LoggerFactory
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.kotlinFunction

abstract class AbstractCommand(val label: String, val category: CommandCategory = CommandCategory.NONE, val description: String = "Insira descrição do comando aqi", val usage: String = "", val aliases: List<String> = listOf()) {

    val logger = LoggerFactory.getLogger(AbstractCommand::class.java)

    fun matches(msg: Message): Boolean {
        val message = msg.contentRaw

        val config = msg.guild.config

        val args = message.trim().replace(Regex(" +"), " ").split(" ").toMutableList()

        var byMention = false
        if (args[0] == "<@${ayla.config.clientId}>" || args[0] == "<@!${ayla.config.clientId}>") {
            byMention = true
        }

        val command = if (byMention) args[1] else args[0]
        args.removeAt(0)

        val labels = mutableListOf(label)
        labels.addAll(aliases)

        var valid = labels.any { command.equals(config.prefix + it, true) }

        if (byMention) {
            valid = labels.any { command.equals(it, true) }

            args.removeAt(0)
        }

        if (!valid)
            return false

        msg.channel.sendTyping().queue()

        run(CommandContext(msg, args.toTypedArray(), this))
        return true
    }

    fun run(context: CommandContext) {
        val args = context.args
        val baseClass = this::class.java

        // Ao executar, nós iremos pegar várias anotações para ver o que devemos fazer agora
        val methods = this::class.java.methods.filter { it.name != "matches" && it.name != "run" }

        for (method in methods.filter { it.isAnnotationPresent(Subcommand::class.java) }.sortedByDescending { it.parameterCount }) {
            val subcommandAnnotation = method.getAnnotation(Subcommand::class.java)
            val values = subcommandAnnotation.values
            for (value in values.map { it.split(" ") }) {
                var matchedCount = 0
                for ((index, text) in value.withIndex()) {
                    val arg = args.getOrNull(index)
                    if (text == arg)
                        matchedCount++
                }
                val matched = matchedCount == value.size
                if (matched) {
                    if (executeMethod(baseClass, method, context, context.message, context.guild.config.prefix, args ,matchedCount))
                        return
                }
            }
        }

        // Nenhum comando foi executado... #chateado
        for (method in methods.filter { it.isAnnotationPresent(Subcommand::class.java) }.sortedByDescending { it.parameterCount }) {
            val subcommandAnnotation = method.getAnnotation(Subcommand::class.java)
            if (subcommandAnnotation.values.isEmpty()) {
                if (executeMethod(baseClass, method, context, context.message, "g!" /* TODO: Corrigir isto */, args, 0))
                    return
            }
        }
        return
    }

    fun executeMethod(baseClass: Class<out AbstractCommand>, method: Method, context: CommandContext, message: Message, commandLabel: String, args: Array<String>, skipArgs: Int): Boolean {
        // check method arguments
        val arguments = args.toMutableList()
        for (i in 0 until skipArgs)
            arguments.removeAt(0)

        val params = getContextualArgumentList(method, context, message.author, commandLabel, arguments)

        // Agora iremos "validar" o argument list antes de executar
        for ((index, parameter) in method.kotlinFunction!!.valueParameters.withIndex()) {
            if (!parameter.type.isMarkedNullable && params[index] == null)
                return false
        }

        if (params.size != method.parameterCount)
            return false

        try {
            val start = System.currentTimeMillis()
            logger.info("${ConsoleColors.YELLOW}[COMMAND EXECUTED]${ConsoleColors.RESET} (${message.guild.name} -> #${message.channel.name}) ${message.author.tag}: ${message.contentRaw}")

            val cooldown = ayla.commandCooldownCache[message.author.id]
            if (cooldown != null && cooldown > System.currentTimeMillis() && message.author.id != ayla.config.ownerId) {
                ayla.commandCooldownCache.put(message.author.id, cooldown + 1000)
                context.sendMessage(context.getAsMention(true) + "Espere **${AylaUtils.formatDateDiff(cooldown + 1000)}** para executar este comando novamente!")

                logger.info("${ConsoleColors.YELLOW}[COMMAND EXECUTED]${ConsoleColors.RESET} (${message.guild.name} -> #${message.channel.name}) ${message.author.tag}: ${message.contentRaw} - OK! Processado em ${System.currentTimeMillis() - start}ms")
                return true
            }

            val permissions = method.getAnnotation(SubcommandPermissions::class.java)

            if (permissions != null) {
                if (permissions.onlyOwner && context.user.id != ayla.config.ownerId) {
                    context.sendMessage(context.getAsMention(true) + "**Sem permissão!**")

                    logger.info("${ConsoleColors.YELLOW}[COMMAND EXECUTED]${ConsoleColors.RESET} (${message.guild.name} -> #${message.channel.name}) ${message.author.tag}: ${message.contentRaw} - OK! Processado em ${System.currentTimeMillis() - start}ms")
                    return true
                }

                val defaultBotPermissions = arrayOf(
                        Permission.MESSAGE_WRITE,
                        Permission.MESSAGE_EXT_EMOJI,
                        Permission.MESSAGE_EMBED_LINKS,
                        Permission.MESSAGE_ATTACH_FILES,
                        Permission.MESSAGE_ADD_REACTION
                )
                val allBotPermissions = mutableListOf<Permission>()
                allBotPermissions.addAll(defaultBotPermissions)
                allBotPermissions.addAll(permissions.botPermissions)

                val missingBotPermissions = mutableListOf<Permission>()
                missingBotPermissions.addAll(allBotPermissions.filter { !context.guild.selfMember.hasPermission(it) })
                if (missingBotPermissions.isNotEmpty()) {
                    context.sendMessage(context.getAsMention(true) + "Eu não posso executar este comando porque eu não tenho acesso a `${missingBotPermissions.joinToString(" ")}`! :cry:")

                    logger.info("${ConsoleColors.YELLOW}[COMMAND EXECUTED]${ConsoleColors.RESET} (${message.guild.name} -> #${message.channel.name}) ${message.author.tag}: ${message.contentRaw} - OK! Processado em ${System.currentTimeMillis() - start}ms")
                    return true
                }

                val missingPermissions = mutableListOf<Permission>()
                missingPermissions.addAll(permissions.permissions.filter { !message.member.hasPermission(it) })

                if (missingPermissions.isNotEmpty() && context.user.id != ayla.config.ownerId) {
                    context.sendMessage(context.getAsMention(true) + "**Sem permissão!**")

                    logger.info("${ConsoleColors.YELLOW}[COMMAND EXECUTED]${ConsoleColors.RESET} (${message.guild.name} -> #${message.channel.name}) ${message.author.tag}: ${message.contentRaw} - OK! Processado em ${System.currentTimeMillis() - start}ms")
                    return true
                }
            }

            method.invoke(this, *params.toTypedArray())

            ayla.commandCooldownCache.put(message.author.id, System.currentTimeMillis() + 2500)
            logger.info("${ConsoleColors.YELLOW}[COMMAND EXECUTED]${ConsoleColors.RESET} (${message.guild.name} -> #${message.channel.name}) ${message.author.tag}: ${message.contentRaw} - OK! Processado em ${System.currentTimeMillis() - start}ms")
        } catch (ite: InvocationTargetException) {
            val e = ite.targetException

            val payload = JsonObject()

            payload["description"] = "Erro ao executar o comando \"..${this.label}\""
            payload["public"] = false

            val error = JsonObject()
            error["content"] = """
                |Message: ${message.contentRaw} (ID: ${message.id})
                |Channel: ${message.channel}
                |
                |Author: ${message.author}
                |Guild: ${message.guild}
                |Timestamp: ${OffsetDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.systemDefault())}
                |
                |${ExceptionUtils.getStackTrace(e)}
            """.trimMargin()

            val files = JsonObject()
            files["error.txt"] = error

            payload["files"] = files

            val requestBody = HttpRequest.post("https://api.github.com/gists")
                    .userAgent(Constants.USER_AGENT)
                    .authorization("token ${ayla.config.gistToken}")
                    .send(payload.toString())
                    .body()

            val receivedPayload = JsonParser().parse(requestBody)

            val url = receivedPayload["html_url"].string

            // TODO: Fazer com que isto seja configurável
            val guild = ayla.getGuildById("451537296441737216") // Minha guild de testes
            val channel = guild?.getTextChannelById("491339383904010240")

            channel?.sendMessage("<@${ayla.config.ownerId}> $url")?.queue()

            message.channel.sendMessage("${message.author.asMention} Um erro aconteceu durante a execução desse comando! Desculpe pela incoveniência! :cry:").queue()
            logger.error("${ConsoleColors.YELLOW}[COMMAND EXECUTED]${ConsoleColors.RESET} (${message.guild.name} -> #${message.channel.name}) ${message.author.tag}: ${message.contentRaw} - ERROR!")
            logger.error(ExceptionUtils.getStackTrace(e))
        }
        return true
    }

    fun getContextualArgumentList(method: Method, context: CommandContext, sender: User, commandLabel: String, arguments: MutableList<String>): List<Any?> {
        var dynamicArgIdx = 0
        val params = mutableListOf<Any?>()

        for ((index, param) in method.parameters.withIndex()) {
            val typeName = param.type.simpleName.toLowerCase()
            val injectArgumentAnnotation = param.getAnnotation(InjectArgument::class.java)
            when {
                injectArgumentAnnotation != null && injectArgumentAnnotation.type == ArgumentType.COMMAND_LABEL -> {
                    params.add(commandLabel)
                }
                injectArgumentAnnotation != null && injectArgumentAnnotation.type == ArgumentType.ARGUMENT_LIST -> {
                    val duplicated = arguments.toMutableList()
                    for (idx in 0 until dynamicArgIdx) {
                        duplicated.removeAt(0)
                    }

                    params.add(arguments.joinToString(" "))
                }
                injectArgumentAnnotation != null && injectArgumentAnnotation.type == ArgumentType.USER -> {
                    params.add(context.getUser(arguments.getOrNull(dynamicArgIdx)))
                }
                injectArgumentAnnotation != null && injectArgumentAnnotation.type == ArgumentType.TEXT_CHANNEL -> {
                    params.add(context.getTextChannel(arguments.getOrNull(dynamicArgIdx)))
                }
                param.type.isAssignableFrom(String::class.java) -> {
                    params.add(arguments.getOrNull(dynamicArgIdx))
                    dynamicArgIdx++
                }
                param.type.isAssignableFrom(CommandContext::class.java) -> {
                    params.add(context)
                }
                // Sim, é necessário usar os nomes assim, já que podem ser tipos primitivos ou objetos
                typeName == "int" || typeName == "integer" -> {
                    params.add(arguments.getOrNull(dynamicArgIdx)?.toIntOrNull())
                    dynamicArgIdx++
                }
                typeName == "double" -> {
                    params.add(arguments.getOrNull(dynamicArgIdx)?.toDoubleOrNull())
                    dynamicArgIdx++
                }
                typeName == "float" -> {
                    params.add(arguments.getOrNull(dynamicArgIdx)?.toFloatOrNull())
                    dynamicArgIdx++
                }
                typeName == "long" -> {
                    params.add(arguments.getOrNull(dynamicArgIdx)?.toLongOrNull())
                    dynamicArgIdx++
                }
                param.type.isAssignableFrom(Array<String>::class.java) -> {
                    params.add(arguments.subList(dynamicArgIdx, arguments.size).toTypedArray())
                }
                param.type.isAssignableFrom(Array<Int?>::class.java) -> {
                    params.add(arguments.subList(dynamicArgIdx, arguments.size).map { it.toIntOrNull() }.toTypedArray())
                }
                param.type.isAssignableFrom(Array<Double?>::class.java) -> {
                    params.add(arguments.subList(dynamicArgIdx, arguments.size).map { it.toDoubleOrNull() }.toTypedArray())
                }
                param.type.isAssignableFrom(Array<Float?>::class.java) -> {
                    params.add(arguments.subList(dynamicArgIdx, arguments.size).map { it.toFloatOrNull() }.toTypedArray())
                }
                param.type.isAssignableFrom(Array<Long?>::class.java) -> {
                    params.add(arguments.subList(dynamicArgIdx, arguments.size).map { it.toLongOrNull() }.toTypedArray())
                }
                else -> params.add(null)
            }
        }
        return params
    }
}