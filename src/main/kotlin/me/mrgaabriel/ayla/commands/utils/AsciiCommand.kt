package me.mrgaabriel.ayla.commands.utils

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import me.mrgaabriel.ayla.utils.Constants
import me.mrgaabriel.ayla.utils.commands.AbstractCommand
import me.mrgaabriel.ayla.utils.commands.CommandCategory
import me.mrgaabriel.ayla.utils.commands.CommandContext
import me.mrgaabriel.ayla.utils.commands.annotations.ArgumentType
import me.mrgaabriel.ayla.utils.commands.annotations.InjectArgument
import me.mrgaabriel.ayla.utils.commands.annotations.Subcommand
import java.net.URLEncoder

class AsciiCommand : AbstractCommand("ascii", category = CommandCategory.UTILS, description = "Transforme textos em ASCII", usage = "texto") {

    @Subcommand
    fun ascii(context: CommandContext, @InjectArgument(ArgumentType.ARGUMENT_LIST) input: String?) {
        if (input == null) {
            return context.explain()
        }

        val request = HttpRequest.get("https://api.c2g.space/fun/asciify?text=${URLEncoder.encode(input)}")
                .userAgent(Constants.USER_AGENT)

        val payload = JsonParser().parse(request.body())
        val ascii = payload["text"].string

        context.sendMessage("${context.getAsMention()} ```$ascii```")
    }
}