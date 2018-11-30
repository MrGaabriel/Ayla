package com.github.mrgaabriel.ayla.dao

import com.github.mrgaabriel.ayla.tables.Guilds
import com.github.mrgaabriel.ayla.tables.SnowflakeEntity
import com.github.mrgaabriel.ayla.tables.SnowflakeEntityClass
import org.jetbrains.exposed.dao.EntityID

class Guild(id: EntityID<String>) : SnowflakeEntity(id) {
    companion object : SnowflakeEntityClass<Guild>(Guilds)

    var prefix by Guilds.prefix

    var welcomeEnabled by Guilds.welcomeEnabled
    var welcomeChannelId by Guilds.welcomeChannelId
}