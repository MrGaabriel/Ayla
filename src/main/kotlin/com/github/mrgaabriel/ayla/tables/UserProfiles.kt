package com.github.mrgaabriel.ayla.tables

object UserProfiles : SnowflakeTable() {

    val blacklisted = bool("blacklisted").default(false)
    val blacklistReason = text("blacklist_reason").nullable()
}