package me.mrgaabriel.ayla.utils.commands

import com.github.kevinsawicki.http.*
import com.github.salomonbrys.kotson.*
import com.google.gson.*
import me.mrgaabriel.ayla.utils.*
import me.mrgaabriel.ayla.utils.commands.annotations.*
import net.dv8tion.jda.core.*
import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.events.message.*
import net.dv8tion.jda.core.events.message.guild.*
import org.apache.commons.lang3.exception.*
import org.jetbrains.kotlin.codegen.coroutines.*
import org.slf4j.*
import java.lang.reflect.*
import java.lang.reflect.Member
import java.net.*
import java.util.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.*

abstract class AbstractCommand(val label: String, val category: CommandCategory = CommandCategory.NONE, val description: String = "Insira descrição do comando aqi", val usage: String = "", val aliases: List<String> = listOf()) {

    val logger = LoggerFactory.getLogger(AbstractCommand::class.java)

    fun matches(msg: Message): Boolean {
        val message = msg.contentDisplay

        val config = msg.guild.config

        val args = message.split(" ").toMutableList()
        val command = args[0]
        args.removeAt(0)

        val labels = mutableListOf(label)
        labels.addAll(aliases)

        val valid = labels.any { command == config.prefix + it }

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

            val permissions = method.getAnnotation(SubcommandPermissions::class.java)

            if (permissions != null) {
                if (permissions.onlyOwner && context.user.id != ayla.config.ownerId) {
                    context.sendMessage(context.getAsMention(true) + "**Sem permissão!**")

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

            logger.info("${ConsoleColors.YELLOW}[COMMAND EXECUTED]${ConsoleColors.RESET} (${message.guild.name} -> #${message.channel.name}) ${message.author.tag}: ${message.contentRaw} - OK! Processado em ${System.currentTimeMillis() - start}ms")
        } catch (ite: InvocationTargetException) {
            val e = ite.targetException

            val payload = JsonObject()

            payload["description"] = "Erro ao executar o comando \"..${this.label}\""
            payload["public"] = false

            val error = JsonObject()
            error["content"] = ExceptionUtils.getStackTrace(e)

            val files = JsonObject()
            files["error.txt"] = error

            payload["files"] = files

            val requestBody = HttpRequest.post("https://api.github.com/gists")
                    .userAgent(Constants.USER_AGENT)
                    .authorization("token 113db8b5517f7c94fd7cde407b138f81146d66a9")
                    .send(payload.toString())
                    .body()

            val receivedPayload = JsonParser().parse(requestBody)

            val url = receivedPayload["html_url"].string

            val errorMessage = arrayOf(
                    message.author.asMention,
                    "Um erro aconteceu durante a execução desse comando!",
                    "",
                    url,
                    "",
                    "Contate o `MrGaabriel#2430` e mande este erro!"
            )

            message.channel.sendMessage(errorMessage.joinToString("\n")).queue()
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
                    params.add(arguments.joinToString(" "))
                }
                param.type.isAssignableFrom(Member::class.java) && sender is Member -> { params.add(sender) }
                param.type.isAssignableFrom(User::class.java) && sender is User -> { params.add(sender) }
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