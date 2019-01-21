package com.github.mrgaabriel.ayla.commands.images

import com.github.mrgaabriel.ayla.commands.*
import com.github.mrgaabriel.ayla.utils.AylaUtils
import com.github.mrgaabriel.ayla.utils.annotation.InjectParameterType
import com.github.mrgaabriel.ayla.utils.annotation.ParameterType
import com.github.mrgaabriel.ayla.utils.extensions.tag
import com.github.mrgaabriel.ayla.utils.makeRoundedCorner
import com.github.mrgaabriel.ayla.utils.resize
import net.dv8tion.jda.core.entities.User
import net.perfectdreams.commands.annotation.Subcommand
import java.awt.Color
import java.awt.Font
import java.io.File
import javax.imageio.ImageIO
import kotlin.contracts.ExperimentalContracts

class IsThisCommand : AylaCommand("isthis", "isthat") {

    override val description: String
        get() = "Cria uma mensagem do meme \"is this a butterfly?\""

    override val category: CommandCategory
        get() = CommandCategory.IMAGES

    override val usage: String
        get() = "usuário frase"

    @Subcommand
    suspend fun root(context: AylaCommandContext) {
        context.explain()
    }

    @Subcommand
    @ExperimentalContracts
    suspend fun isthis(context: AylaCommandContext, user: User?, @InjectParameterType(ParameterType.ARGUMENT_LIST) text: String) {
        notNull(user, "Usuário não encontrado!")

        val file = File("assets", "isthis.png")
        val image = ImageIO.read(file)

        val graphics = image.createGraphics()

        val avatar = AylaUtils.downloadImage(user.effectiveAvatarUrl)
            ?: throw RuntimeException("Avatar is null")

        graphics.drawImage(avatar.resize(750, 800).makeRoundedCorner(700), 80, 270, null)

        graphics.font = Font.createFont(0, File("assets", "bebas_neue.ttf"))
            .deriveFont(100f)
        graphics.color = Color.BLACK
        graphics.drawString(user.tag, 250, 1150)

        graphics.font = Font.createFont(0, File("assets", "bebas_neue.ttf"))
            .deriveFont(200f)
        graphics.color = Color.WHITE

        val vowels = listOf('a', 'e', 'i', 'o', 'u')
        val article = "a${if (vowels.any { text[0].equals(it, true) }) "n" else ""}"

        graphics.drawString("Is this $article $text?", 0, 150)

        context.sendFile(image, "isthis.png", context.event.author.asMention)
    }
}