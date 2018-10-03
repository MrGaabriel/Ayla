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
                 val mongoHostname: String = "Hostname do MongoDB",
                 val mongoDatabaseName: String = "Nome do banco de dados do MongoDB",
                 val youtubeApiKey: String = "API key do YouTube",
                 val botsOnDiscordToken: String = "Token do Bots on Discord",
                 val openWeatherMapKey: String = "Key do OpenWeatherMap")
