package me.mrgaabriel.ayla.data

import me.mrgaabriel.ayla.threads.AylaGameWrapper

class AylaConfig(val clientToken: String = "Token do Bot",
                 val clientId: String = "Client ID do Bot",
                 val ownerId: String = "ID do dono do Bot",
                 val shardCount: Int = 1,
                 val games: List<AylaGameWrapper> = listOf(),
                 val gistToken: String = "Token do GitHub",
                 val vespertineBotsToken: String = "Token do Vespertine's Bot List",
                 val discordBotListToken: String = "Token do Discord Bot List",
                 val mongoDatabase: String = "Nome da database do MongoDB")