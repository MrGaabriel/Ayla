package com.github.mrgaabriel.ayla.dao

import com.github.mrgaabriel.ayla.tables.SubReddits
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass

class SubReddit(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<SubReddit>(SubReddits)

    var name by SubReddits.name

    var channels by SubReddits.channels
    var lastCommentCreation by SubReddits.lastCommentCreation
}