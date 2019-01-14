package com.github.mrgaabriel.ayla.commands.discord

import com.github.mrgaabriel.ayla.commands.*
import com.github.mrgaabriel.ayla.utils.extensions.await
import com.github.mrgaabriel.ayla.utils.humanize
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Invite
import net.dv8tion.jda.core.exceptions.ErrorResponseException
import net.perfectdreams.commands.annotation.Subcommand
import java.awt.Color

class InviteInfoCommand : AylaCommand("inviteinfo") {

    override val description: String
        get() = "Pega as informações de um invite do Discord"

    override val usage: String
        get() = "invite"

    override val category: CommandCategory
        get() = CommandCategory.DISCORD

    @Subcommand
    suspend fun root(context: AylaCommandContext) {
        context.explain()
    }

    @Subcommand
    suspend fun inviteinfo(context: AylaCommandContext, invite: String) {
        val code = invite.replace(
            Regex("(http(s)?://)?(discord.gg/|discordapp.com/invite/|discord.com/invite/)"),
            ""
        )

        try {
            val inviteObj = Invite.resolve(context.event.jda, code, true).await()

            val guild = inviteObj.guild

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

            builder.addField("\uD83D\uDEAA Canal de boas-vindas", "#${inviteObj.channel.name} - `${inviteObj.channel.id}`", true)

            builder.setColor(Color(114, 137, 218))

            context.reply(builder.build())
        } catch (e: ErrorResponseException) {
            val errCode = e.errorCode

            if (errCode == 10006) {
                context.reply("Convite `$code` é inválido!")
            }
        }
    }
}