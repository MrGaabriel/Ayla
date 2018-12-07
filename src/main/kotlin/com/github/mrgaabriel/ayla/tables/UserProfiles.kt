package com.github.mrgaabriel.ayla.tables

import com.github.mrgaabriel.ayla.utils.exposed.SnowflakeTable

object UserProfiles : SnowflakeTable() {

    val blacklisted = bool("blacklisted").default(false)
    val blacklistReason = text("blacklist_reason").nullable()
}