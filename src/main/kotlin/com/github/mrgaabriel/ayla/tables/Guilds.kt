package com.github.mrgaabriel.ayla.tables

object Guilds : SnowflakeTable() {

    val prefix = text("prefix").default("..")

    val welcomeEnabled = bool("welcome_enabled").default(false)
    val welcomeChannelId = varchar("welcome_channel_id", 18).nullable()
}