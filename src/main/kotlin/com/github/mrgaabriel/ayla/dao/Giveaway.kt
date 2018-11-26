package com.github.mrgaabriel.ayla.dao

import com.github.mrgaabriel.ayla.tables.Giveaways
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.EntityID

class Giveaway(id: EntityID<Long>) : Entity<Long>(id) {
    companion object : EntityClass<Long, Giveaway>(Giveaways)

    var messageId by Giveaways.messageId

    var guildId by Giveaways.guildId
    var channelId by Giveaways.channelId
    var authorId by Giveaways.authorId

    var prize by Giveaways.prize
    var users by Giveaways.users

    var endsAt by Giveaways.endsAt
}