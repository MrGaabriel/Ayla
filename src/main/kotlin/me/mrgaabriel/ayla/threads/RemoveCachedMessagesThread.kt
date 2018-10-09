package me.mrgaabriel.ayla.threads

import com.mongodb.client.model.Filters
import me.mrgaabriel.ayla.utils.ayla
import org.slf4j.LoggerFactory

class RemoveCachedMessagesThread : Thread("Remove Cached Messages Thread") {

    val logger = LoggerFactory.getLogger(RemoveCachedMessagesThread::class.java)

    override fun run() {
        while (true) {
            deleteOldMessages()

            Thread.sleep(60 * 1000)
        }
    }

    fun deleteOldMessages() {
        val storagedMessages = ayla.storedMessagesColl.find()
        val oldMessages = storagedMessages.filter { (it.createdAt + 604800000) > System.currentTimeMillis() }

        for (message in oldMessages) {
            ayla.storedMessagesColl.deleteOne(Filters.eq("_id", message.messageId))
        }

        if (oldMessages.isNotEmpty()) {
            logger.info("${oldMessages.size} mensagens guardadas antigas deletadas com sucesso")
        }
    }
}