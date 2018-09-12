package me.mrgaabriel.ayla.data

import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonProperty

class AylaGuildConfig @BsonCreator constructor(@BsonProperty("_id") _id: String) {

    @BsonProperty("_id")
    val id = _id

    var prefix = ".."

    var eventLogEnabled = false
    var eventLogChannel = ""
    var eventLogIgnoredChannels = mutableListOf<String>()

    var welcomeEnabled = false
    var welcomeChannel = ""

    var redditSubs = mutableMapOf<String, String>() // Sub reddit e ID do canal que vai ser avisado
    var lastRedditPostCreation = mutableMapOf<String, String>()

    var badWordsEnabled = true
    var badWords = mutableListOf<String>()
    var badWordsIgnoredChannels = mutableListOf<String>()
}