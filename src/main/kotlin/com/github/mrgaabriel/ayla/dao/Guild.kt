package com.github.mrgaabriel.ayla.dao

import com.github.mrgaabriel.ayla.tables.Guilds
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.EntityID

class Guild(id: EntityID<String>) : Entity<String>(id)  {
    companion object : EntityClass<String, Guild>(Guilds)

    var prefix by Guilds.prefix

    var welcomeEnabled by Guilds.welcomeEnabled
    var welcomeChannelId by Guilds.welcomeChannelId
}