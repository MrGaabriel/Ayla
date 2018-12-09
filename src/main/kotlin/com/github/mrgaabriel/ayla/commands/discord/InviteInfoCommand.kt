package com.github.mrgaabriel.ayla.commands.discord

import com.github.mrgaabriel.ayla.commands.AbstractCommand
import com.github.mrgaabriel.ayla.commands.CommandContext
import com.github.mrgaabriel.ayla.utils.extensions.await
import com.github.mrgaabriel.ayla.utils.humanize
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Invite
import net.dv8tion.jda.core.exceptions.ErrorResponseException
import java.awt.Color

class InviteInfoCommand : AbstractCommand("inviteinfo") {

    override fun getDescription(): String {
        return "Pega as informações de um invite do Discord"
    }

    override fun getUsage(): String {
        return "código do invite"
    }

    override suspend fun run(context: CommandContext) {
        if (context.args.isEmpty()) {
            context.explain()
            return
        }

        val code = context.args[0].replace(
            Regex("(http(s)?://)?(discord.gg/|discordapp.com/invite/|discord.com/invite/)"),
            ""
        )

        try {
            val invite = Invite.resolve(context.event.jda, code, true).await()

            val guild = invite.guild

            val builder = EmbedBuilder()

            builder.setTitle(":small_blue_diamond: Convite \"$code\" - ${guild.name}")

            builder.setThumbnail(guild.iconUrl)
            if (guild.splashUrl != null) {
                builder.setImage("${guild.splashUrl}?size=2048")
            }

            builder.addField("\uD83D\uDCBB ID do servidor", guild.id, true)
            builder.addField("\uD83D\uDC65 Membros", "**Total:** ${guild.memberCount}\n**Online:** ${guild.onlineCount}\n**Offline:** ${guild.memberCount - guild.onlineCount}", true)
            builder.addField("\uD83D\uDE94 Nível de verificação", guild.verificationLevel.name, true)

            if (guild.features.isNotEmpty())
                builder.addField("✨ Features exclusivas", guild.features.joinToString(", "), true)

            builder.addField("\uD83D\uDEAA Canal de boas-vindas", "#${invite.channel.name} - `${invite.channel.id}`", true)

            builder.setColor(Color(114, 137, 218))

            context.sendMessage(builder.build(), context.event.author.asMention)
        } catch (e: ErrorResponseException) {
            val errCode = e.errorCode

            if (errCode == 10006) {
                context.sendMessage("${context.event.author.asMention} Convite `$code` é inválido!")
            }
        }
    }
}