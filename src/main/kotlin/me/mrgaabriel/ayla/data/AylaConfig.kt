package me.mrgaabriel.ayla.data

import me.mrgaabriel.ayla.threads.AylaGameWrapper

class AylaConfig(val clientToken: String,
                 val clientId: String,
                 val ownerId: String,
                 val shardCount: Int,
                 val games: List<AylaGameWrapper>,
                 val gistToken: String,
                 val vespertineBotsToken: String)