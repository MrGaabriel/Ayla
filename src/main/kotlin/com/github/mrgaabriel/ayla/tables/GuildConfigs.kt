package com.github.mrgaabriel.ayla.tables

import com.github.mrgaabriel.ayla.utils.exposed.SnowflakeTable

object GuildConfigs : SnowflakeTable("guilds") {

    val prefix = text("prefix").default("..")

    val welcomeEnabled = bool("welcome_enabled").default(false)
    val welcomeChannelId = varchar("welcome_channel_id", 18).nullable()
}