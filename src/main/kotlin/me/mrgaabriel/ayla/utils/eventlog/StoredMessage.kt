package me.mrgaabriel.ayla.utils.eventlog

import org.bson.codecs.pojo.annotations.*

class StoredMessage @BsonCreator constructor(
        @BsonProperty("_id")
        @get:[BsonIgnore]
        val messageId: String
) {

    @BsonProperty("_id")
    val id = messageId

    val createdAt = System.currentTimeMillis()

    lateinit var authorId: String
    lateinit var channelId: String
    lateinit var content: String

    constructor(messageId: String, content: String, authorId: String, channelId: String) : this(messageId) {
        this.authorId = authorId
        this.content = content
        this.channelId = channelId
    }
}