package me.mrgaabriel.ayla.commands.config

import com.github.kevinsawicki.http.*
import com.github.salomonbrys.kotson.*
import com.google.gson.*
import me.mrgaabriel.ayla.commands.*
import me.mrgaabriel.ayla.utils.*
import net.dv8tion.jda.core.*

class RedditCommand : AbstractCommand() {

    init {
        this.label = "reddit"
        this.description = "Configura o módulo de sincronizar posts do Reddit em seu servidor"
        this.usage = "subreddit canal/off"

        this.memberPermissions = mutableListOf(Permission.MANAGE_SERVER)
        this.category = CommandCategory.CONFIG

    }

    override fun execute(context: CommandContext) {
        if (context.args.size != 2) {
            context.explain()
            return
        }

        val subreddit = context.args[0]

        val request = HttpRequest.get("https://reddit.com/r/$subreddit/.json")
                .userAgent(Constants.USER_AGENT)
                .body()
        val payload = JsonParser().parse(request).obj

        if (payload["error"] != null && payload["error"].int == 404 && payload["message"].string == "Not Found") {
            context.sendMessage(context.getAsMention(true) + "Sub-reddit `r/$subreddit` não encontrado!")
            return
        }

        val config = context.guild.config

        if (context.args[1] == "off") {
            if (config.redditSubs[subreddit] == null) {
                context.sendMessage(context.getAsMention(true) + "O sub-reddit `r/$subreddit` não está configurado para postar em nenhum canal!")
                return
            }

            config.redditSubs.remove(subreddit)
            config.lastRedditPostCreation.remove(subreddit)
            context.guild.config = config

            context.sendMessage(context.getAsMention(true) + "Sub-reddit `r/$subreddit` removido!")
            return
        }

        val channelId = context.args[1]
                .replace("<", "")
                .replace("#", "")
                .replace(">", "")

        if (context.guild.getTextChannelById(channelId) == null) {
            context.sendMessage(context.getAsMention(true) + "Canal inexistente!")
            return
        }

        config.redditSubs[subreddit] = channelId

        context.guild.config = config
        context.sendMessage(context.getAsMention(true) + "Agora as novidades do sub-reddit `r/$subreddit` serão postadas no canal <#${channelId}>!")
    }
}