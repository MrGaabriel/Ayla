package com.github.mrgaabriel.ayla.commands

import com.github.mrgaabriel.ayla.dao.Guild
import com.github.mrgaabriel.ayla.events.AylaMessageEvent
import com.github.mrgaabriel.ayla.tables.Guilds
import com.github.mrgaabriel.ayla.utils.extensions.await
import com.github.mrgaabriel.ayla.utils.extensions.ayla
import com.github.mrgaabriel.ayla.utils.extensions.tag
import com.github.mrgaabriel.ayla.utils.logger
import com.github.mrgaabriel.ayla.utils.t
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.exceptions.ErrorResponseException
import org.jetbrains.exposed.sql.transactions.transaction

abstract class AbstractCommand(val label: String, val aliases: List<String> = listOf()) {

    val logger by logger()

    open fun getDescription() = "Insira descrição do comando aqui"
    open fun getUsage() = ""

    open fun onlyOwner() = false

    open fun getMemberPermissions() = listOf<Permission>()
    open fun getBotPermissions() = listOf<Permission>()

    suspend fun matches(event: AylaMessageEvent): Boolean {
        val config = transaction(ayla.database) {
            Guild.find { Guilds.id eq event.guild!!.idLong }.first()
        }

        val contentSplitted = event.message.contentRaw.split(" ")

        val labels = mutableListOf(label)
        labels.addAll(aliases)

        val label = contentSplitted[0]
        val valid = labels.any { (config.prefix + it).equals(label, true) }

        if (valid) {
            try {
                event.channel.sendTyping().queue()

                val start = System.currentTimeMillis()
                logger.info("${t.yellow}[COMMAND EXECUTED]${t.reset} (${event.guild!!.name} -> #${event.channel.name}) ${event.author.tag}: ${event.message.contentRaw} (${this.label})")

                val args = contentSplitted.toMutableList()
                args.removeAt(0)

                val context = CommandContext(event, this, args)

                if (onlyOwner() && context.event.author.id != ayla.config.ownerId) {
                    context.sendMessage("${context.event.author.asMention} Você não tem permissão para fazer isto!")

                    logger.info("${t.green}[COMMAND EXECUTED]${t.reset} (${event.guild!!.name} -> #${event.channel.name}) ${event.author.tag}: ${event.message.contentRaw} (${this.label}) - OK! Processado em ${System.currentTimeMillis() - start}ms")
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

                allBotPermissions.addAll(getBotPermissions())

                val missingBotPermissions = allBotPermissions.filter { !context.event.guild?.selfMember!!.hasPermission(it) }

                if (missingBotPermissions.isNotEmpty() && Permission.MESSAGE_WRITE !in missingBotPermissions) {
                    context.sendMessage("${context.event.author.asMention} Eu não posso processar este comando porque eu não tenho as permissões `${missingBotPermissions.joinToString(", ", transform = { it.name })}`")

                    logger.info("${t.green}[COMMAND EXECUTED]${t.reset} (${event.guild!!.name} -> #${event.channel.name}) ${event.author.tag}: ${event.message.contentRaw} (${this.label}) - OK! Processado em ${System.currentTimeMillis() - start}ms")
                    return true
                } else if (Permission.MESSAGE_WRITE in missingBotPermissions) {
                    try {
                        val channel = context.event.author.openPrivateChannel().await()

                        channel.sendMessage("${context.event.author} Eu não tenho permissão para falar no canal ${context.event.textChannel?.asMention}!").queue()
                    } catch (e: ErrorResponseException) { }

                    logger.info("${t.green}[COMMAND EXECUTED]${t.reset} (${event.guild!!.name} -> #${event.channel.name}) ${event.author.tag}: ${event.message.contentRaw} (${this.label}) - OK! Processado em ${System.currentTimeMillis() - start}ms")
                    return true
                }

                val missingMemberPermissions = getMemberPermissions().filter { !context.event.member!!.hasPermission(it) }

                if (missingMemberPermissions.isNotEmpty()) {
                    context.sendMessage("${context.event.author.asMention} Você não tem permissão para fazer isto!")

                    logger.info("${t.green}[COMMAND EXECUTED]${t.reset} (${event.guild!!.name} -> #${event.channel.name}) ${event.author.tag}: ${event.message.contentRaw} (${this.label}) - OK! Processado em ${System.currentTimeMillis() - start}ms")
                    return true
                }

                run(context)

                logger.info("${t.green}[COMMAND EXECUTED]${t.reset} (${event.guild!!.name} -> #${event.channel.name}) ${event.author.tag}: ${event.message.contentRaw} (${this.label}) - OK! Processado em ${System.currentTimeMillis() - start}ms")
            } catch (e: Exception) {
                event.channel.sendMessage("${event.author.asMention} Um erro aconteceu durante a execução deste comando!").queue()

                logger.info("${t.red}[COMMAND STATUS]${t.reset} (${event.guild!!.name} -> #${event.channel.name}) ${event.author.tag}: ${event.message.contentRaw} (${this.label}) - ERROR!", e)
            }

            return true
        }

        return false
    }

    abstract suspend fun run(context: CommandContext)
}