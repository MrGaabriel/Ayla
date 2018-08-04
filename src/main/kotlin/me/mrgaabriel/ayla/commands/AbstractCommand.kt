package me.mrgaabriel.ayla.commands

import com.google.common.flogger.*
import me.mrgaabriel.ayla.utils.*
import net.dv8tion.jda.core.*
import net.dv8tion.jda.core.entities.*
import org.apache.commons.lang3.*
import org.apache.commons.lang3.exception.*
import java.util.*

abstract class AbstractCommand {

    val logger = FluentLogger.forEnclosingClass()

    var label = ""
    var aliases = listOf<String>()

    var description = "Insira uma descrição"
    var usage = ""

    var category = CommandCategory.NONE

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
                logger.atInfo().log("${ConsoleColors.YELLOW}[COMMAND EXECUTED]${ConsoleColors.RESET} (${guild.name} -> #${channel.name}) ${user.tag}: ${message.contentRaw}")

                val args = ArrayUtils.remove(rawMessageArgs.toTypedArray(), 0)

                val context = CommandContext(message, args, this)
                execute(context)

                logger.atInfo().log("${ConsoleColors.YELLOW}[COMMAND STATUS]${ConsoleColors.RESET} (${guild.name} -> #${channel.name}) ${user.tag}: ${message.contentRaw} - OK! Finalizado em ${System.currentTimeMillis() - start}ms")
                return true
            } catch (e: Exception) {
                logger.atSevere().log("${ConsoleColors.YELLOW}[COMMAND STATUS]${ConsoleColors.RESET} (${guild.name} -> #${channel.name}) ${user.tag}: ${message.contentRaw} - ERROR! ${e.message?: ""}")
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

                channel.sendMessage(errorMessage.joinToString("\n")).complete()
                return true
            }
        }

        return false
    }
}