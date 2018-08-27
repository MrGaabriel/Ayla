package me.mrgaabriel.ayla.commands

import me.mrgaabriel.ayla.utils.*
import net.dv8tion.jda.core.*
import net.dv8tion.jda.core.entities.*
import org.apache.commons.lang3.*
import org.apache.commons.lang3.exception.*
import org.slf4j.*
import java.util.*

abstract class AbstractCommand {

    val logger = LoggerFactory.getLogger(AbstractCommand::class.java)

    var label = ""
    var aliases = mutableListOf<String>()

    var description = "Insira uma descrição"
    var usage = ""

    var category = CommandCategory.NONE

    var onlyOwner = false

    var botPermissions = mutableListOf<Permission>()
    var memberPermissions = mutableListOf<Permission>()

    abstract fun execute(context: CommandContext)

    fun matches(message: Message): Boolean {
        val user = message.author
        val channel = message.channel

        val guild = message.guild
        val guildConfig = guild.config

        val labels = mutableListOf<String>()
        labels.add(label)
        labels.addAll(aliases)

        val rawMessageArgs = message.contentRaw.split(" ")

        val valid = labels.any { rawMessageArgs[0].toLowerCase() == guildConfig.prefix + it.toLowerCase() }

        if (valid) {
            try {
                val start = System.currentTimeMillis()
                logger.info("${ConsoleColors.YELLOW}[COMMAND EXECUTED]${ConsoleColors.RESET} (${guild.name} -> #${channel.name}) ${user.tag}: ${message.contentRaw}")

                channel.sendTyping().queue()

                val args = ArrayUtils.remove(rawMessageArgs.joinToString(" ").trim().split(" ").toTypedArray(), 0)

                val context = CommandContext(message, args, this)

                if (onlyOwner && user.id != ayla.config.ownerId) {
                    context.sendMessage(context.getAsMention(true) + "**Sem permissão!**")

                    logger.info("${ConsoleColors.YELLOW}[COMMAND STATUS]${ConsoleColors.RESET} (${guild.name} -> #${channel.name}) ${user.tag}: ${message.contentRaw} - OK! Finalizado em ${System.currentTimeMillis() - start}ms")
                    return true
                }

                botPermissions.addAll(listOf(Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_EXT_EMOJI))
                val missingBotPermissions = botPermissions.filter { !guild.selfMember.hasPermission(it) }

                if (missingBotPermissions.isNotEmpty()) {
                    context.sendMessage(context.getAsMention(true) + "Eu não consigo executar esse comando! Eu preciso das permissões ${missingBotPermissions.joinToString(", ", transform={"`$it`"})}! Peça a algum administrador para me conceder, obrigada!")

                    logger.info("${ConsoleColors.YELLOW}[COMMAND STATUS]${ConsoleColors.RESET} (${guild.name} -> #${channel.name}) ${user.tag}: ${message.contentRaw} - OK! Finalizado em ${System.currentTimeMillis() - start}ms")
                    return true
                }

                val missingMemberPermissions = memberPermissions.filter { !guild.getMember(user).hasPermission(it) }
                if (missingMemberPermissions.isNotEmpty()) {
                    context.sendMessage(context.getAsMention(true) + "**Sem permissão!**")

                    logger.info("${ConsoleColors.YELLOW}[COMMAND STATUS]${ConsoleColors.RESET} (${guild.name} -> #${channel.name}) ${user.tag}: ${message.contentRaw} - OK! Finalizado em ${System.currentTimeMillis() - start}ms")
                    return true
                }

                execute(context)

                logger.info("${ConsoleColors.YELLOW}[COMMAND STATUS]${ConsoleColors.RESET} (${guild.name} -> #${channel.name}) ${user.tag}: ${message.contentRaw} - OK! Finalizado em ${System.currentTimeMillis() - start}ms")
                return true
            } catch (e: Exception) {
                logger.info("${ConsoleColors.YELLOW}[COMMAND STATUS]${ConsoleColors.RESET} (${guild.name} -> #${channel.name}) ${user.tag}: ${message.contentRaw} - ERROR! ${e.message?: ""}")
                e.printStackTrace()

                val stacktrace = ExceptionUtils.getStackTrace(e)
                val base64 = String(Base64.getEncoder().encode(stacktrace.toByteArray()))

                val errorMessage = arrayOf(
                        user.asMention,
                        "Um erro aconteceu durante a execução desse comando!",
                        "```$base64```",
                        "",
                        "Contate o `MrGaabriel#2430` e mande o erro!"
                )

                channel.sendMessage(errorMessage.joinToString("\n")).queue()
                return true
            }
        }

        return false
    }
}