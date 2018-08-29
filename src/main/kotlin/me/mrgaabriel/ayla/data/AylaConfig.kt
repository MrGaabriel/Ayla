package me.mrgaabriel.ayla.data

import me.mrgaabriel.ayla.threads.*

class AylaConfig(val clientToken: String,
                 val clientId: String,
                 val ownerId: String,
                 val shardCount: Int,
                 val games: List<AylaGameWrapper>,
                 val gistToken: String)