package me.mrgaabriel.ayla.threads

import com.mongodb.client.model.*
import me.mrgaabriel.ayla.utils.*
import org.slf4j.*

class RemoveCachedMessagesThread : Thread("Remove Cached Messages Thread") {

    val logger = LoggerFactory.getLogger(RemoveCachedMessagesThread::class.java)

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
            logger.info("${oldMessages.size} mensagens guardadas antigas deletadas com sucesso")
        }
    }
}