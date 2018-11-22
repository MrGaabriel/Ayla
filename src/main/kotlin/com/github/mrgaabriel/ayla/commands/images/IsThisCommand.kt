package com.github.mrgaabriel.ayla.commands.images

import com.github.mrgaabriel.ayla.commands.AbstractCommand
import com.github.mrgaabriel.ayla.commands.CommandContext
import com.github.mrgaabriel.ayla.utils.AylaUtils
import com.github.mrgaabriel.ayla.utils.extensions.tag
import com.github.mrgaabriel.ayla.utils.makeRoundedCorner
import com.github.mrgaabriel.ayla.utils.resize
import java.awt.Color
import java.awt.Font
import java.io.File
import javax.imageio.ImageIO

class IsThisCommand : AbstractCommand("isthis", aliases = listOf("isthat")) {

    override fun getDescription(): String {
        return "Is this a butterfly?"
    }

    override suspend fun run(context: CommandContext) {
        if (context.args.isEmpty()) {
            context.explain()
            return
        }

        val user = AylaUtils.getUser(context.args[0], context.event.guild!!)
        if (user == null) {
            context.sendMessage("${context.event.author.asMention} Usuário não encontrado!")
            return
        }

        context.args.removeAt(0)
        val text = context.args.joinToString(" ")

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
        val article = "a${if (vowels.any { text[0] == it }) "n" else ""}"

        graphics.drawString("Is this $article $text?", 0, 150)

        context.sendFile(image, "isthis.png", context.event.author.asMention)
    }
}