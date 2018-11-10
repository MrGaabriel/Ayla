package me.mrgaabriel.ayla.commands.`fun`

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.jsonObject
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
import me.mrgaabriel.ayla.utils.escapeMentions
import me.mrgaabriel.ayla.utils.onMessage
import me.mrgaabriel.ayla.utils.onReactionAdd
import java.net.URLEncoder

class SimSimiCommand : AbstractCommand("simsimi", category = CommandCategory.FUN, description = "Fale com o SimSimi") {

    @Subcommand
    fun simsimi(context: CommandContext, @InjectArgument(ArgumentType.ARGUMENT_LIST) question: String?) {
        if (context.args.isEmpty()) {
            context.explain()
            return
        }

        val questionEncoded = URLEncoder.encode(question)
        val request = HttpRequest.get("https://mrgaabriel.space/api/simsimi?question=$questionEncoded")
                .userAgent(Constants.USER_AGENT)
                .body()

        val payload = JsonParser().parse(request).obj

        val code = payload["api:code"].int
        if (code == 2) { // Não há respostas
            context.sendMessage(context.getAsMention(true) + "Não encontrei uma resposta para esta pergunta! Me ensine clicando na :bulb:!", { message ->
                message.addReaction("\uD83D\uDCA1").queue {
                    message.onReactionAdd(true) { event ->
                        if (event.user == context.user && event.reactionEmote.name == "\uD83D\uDCA1") {
                            message.delete().queue()

                            context.sendMessage(context.getAsMention(true) + "Digite o que você quer que o SimSimi responda quando alguém fale `$question`")

                            context.channel.onMessage(true) { event ->
                                if (event.author == context.user) {
                                    val request = HttpRequest.post("https://mrgaabriel.space/api/simsimi")
                                            .userAgent(Constants.USER_AGENT)
                                            .contentType("application/json")
                                            .send(jsonObject(
                                                    "question" to question,
                                                    "response" to event.message.contentDisplay
                                            ).toString())
                                            .ok()

                                    context.sendMessage(context.getAsMention(true) + "Agora, quando alguém falar `$question` com o simsimi, ele responderá com `${event.message.contentDisplay}`!")
                                }
                            }
                        }
                    }
                }
            })
        }

        if (code == 0) {
            val response = payload["response"].string

            context.sendMessage("<:simsimi:493853629766172672> **|** " + context.getAsMention(true) + response.escapeMentions(), { message ->
                message.addReaction("\uD83D\uDCA1").queue {
                    message.onReactionAdd(true) { event ->
                        if (event.user == context.user && event.reactionEmote.name == "\uD83D\uDCA1") {
                            message.delete().queue()

                            context.sendMessage(context.getAsMention(true) + "Digite o que você quer que o SimSimi responda quando alguém fale `$question`")

                            context.channel.onMessage(true) { event ->
                                if (event.author == context.user) {
                                    val request = HttpRequest.post("https://mrgaabriel.space/api/simsimi")
                                            .userAgent(Constants.USER_AGENT)
                                            .contentType("application/json")
                                            .send(jsonObject(
                                                    "question" to question,
                                                    "response" to event.message.contentDisplay
                                            ).toString())
                                            .ok()

                                    context.sendMessage(context.getAsMention(true) + "Agora, quando alguém falar `$question` com o simsimi, ele responderá com `${event.message.contentDisplay}`!")
                                }
                            }
                        }
                    }
                }
            })
        }
    }
}