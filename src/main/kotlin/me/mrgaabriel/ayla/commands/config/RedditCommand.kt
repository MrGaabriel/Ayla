package me.mrgaabriel.ayla.commands.config

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.bool
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import me.mrgaabriel.ayla.utils.Constants
import me.mrgaabriel.ayla.utils.commands.AbstractCommand
import me.mrgaabriel.ayla.utils.commands.CommandCategory
import me.mrgaabriel.ayla.utils.commands.CommandContext
import me.mrgaabriel.ayla.utils.commands.annotations.ArgumentType
import me.mrgaabriel.ayla.utils.commands.annotations.InjectArgument
import me.mrgaabriel.ayla.utils.commands.annotations.Subcommand
import me.mrgaabriel.ayla.utils.commands.annotations.SubcommandPermissions
import me.mrgaabriel.ayla.utils.config
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.TextChannel
import kotlin.collections.set

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

        val about = HttpRequest.get("https://reddit.com/r/$subreddit/about.json")
                .userAgent(Constants.USER_AGENT)
                .body()
        val aboutPayload = JsonParser().parse(about).obj

        if (aboutPayload["error"] != null && aboutPayload["error"].int == 403 && aboutPayload["message"].string == "Forbidden") {
            context.sendMessage(context.getAsMention(true) + "Sub-reddit `r/$subreddit` é privada!")
            return
        } else if (aboutPayload["data"] != null && aboutPayload["data"].obj["over18"] != null && aboutPayload["data"]["over18"].bool == true) {
            context.sendMessage(context.getAsMention(true) + "Sub-reddit `r/$subreddit` é marcado como NSFW... e eu não quero postar este tipo de coisa por aqui.")
            return
        }

        val request = HttpRequest.get("https://reddit.com/r/$subreddit/.json")
                .userAgent(Constants.USER_AGENT)
                .body()
        val payload = JsonParser().parse(request).obj

        if (payload["error"] != null && payload["error"].int == 404 && payload["message"].string == "Not Found") {
            context.sendMessage(context.getAsMention(true) + "Sub-reddit `r/$subreddit` não encontrado!")
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