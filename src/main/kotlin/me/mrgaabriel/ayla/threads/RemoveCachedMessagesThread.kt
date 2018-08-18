package me.mrgaabriel.ayla.threads

import com.google.common.flogger.*
import com.mongodb.client.model.*
import me.mrgaabriel.ayla.utils.*
import kotlin.concurrent.*

class RemoveCachedMessagesThread : Thread("Remove Cached Messages Thread") {

    val logger = FluentLogger.forEnclosingClass()

    override fun run() {
        while (true) {
            deleteOldMessages()

            Thread.sleep(60 * 1000)
        }
    }

    fun deleteOldMessages() {
        val storageMessages = ayla.storedMessagesColl.find()
        // Pega todas as mensagens guardadas com mais de uma semana
        val oldMessages = storageMessages.filter { (System.currentTimeMillis() - it.createdAt) >= 604800000 }

        for (message in oldMessages) {
            ayla.storedMessagesColl.deleteOne(Filters.eq("_id", message.messageId))
        }

        if (oldMessages.isNotEmpty()) {
            logger.atInfo().log("${oldMessages.size} mensagens guardadas antigas deletadas com sucesso")
        }
    }
}