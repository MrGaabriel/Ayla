package com.github.mrgaabriel.ayla.commands

import com.github.mrgaabriel.ayla.dao.Guild
import com.github.mrgaabriel.ayla.events.AylaMessageEvent
import com.github.mrgaabriel.ayla.tables.Guilds
import com.github.mrgaabriel.ayla.utils.extensions.ayla
import com.github.mrgaabriel.ayla.utils.extensions.tag
import com.github.mrgaabriel.ayla.utils.logger
import com.github.mrgaabriel.ayla.utils.t
import org.jetbrains.exposed.sql.transactions.transaction

abstract class AbstractCommand(val label: String, val aliases: List<String> = listOf()) {

    val logger by logger()

    suspend fun matches(event: AylaMessageEvent): Boolean {
        val config = transaction(ayla.database) {
            Guild.find { Guilds.id eq event.guild!!.idLong }.first()
        }

        val contentSplitted = event.message.contentRaw.split(" ")

        val labels = mutableListOf(label)
        labels.addAll(labels)

        val label = contentSplitted[0]
        val valid = labels.any { (config.prefix + it).equals(label, true) }

        if (valid) {
            try {
                val start = System.currentTimeMillis()
                logger.info("${t.yellow}[COMMAND EXECUTED]${t.reset} (${event.guild!!.name} -> #${event.channel.name}) ${event.author.tag}: ${event.message.contentRaw} (${this.label})")

                val context = CommandContext(event, this)
                run(context)

                logger.info("${t.green}[COMMAND EXECUTED]${t.reset} (${event.guild!!.name} -> #${event.channel.name}) ${event.author.tag}: ${event.message.contentRaw} (${this.label}) - OK! Processado em ${System.currentTimeMillis() - start}")
            } catch (e: Exception) {
                logger.info("${t.red}[COMMAND STATUS]${t.reset} (${event.guild!!.name} -> #${event.channel.name}) ${event.author.tag}: ${event.message.contentRaw} (${this.label}) - ERROR!", e)
            }

            return true
        }

        return false
    }

    abstract suspend fun run(context: CommandContext)
}