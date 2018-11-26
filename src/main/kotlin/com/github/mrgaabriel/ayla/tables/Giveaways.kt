package com.github.mrgaabriel.ayla.tables

import com.github.mrgaabriel.ayla.utils.exposed.array
import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.TextColumnType

object Giveaways : LongIdTable() {

    val messageId = varchar("message_id", 18).index()

    val guildId = varchar("guild_id", 18)
    val channelId = varchar("channel_id", 18)
    val authorId = varchar("author_id", 18)

    val prize = text("prize")
    val users = array<String>("users", TextColumnType())

    val endsAt = long("ends_at")
}