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
    val postgrePassword: String = "Senha do PostgreSQL"
) {

    override fun toString() = Static.YAML_MAPPER.writeValueAsString(this)
}