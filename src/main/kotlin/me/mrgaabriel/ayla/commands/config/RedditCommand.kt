package me.mrgaabriel.ayla.commands.config

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.nullInt
import com.github.salomonbrys.kotson.obj
import com.google.gson.JsonParser
import me.mrgaabriel.ayla.data.AylaGuildConfig
import me.mrgaabriel.ayla.utils.Constants
import me.mrgaabriel.ayla.utils.ayla
import me.mrgaabriel.ayla.utils.commands.AbstractCommand
import me.mrgaabriel.ayla.utils.commands.CommandCategory
import me.mrgaabriel.ayla.utils.commands.CommandContext
import me.mrgaabriel.ayla.utils.commands.annotations.Subcommand
import me.mrgaabriel.ayla.utils.commands.annotations.SubcommandPermissions
import me.mrgaabriel.ayla.utils.config
import me.mrgaabriel.ayla.utils.onMessage
import me.mrgaabriel.ayla.utils.onReactionAdd
import me.mrgaabriel.ayla.utils.saveConfig
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import java.net.URLEncoder

class RedditCommand : AbstractCommand("reddit",  category = CommandCategory.CONFIG, description = "Configura o módulo de sincronizar posts do Reddit em seu servidor", usage = "subreddit (canal/off)") {

    @Subcommand
    @SubcommandPermissions([Permission.MANAGE_SERVER])
    fun reddit(context: CommandContext) {
        val config = context.guild.config

        val builder = EmbedBuilder()

        builder.setAuthor(context.guild.name, null, context.guild.iconUrl)
        builder.setTitle("Configuração do módulo Reddit")

        builder.setColor(Constants.REDDIT_ORANGE_RED)
        builder.setDescription(        """
            Configure o módulo para interagir com o [Reddit](https://reddit.com/) e notificar seu servidor sobre os novos posts em um sub-reddit

            ➕ **»** Adiciona um sub-reddit
            ➖ **»** Remove um sub-reddit
        """.trimIndent())

        context.sendMessage(builder.build(), context.getAsMention()) { message ->
            message.addReaction("➕").queue()
            message.addReaction("➖").queue()

            message.onReactionAdd { event ->
                if (event.user.id == context.user.id) {
                    when (event.reactionEmote.name) {
                        "➕" -> {
                            message.delete().queue()

                            context.sendMessage("${context.getAsMention()} Digite o nome do sub-reddit que você deseja adicionar!")
                            context.channel.onMessage { event ->
                                if (event.author.id == context.user.id) {
                                    val sub = event.message.contentRaw

                                    val about = HttpRequest.get("https://reddit.com/r/${URLEncoder.encode(sub)}/about.json")
                                            .userAgent(Constants.USER_AGENT)
                                            .body()

                                    val payload = JsonParser().parse(about).obj
                                    val error = payload["error"].nullInt

                                    if (error != null) {
                                        if (error == 403) {
                                            ayla.messageInteractionCache.remove(context.channel.id)

                                            return@onMessage context.sendMessage("${context.getAsMention()} Este sub-reddit é privado!")
                                        }

                                        if (error == 404) {
                                            ayla.messageInteractionCache.remove(context.channel.id)

                                            return@onMessage context.sendMessage("${context.getAsMention()} Este sub-reddit não existe!")
                                        }


                                    }

                                    context.sendMessage("${context.getAsMention()} Digite o canal que você quer que as novidades do sub-reddit sejam postadas!")

                                    ayla.messageInteractionCache.remove(context.channel.id)
                                    context.channel.onMessage { event ->
                                        if (event.author.id == context.user.id) {
                                            val channel = context.getTextChannel(event.message.contentRaw)

                                            if (channel == null) {
                                                context.sendMessage("${context.getAsMention()} Canal inexistente!")

                                                ayla.messageInteractionCache.remove(context.channel.id)
                                                return@onMessage
                                            }

                                            val subredditWrapepr = AylaGuildConfig.SubRedditWrapper(sub, channel.id)

                                            config.redditSubs.add(subredditWrapepr)
                                            context.guild.saveConfig(config)

                                            context.sendMessage("${context.getAsMention()} Agora as novidades do sub-reddit `/r/$sub` serão notificadas no canal ${channel.asMention}")

                                            ayla.messageInteractionCache.remove(context.channel.id)
                                        }
                                    }
                                }
                            }
                        }

                        "➖" -> {
                            message.delete().queue()
                            context.sendMessage("${context.getAsMention()} Digite o nome do sub-reddit que você quer parar de ser notificado!")

                            context.channel.onMessage { event ->
                                if (event.author.id == context.user.id) {
                                    val sub = event.message.contentRaw.toLowerCase()

                                    config.redditSubs.filter { it.subReddit.toLowerCase() == sub }.forEach {
                                        config.redditSubs.remove(it)
                                    }

                                    context.sendMessage("${context.getAsMention()} Agora eu não vou mais enviar as novidades do sub-reddit `/r/$sub`!")
                                    ayla.messageInteractionCache.remove(context.channel.id)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}