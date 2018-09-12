package me.mrgaabriel.ayla.modules

import com.mrpowergamerbr.temmiewebhook.DiscordMessage
import com.mrpowergamerbr.temmiewebhook.TemmieWebhook
import me.mrgaabriel.ayla.utils.config
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Message

object BadWordModule {

    fun handleMessage(message: Message) {
        val config = message.guild.config

        if (config.badWordsEnabled) {
            if (config.badWordsIgnoredChannels.contains(message.channel.id))
                return

            if (message.guild.selfMember.hasPermission(Permission.MESSAGE_MANAGE) && message.guild.selfMember.hasPermission(message.textChannel, Permission.MESSAGE_MANAGE)) {
                if (message.guild.selfMember.hasPermission(Permission.MANAGE_WEBHOOKS) && message.guild.selfMember.hasPermission(message.textChannel, Permission.MANAGE_WEBHOOKS)) {
                    message.textChannel.webhooks.queue({ webhooks ->
                        var content = message.contentDisplay

                        val args = message.contentDisplay
                                .split(" ")

                        val blacklistedWords = args.filter { config.badWords.contains(it.toLowerCase()) }
                        blacklistedWords.forEach { word ->
                            content = content.replace(word, "#".repeat(word.length))
                        }

                        if (blacklistedWords.isNotEmpty()) {
                            val webhook = webhooks.firstOrNull { it.name == "Bad Words Webhook" }
                                ?: message.textChannel.createWebhook("Bad Words Webhook").complete()
                            message.delete().queue()
                            val temmie = TemmieWebhook(webhook.url)

                            temmie.sendMessage(DiscordMessage.builder()
                                    .avatarUrl(message.author.effectiveAvatarUrl)
                                    .username(message.member.effectiveName)
                                    .content(content.replace("@", "@\u200B"))
                                    .build())
                        }
                    })
                } else {
                    config.badWords.forEach {
                        if (message.contentRaw.contains(it)) {
                            message.delete().queue()

                            message.channel.sendMessage("${message.author.asMention} Não fale isto aqui!").queue()
                        }
                    }
                }
            }
        }
    }
}