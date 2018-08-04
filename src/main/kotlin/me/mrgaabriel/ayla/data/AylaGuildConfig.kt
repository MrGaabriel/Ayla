package me.mrgaabriel.ayla.data

import org.bson.codecs.pojo.annotations.*

class AylaGuildConfig @BsonCreator constructor(@BsonProperty("_id") _id: String) {

    @BsonProperty("_id") val id = _id

    @BsonProperty("prefix") var prefix = ".."
}