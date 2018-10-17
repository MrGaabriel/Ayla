package me.mrgaabriel.ayla.commands.`fun`

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import me.mrgaabriel.ayla.utils.AylaUtils
import me.mrgaabriel.ayla.utils.Constants
import me.mrgaabriel.ayla.utils.commands.AbstractCommand
import me.mrgaabriel.ayla.utils.commands.CommandCategory
import me.mrgaabriel.ayla.utils.commands.CommandContext
import me.mrgaabriel.ayla.utils.commands.annotations.Subcommand
import net.dv8tion.jda.core.EmbedBuilder
import java.net.URL
import java.time.OffsetDateTime
import javax.imageio.ImageIO

class RandomFoxCommand : AbstractCommand("randomfox", category = CommandCategory.FUN, description = "Veja a imagem de uma raposa aleatória") {

    @Subcommand
    fun randomFox(context: CommandContext) {
        val builder = EmbedBuilder()

        builder.setTimestamp(OffsetDateTime.now())
        builder.setFooter("Powered by randomfox.ca", null)

        builder.setImage(getRandomFox())

        builder.setColor(AylaUtils.randomColor())

        context.sendMessage(builder.build(), context.getAsMention())
    }

    fun getRandomFox(): String {
        val request = HttpRequest.get("https://randomfox.ca/floof")
                .userAgent(Constants.USER_AGENT)
                .body()

        val payload = JsonParser().parse(request).obj
        val link = payload["image"].string.toLowerCase()

        // Somente ".png" e ".jpg", ou seja, imagens válidas
        if (!link.endsWith(".png") && !link.endsWith(".jpg")) {
            return getRandomFox()
        }

        // Se mesmo assim, não retornar uma imagem válida, tente pegar outra!
        if (ImageIO.read(URL(link)) == null) {
            return getRandomFox()
        }

        return link
    }
}