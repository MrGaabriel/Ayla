package com.github.mrgaabriel.ayla.tables

import com.github.mrgaabriel.ayla.utils.exposed.array
import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.TextColumnType

object SubReddits : LongIdTable() {

    val name = text("name").index()

    val channels = array<String>("channels", TextColumnType()) // Canais em as novidades do sub-reddit serão notificadas
    val lastCommentCreation = long("last_comment_creation").default(0)
}