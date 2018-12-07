package com.github.mrgaabriel.ayla.utils

import com.github.ajalt.mordant.TermColors
import com.github.mrgaabriel.ayla.utils.extensions.await
import com.github.mrgaabriel.ayla.utils.extensions.ayla
import com.github.mrgaabriel.ayla.utils.extensions.isValidSnowflake
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.User
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Image
import java.awt.RenderingHints
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.net.URL
import javax.imageio.ImageIO

fun <R : Any> R.logger(): Lazy<Logger> {
    return lazy { LoggerFactory.getLogger(getClassName(this.javaClass)) }
}

fun <T : Any> getClassName(clazz: Class<T>): String {
    return clazz.name.removeSuffix("\$Companion")
}

fun BufferedImage.resize(newW: Int, newH: Int): BufferedImage {
    val tmp = this.getScaledInstance(newW, newH, Image.SCALE_SMOOTH)
    val dimg = BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB)

    val g2d = dimg.createGraphics()
    g2d.drawImage(tmp, 0, 0, null)
    g2d.dispose()

    return dimg
}

fun BufferedImage.makeRoundedCorner(cornerRadius: Int): BufferedImage {
    val w = this.width
    val h = this.height
    val output = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
    val g2 = output.createGraphics()
    // This is what we want, but it only does hard-clipping, i.e. aliasing
    // g2.setClip(new RoundRectangle2D ...)
    // so instead fake soft-clipping by first drawing the desired clip shape
    // in fully opaque white with antialiasing enabled...
    g2.composite = AlphaComposite.Src
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g2.color = Color.WHITE
    g2.fill(RoundRectangle2D.Float(0f, 0f, w.toFloat(), h.toFloat(), cornerRadius.toFloat(), cornerRadius.toFloat()))
    // ... then compositing the image on top,
    // using the white shape from above as alpha source
    g2.composite = AlphaComposite.SrcAtop
    g2.drawImage(this, 0, 0, null)
    g2.dispose()
    return output
}

val t = TermColors()

object AylaUtils {

    fun getTextChannel(input: String?, guild: Guild): TextChannel? {
        if (input == null)
            return null
        val channels = guild.getTextChannelsByName(input, false)
        if (channels.isNotEmpty()) {
            return channels[0]
        }
        val id = input
            .replace("<", "")
            .replace("#", "")
            .replace(">", "")
        if (!id.isValidSnowflake())
            return null
        val channel = guild.getTextChannelById(id)
        if (channel != null) {
            return channel
        }
        return null
    }

    suspend fun getUser(input: String?, guild: Guild): User? {
        if (input == null)
            return null
        val splitted = input.split("#")
        if (splitted.size == 2) {
            val users = mutableListOf<User>()
            ayla.shardManager.shards.forEach { users.addAll(it.getUsersByName(splitted[0], true)) }
            val matchedUser = users.stream().filter { it.discriminator == splitted[1] }.findFirst()
            if (matchedUser.isPresent) {
                return matchedUser.get()
            }
        }
        val members = guild.getMembersByEffectiveName(input, true)
        if (members.isNotEmpty()) {
            return members[0].user
        }
        val users = mutableListOf<User>()
        ayla.shardManager.shards.forEach { users.addAll(it.getUsersByName(input, true)) }
        if (users.isNotEmpty()) {
            return users[0]
        }
        val id = input.replace("<", "")
            .replace("@", "")
            .replace("!", "")
            .replace(">", "") // Se for uma menção, retirar <, @, ! e >
        if (!id.isValidSnowflake())
            return null
        val user = ayla.shardManager.retrieveUserById(id).await()
        if (user != null) {
            return user
        }
        return null
    }

    fun downloadImage(url: String): BufferedImage? {
        try {
            val conn = URL(url).openConnection()

            conn.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:65.0) Gecko/20100101 Firefox/65.0"
            )

            val image = ImageIO.read(conn.getInputStream())
            return image
        } catch (e: Exception) {
            return null
        }
    }

    fun getTimestamp(milis: Long): String {
        var seconds = milis / 1000
        val hours = Math.floorDiv(seconds, 3600)
        seconds = seconds - hours * 3600
        val mins = Math.floorDiv(seconds, 60)
        seconds = seconds - mins * 60
        return (if (hours == 0L) "" else hours.toString() + ":") + String.format("%02d", mins) + ":" + String.format("%02d", seconds)
    }

    fun isValidImage(url: String): Boolean {
        try {
            val conn = URL(url).openConnection()
            conn.setRequestProperty("User-Agent", Constants.USER_AGENT)

            val image = ImageIO.read(conn.getInputStream())

            return image != null
        } catch (e: Exception) {
            return false
        }
    }
}