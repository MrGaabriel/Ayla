package me.mrgaabriel.ayla.commands.utils

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.mrgaabriel.ayla.utils.AylaUtils
import me.mrgaabriel.ayla.utils.ayla
import me.mrgaabriel.ayla.utils.commands.AbstractCommand
import me.mrgaabriel.ayla.utils.commands.CommandCategory
import me.mrgaabriel.ayla.utils.commands.CommandContext
import me.mrgaabriel.ayla.utils.commands.annotations.Subcommand
import net.dv8tion.jda.core.EmbedBuilder

class PingCommand : AbstractCommand("ping", CommandCategory.UTILS, "Verifica a conexão do bot com os servidores do Discord") {

    @Subcommand
    suspend fun ping(context: CommandContext, arg: String?) {
        GlobalScope.launch {
            if (arg != null && arg == "shards") {
                val builder = EmbedBuilder()

                builder.setColor(AylaUtils.randomColor())

                val shards = buildString {
                    for (shard in ayla.shardManager.shards.sortedBy { it.shardInfo.shardId }) {
                        appendln("**SHARD #${shard.shardInfo.shardId}:** ${shard.ping}ms - (${shard.users.size} usuários, ${shard.guilds.size} guilds)")
                    }
                }

                builder.setDescription("""
                    $shards
                    **Média de ping:** ${ayla.shardManager.averagePing}ms
                """.trimIndent())

                context.sendMessage(builder.build())
                return@launch
            }

            val start = System.currentTimeMillis()
            val message = context.sendMessageAsync("Calculando...")

            message.editMessage("${context.getAsMention()} **Pong!** \uD83C\uDFD3\nWebSocket: `${context.jda.ping}ms` - API: `${System.currentTimeMillis() - start}ms`").queue()
        }
    }
}