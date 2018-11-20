package com.github.mrgaabriel.ayla.tables

import org.jetbrains.exposed.dao.IdTable
import org.jetbrains.exposed.sql.Table

object Guilds : SnowflakeTable() {

    val prefix = text("prefix")
}