package com.github.mrgaabriel.ayla.dao

import com.github.mrgaabriel.ayla.tables.SnowflakeEntity
import com.github.mrgaabriel.ayla.tables.SnowflakeEntityClass
import com.github.mrgaabriel.ayla.tables.UserProfiles
import org.jetbrains.exposed.dao.EntityID

class UserProfile(id: EntityID<String>) : SnowflakeEntity(id) {
    companion object : SnowflakeEntityClass<UserProfile>(UserProfiles)

    var blacklisted by UserProfiles.blacklisted
    var blacklistReason by UserProfiles.blacklistReason
}