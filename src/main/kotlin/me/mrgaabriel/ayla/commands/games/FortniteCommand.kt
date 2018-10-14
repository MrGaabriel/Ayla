package me.mrgaabriel.ayla.commands.games

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import me.mrgaabriel.ayla.utils.AylaUtils
import me.mrgaabriel.ayla.utils.Constants
import me.mrgaabriel.ayla.utils.ayla
import me.mrgaabriel.ayla.utils.commands.AbstractCommand
import me.mrgaabriel.ayla.utils.commands.CommandCategory
import me.mrgaabriel.ayla.utils.commands.CommandContext
import me.mrgaabriel.ayla.utils.commands.annotations.Subcommand
import net.dv8tion.jda.core.EmbedBuilder
import java.net.URLEncoder

class FortniteCommand : AbstractCommand("fortnite", category = CommandCategory.GAMES, description = "Veja as informações de um jogador do Fortnite", usage = "(ps4|xbox|pc) usuário") {

    @Subcommand
    fun fortnite(context: CommandContext, platform: String?, player: String?) {
        if (platform == null || player == null) {
            return context.explain()
        }

        if (!platform.toLowerCase().matches(Regex("(xbox|ps4|pc)"))) {
            return context.explain()
        }

        val request = HttpRequest.get("https://api.fortnitetracker.com/v1/profile/${platform.toLowerCase()}/${URLEncoder.encode(player)}")
                .userAgent(Constants.USER_AGENT)
                .header("TRN-Api-Key", ayla.config.fortniteTrackerApiKey)

        val payload = JsonParser().parse(request.body()).obj

        val error = payload["error"].nullString
        if (error != null && error == "Player Not Found") {
            return context.sendMessage("${context.getAsMention()} Jogador não encontrado!")
        }

        val accountId = payload["accountId"].string
        val nickname = payload["epicUserHandle"].string
        val platformName = payload["platformNameLong"].string

        val stats = payload["lifeTimeStats"].array

        val wins = stats[8]["value"].string
        val kd = stats[11]["value"].string

        val matches = stats[7]["value"].string
        val winRatio = stats[9]["value"].string

        val kills = stats[10]["value"].string

        val builder = EmbedBuilder()

        builder.setAuthor("Fortnite", null, "https://cdn.discordapp.com/emojis/454697726358323200.png?v=1")

        builder.setTitle("Informações do jogador $nickname - Plataforma: $platformName")
        builder.setDescription("""
            **Partidas jogadas:** $matches

            **Vitórias:** $wins
            **% vitória:** $winRatio

            **Kills:** $kills
            **K/D:** $kd
        """.trimIndent())

        builder.setFooter("Account ID: $accountId", null)

        builder.setColor(AylaUtils.randomColor())

        context.sendMessage(builder.build(), context.getAsMention())
    }
}