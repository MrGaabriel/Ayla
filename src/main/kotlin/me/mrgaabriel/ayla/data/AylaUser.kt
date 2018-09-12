package me.mrgaabriel.ayla.data

import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonProperty

class AylaUser @BsonCreator constructor(@BsonProperty("_id") _id: String) {

    @BsonProperty("_id")
    val id = _id

    var xp = 0

    var blacklisted = false
    var blacklistReason = ""
}