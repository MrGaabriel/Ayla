package me.mrgaabriel.ayla.utils.commands

import me.mrgaabriel.ayla.utils.ayla
import me.mrgaabriel.ayla.utils.config
import me.mrgaabriel.ayla.utils.fancy
import me.mrgaabriel.ayla.utils.isValidSnowflake
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.User
import java.time.OffsetDateTime

class CommandContext(val message: Message,
                     val args: Array<String>,
                     val command: AbstractCommand) {

    val channel = message.channel
    val user = message.author

    val guild = message.guild

    fun sendMessage(content: Any, success: ((Message) -> Unit)? = null) {
        channel.sendMessage(content.toString()).queue({ message ->
            if (success != null) {
                success.invoke(message)
            }
        })

    }

    fun sendMessage(content: String, success: ((Message) -> Unit)? = null) {
        channel.sendMessage(content).queue({ message ->
            if (success != null) {
                success.invoke(message)
            }
        })
    }

    fun sendMessage(content: Message, success: ((Message) -> Unit)? = null) {
        channel.sendMessage(content).queue({ message ->
            if (success != null) {
                success.invoke(message)
            }
        })
    }

    fun sendMessage(embed: MessageEmbed, success: ((Message) -> Unit)? = null) {
        channel.sendMessage(embed).queue({ message ->
            if (success != null) {
                success.invoke(message)
            }
        })
    }

    fun sendMessage(embed: MessageEmbed, content: String, success: ((Message) -> Unit)? = null) {
        val builder = MessageBuilder()

        return channel.sendMessage(builder.setContent(content)
                .setEmbed(embed)
                .build()).queue({ message ->
            if (success != null) {
                success.invoke(message)
            }
        })
    }

    fun sendMessage(embed: MessageEmbed, content: Any, success: ((Message) -> Unit)? = null) {
        val builder = MessageBuilder()

        channel.sendMessage(builder.setContent(content.toString())
                .setEmbed(embed)
                .build()).queue({ message ->
            if (success != null) {
                success.invoke(message)
            }
        })
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

    fun getTextChannel(input: String?): TextChannel? {
        if (input == null)
            return null

        val arg = input

        val channels = guild.getTextChannelsByName(arg, false)
        if (channels.isNotEmpty()) {
            return channels.get(0)
        }

        val id = arg
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

    fun getTextChannelAt(argument: Int): TextChannel? {
        if (args.size - 1 < argument) {
            return null
        }

        val arg = args[argument]

        val channels = guild.getTextChannelsByName(arg, false)
        if (channels.isNotEmpty()) {
            return channels.get(0)
        }

        val id = arg
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

    fun getUser(input: String?): User? {
        if (input == null)
            return null

        val arg = input

        val splitted = arg.split("#")
        if (splitted.size == 2) {
            val users = mutableListOf<User>()
            ayla.shards.forEach { users.addAll(it.getUsersByName(splitted[0], true)) }

            val matchedUser = users.stream().filter { it.discriminator == splitted[1] }.findFirst()

            if (matchedUser.isPresent) {
                return matchedUser.get()
            }
        }

        val members = guild.getMembersByEffectiveName(arg, true)
        if (members.isNotEmpty()) {
            return members.get(0).user
        }

        val users = mutableListOf<User>()
        ayla.shards.forEach { users.addAll(it.getUsersByName(arg, true)) }

        if (users.isNotEmpty()) {
            return users.get(0)
        }

        val id = arg.replace("<", "")
                .replace("@", "")
                .replace("!", "")
                .replace(">", "") // Se for uma menção, retirar <, @, ! e >

        if (!id.isValidSnowflake())
            return null

        val user = ayla.getUserById(id)

        if (user != null) {
            return user
        }

        return null
    }

    fun getUserAt(argument: Int): User? {
        if (args.size - 1 < argument) {
            return null
        }

        val arg = args[argument]

        val splitted = arg.split("#")
        if (splitted.size == 2) {
            val users = mutableListOf<User>()
            ayla.shards.forEach { users.addAll(it.getUsersByName(splitted[0], true)) }

            val matchedUser = users.stream().filter { it.discriminator == splitted[1] }.findFirst()

            if (matchedUser.isPresent) {
                return matchedUser.get()
            }
        }

        val members = guild.getMembersByEffectiveName(arg, true)
        if (members.isNotEmpty()) {
            return members.get(0).user
        }

        val users = mutableListOf<User>()
        ayla.shards.forEach { users.addAll(it.getUsersByName(arg, true)) }

        if (users.isNotEmpty()) {
            return users.get(0)
        }

        val id = arg.replace("<", "")
                .replace("@", "")
                .replace("!", "")
                .replace(">", "") // Se for uma menção, retirar <, @, ! e >

        if (!id.isValidSnowflake())
            return null

        val user = ayla.getUserById(id)

        if (user != null) {
            return user
        }

        return null
    }
}