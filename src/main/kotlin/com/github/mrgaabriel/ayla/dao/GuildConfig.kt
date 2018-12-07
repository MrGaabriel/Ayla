package com.github.mrgaabriel.ayla.dao

import com.github.mrgaabriel.ayla.tables.GuildConfigs
import com.github.mrgaabriel.ayla.utils.exposed.SnowflakeEntity
import com.github.mrgaabriel.ayla.utils.exposed.SnowflakeEntityClass
import org.jetbrains.exposed.dao.EntityID

class GuildConfig(id: EntityID<String>) : SnowflakeEntity(id) {
    companion object : SnowflakeEntityClass<GuildConfig>(GuildConfigs)

    var prefix by GuildConfigs.prefix

    var welcomeEnabled by GuildConfigs.welcomeEnabled
    var welcomeChannelId by GuildConfigs.welcomeChannelId
}