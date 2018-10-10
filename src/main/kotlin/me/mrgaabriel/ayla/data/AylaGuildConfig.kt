package me.mrgaabriel.ayla.data

import net.dv8tion.jda.core.entities.User
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

    var redditSubs = mutableListOf<SubRedditWrapper>()
    var redditSubsLastPost = mutableMapOf<String, Long>()

    var badWordsEnabled = false
    var badWords = mutableListOf<String>()
    var badWordsIgnoredChannels = mutableListOf<String>()

    var userData = mutableListOf<GuildUserData>()

    class SubRedditWrapper @BsonCreator constructor(@BsonProperty("subReddit") val subReddit: String,
                                                    @BsonProperty("channelId") val channelId: String)

    class GuildUserData @BsonCreator constructor(@BsonProperty("userId") val userId: String) {
        var banned = false
        var bannedUntil = 0.toLong()
    }

    fun getUserData(user: User): GuildUserData {
        return getUserData(user.id)
    }

    fun getUserData(userId: String): GuildUserData {
        var found = userData.firstOrNull { it.userId == userId }

        if (found == null) {
            found = GuildUserData(userId)

            userData.add(found)
        }

        return found
    }

    fun saveUserData(data: GuildUserData) {
        val found = userData.firstOrNull { data.userId == it.userId }

        if (found != null) {
            userData.remove(found)
        }

        userData.add(data)
    }
}