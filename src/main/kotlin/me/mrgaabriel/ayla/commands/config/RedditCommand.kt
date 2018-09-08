package me.mrgaabriel.ayla.commands.config

import com.github.kevinsawicki.http.*
import com.github.salomonbrys.kotson.*
import com.google.gson.*
import me.mrgaabriel.ayla.utils.*
import me.mrgaabriel.ayla.utils.commands.*
import me.mrgaabriel.ayla.utils.commands.annotations.*
import net.dv8tion.jda.core.*
import net.dv8tion.jda.core.entities.TextChannel

class RedditCommand : AbstractCommand(
        "reddit",
        CommandCategory.CONFIG,
        "Configura o módulo de sincronizar posts do Reddit em seu servidor",
        "subreddit (canal/off)"
) {

    @Subcommand()
    @SubcommandPermissions([Permission.MANAGE_SERVER])
    fun onExecute(context: CommandContext, subreddit: String, @InjectArgument(ArgumentType.TEXT_CHANNEL) textChannel: TextChannel?) {
        if (context.args.size != 2) {
            context.explain()
            return
        }

        val request = HttpRequest.get("https://reddit.com/r/$subreddit/about.json")
                .userAgent(Constants.USER_AGENT)
                .body()
        val payload = JsonParser().parse(request).obj

        if (payload["error"] != null && payload["error"].int == 404 && payload["message"].string == "Not Found") {
            context.sendMessage(context.getAsMention(true) + "Sub-reddit `r/$subreddit` não encontrado!")
            return
        } else if (payload["error"] != null && payload["error"].int == 403 && payload["message"].string == "Forbidden") {
            context.sendMessage(context.getAsMention(true) + "Sub-reddit `r/$subreddit` é privada!")
            return
        } else if (payload["data"] != null && payload["data"]["over18"].bool == true) {
            context.sendMessage(context.getAsMention(true) + "Sub-reddit `r/$subreddit` é marcado como NSFW... e eu não quero postar este tipo de coisa por aqui.")
            return
        }

        val config = context.guild.config

        if (textChannel == null) {
            context.sendMessage(context.getAsMention(true) + "Canal inexistente!")
            return
        }

        config.redditSubs[subreddit] = textChannel.id

        context.guild.config = config
        context.sendMessage(context.getAsMention(true) + "Agora as novidades do sub-reddit `r/$subreddit` serão postadas no canal ${textChannel.asMention}!")
    }

    @Subcommand(["off"])
    @SubcommandPermissions([Permission.MANAGE_SERVER])
    fun onOff(context: CommandContext, subreddit: String) {
        val config = context.guild.config

        if (config.redditSubs[subreddit] == null) {
            context.sendMessage(context.getAsMention(true) + "O sub-reddit `r/$subreddit` não está configurado para postar em nenhum canal!")
            return
        }

        config.redditSubs.remove(subreddit)
        config.lastRedditPostCreation.remove(subreddit)
        context.guild.config = config

        context.sendMessage(context.getAsMention(true) + "Sub-reddit `r/$subreddit` removido!")
    }
}