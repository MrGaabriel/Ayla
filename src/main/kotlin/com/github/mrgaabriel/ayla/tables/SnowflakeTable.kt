package com.github.mrgaabriel.ayla.tables

import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

open class SnowflakeTable(name: String = "", columnName: String = "id") : IdTable<String>(name) {
    override val id: Column<EntityID<String>> = varchar(columnName, 18).primaryKey().entityId()
}

abstract class SnowflakeEntity(id: EntityID<String>) : Entity<String>(id)

abstract class SnowflakeEntityClass<out E: Entity<String>>(table: IdTable<String>, entityType: Class<E>? = null) : EntityClass<String, E>(table, entityType)
