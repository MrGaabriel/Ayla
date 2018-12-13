package com.github.mrgaabriel.ayla.config

import com.github.mrgaabriel.ayla.utils.Static

class AylaConfig(
    val clientToken: String = "Client token do bot no Discord",
    val clientId: String = "ID do bot no Discord",
    val shardsCount: Int = -1, // Isto vai retornar o padrão de Shards recomendado pelo Discord
    val onlineStatus: String = "Online status do bot no Discord",
    val postgreIp: String = "IP do PostgreSQL",
    val postgrePort: Int = 5432,
    val postgreDatabaseName: String = "Nome da database do PostgreSQL",
    val postgreUsername: String = "Username do PostgreSQL",
    val postgrePassword: String = "Senha do PostgreSQL",
    val ownerIds: List<String> = listOf("ID do(s) dono(s) do bot"),
    val games: List<AylaGameWrapper> = listOf(AylaGameWrapper("Spotify", "LISTENING")),
    val websiteUrl: String = "URL do website",
    val websitePort: Int = 80,
    val websiteMasterToken: String = "Token mestre do website, usado para coisas *muito* importantes",
    val lavalinkHostname: String = "ws://127.0.0.1:2334",
    val lavalinkPassword: String = "Senha do Lavalink",
    val youtubeApiKey: String = "API key do YouTube"
) {

    class AylaGameWrapper(val name: String, val type: String)

    override fun toString() = Static.YAML_MAPPER.writeValueAsString(this)
}