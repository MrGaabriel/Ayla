package me.mrgaabriel.ayla.data

import org.bson.codecs.pojo.annotations.*

class AylaUser @BsonCreator constructor(@BsonProperty("_id") _id: String) {

    @BsonProperty("_id") val id = _id

    @BsonProperty("xp") val xp = 0

    @BsonProperty("blacklisted") val blacklisted = false
    @BsonProperty("blacklistReason") val blacklistReason = ""
}