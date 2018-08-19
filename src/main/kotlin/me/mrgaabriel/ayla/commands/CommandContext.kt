package me.mrgaabriel.ayla.commands

import me.mrgaabriel.ayla.utils.*
import net.dv8tion.jda.core.*
import net.dv8tion.jda.core.entities.*
import java.time.*

class CommandContext(val message: Message,
                     val args: Array<String>,
                     val command: AbstractCommand) {

    val channel = message.channel
    val user = message.author

    val guild = message.guild

    fun sendMessage(message: Any): Message {
        return channel.sendMessage(message.toString()).complete()
    }

    fun sendMessage(message: String): Message {
        return channel.sendMessage(message).complete()
    }

    fun sendMessage(message: Message): Message {
        return channel.sendMessage(message).complete()
    }

    fun sendMessage(message: MessageEmbed): Message {
        return channel.sendMessage(message).complete()
    }

    fun sendMessage(message: MessageEmbed, content: String): Message {
        val builder = MessageBuilder()

        return channel.sendMessage(builder.setContent(content)
                .setEmbed(message)
                .build()).complete()
    }

    fun sendMessage(message: MessageEmbed, content: Any): Message {
        val builder = MessageBuilder()

        return channel.sendMessage(builder.setContent(content.toString())
                .setEmbed(message)
                .build()).complete()
    }

    fun getAsMention(withSpace: Boolean = true): String {
        return "${user.asMention}${if (withSpace) " " else ""}"
    }

    fun explain() {
        val builder = EmbedBuilder()
        val member = guild.getMember(user)

        val color = member.color

        val usedLabel = message.contentDisplay.split(" ")[0]

        var description = """
            ${this.command.description}


        """.trimIndent()

        if (this.command.usage.isNotEmpty()) {
            description += """
                :thinking: Como usar!?
                `$usedLabel ${this.command.usage}`


            """.trimIndent()
        }

        if (this.command.aliases.isNotEmpty()) {
            val notUsedAliases = this.command.aliases.filter { it != usedLabel }

            description += """
                :heavy_plus_sign: Alternativas
                ${notUsedAliases.joinToString(", ", transform={"`${guild.config.prefix}$it`"})}


            """.trimIndent()
        }

        builder.setDescription(description)

        builder.setAuthor(user.name, null, user.effectiveAvatarUrl)
        builder.setTitle(":thinking: `$usedLabel`")
        builder.setFooter(command.category.name.fancy, null)
        builder.setTimestamp(OffsetDateTime.now())

        builder.setColor(color)

        sendMessage(builder.build(), getAsMention())
    }
}