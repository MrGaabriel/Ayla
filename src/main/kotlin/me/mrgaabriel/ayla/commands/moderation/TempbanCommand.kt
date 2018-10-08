package me.mrgaabriel.ayla.commands.moderation

import me.mrgaabriel.ayla.utils.AylaUtils
import me.mrgaabriel.ayla.utils.ayla
import me.mrgaabriel.ayla.utils.commands.AbstractCommand
import me.mrgaabriel.ayla.utils.commands.CommandCategory
import me.mrgaabriel.ayla.utils.commands.CommandContext
import me.mrgaabriel.ayla.utils.commands.annotations.ArgumentType
import me.mrgaabriel.ayla.utils.commands.annotations.InjectArgument
import me.mrgaabriel.ayla.utils.commands.annotations.Subcommand
import me.mrgaabriel.ayla.utils.commands.annotations.SubcommandPermissions
import me.mrgaabriel.ayla.utils.config
import me.mrgaabriel.ayla.utils.convertToEpochMillis
import me.mrgaabriel.ayla.utils.onMessage
import me.mrgaabriel.ayla.utils.onReactionAdd
import me.mrgaabriel.ayla.utils.saveConfig
import me.mrgaabriel.ayla.utils.tag
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.User
import java.awt.Color

class TempbanCommand : AbstractCommand("tempban", category = CommandCategory.MODERATION, description = "Bane um usuário temporariamente do servidor", usage = "usuário motivo") {

    @Subcommand
    @SubcommandPermissions(permissions = [Permission.BAN_MEMBERS], botPermissions = [Permission.BAN_MEMBERS])
    fun tempban(context: CommandContext, @InjectArgument(ArgumentType.USER) user: User?, @InjectArgument(ArgumentType.ARGUMENT_LIST) reason: String?) {
        if (context.args.isEmpty()) {
            return context.explain()
        }

        if (user == null) {
            return context.sendMessage("${context.getAsMention()} Usuário não encontrado!")
        }

        val effectiveReason = reason ?: "Sem motivo definido"

        val member = context.guild.getMember(user)
        if (member != null) {
            if (!context.guild.selfMember.canInteract(member)) {
                return context.sendMessage("${context.getAsMention()} Eu não posso punir este usuário, meus cargos são menores que os dele!")
            }

            if (!context.member.canInteract(member)) {
                return context.sendMessage("${context.getAsMention()} Você não pode punir este usuário, seus cargos são menores que os dele!")
            }
        }

        context.sendMessage("${context.getAsMention()} Até quando você quer que o usuário esteja banido? (Exemplos: `1 hora`, `07/10/2018 12:00`)")
        context.channel.onMessage { event ->
            if (event.author.id == context.user.id) {
                val millis = event.message.contentRaw.convertToEpochMillis()
                val formatted = AylaUtils.formatDateDiff(millis + 1) // millis + 1, porque o tempo passa ao mandar a mensagem e "1 hora" vira "59 minutos 59 segundos"

                if (millis <= System.currentTimeMillis()) {
                    context.sendMessage("${context.getAsMention()} Tempo inválido!")

                    ayla.messageInteractionCache.remove(context.channel.id)
                    return@onMessage
                }

                context.sendMessage("${context.getAsMention()} Você tem certeza que quer punir o usuário ${user.asMention} (`${user.tag} - ${user.id}`) do seu servidor pelo motivo `$effectiveReason`, por `$formatted`? Clique no :white_check_mark: para confirmar e no :no_good: para cancelar!") { message ->
                    message.addReaction("✅").queue()
                    message.addReaction("\uD83D\uDE45").queue()

                    message.onReactionAdd { event ->
                        when (event.reactionEmote.name) {
                            "✅" -> {
                                val config = context.guild.config
                                val userData = config.getUserData(user)

                                if (context.guild.isMember(user) && (!user.isBot && !user.isFake)) {
                                    val builder = EmbedBuilder()

                                    builder.setTitle("Você foi banido temporariamente do servidor ${context.guild.name}!")
                                    builder.setAuthor(context.user.tag, null, context.user.effectiveAvatarUrl)
                                    builder.setThumbnail(context.user.effectiveAvatarUrl)

                                    builder.addField("Quem puniu", context.user.tag, true)
                                    builder.addField("Motivo", reason ?: "Sem motivo definido", true)
                                    builder.addField("Tempo", formatted, true)

                                    builder.setFooter("Esta mensagem foi enviada por um bot, então não responda!", null)
                                    builder.setColor(Color.RED)

                                    user.openPrivateChannel().queue({
                                        it.sendMessage(builder.build()).queue()
                                    }, {})
                                }

                                context.guild.controller.ban(user.id, 0, "Punido por ${context.user.tag} - Motivo: $reason").queue()

                                userData.banned = true
                                userData.bannedUntil = millis

                                config.saveUserData(userData)

                                context.guild.saveConfig(config)
                                message.delete().queue()

                                ayla.messageInteractionCache.remove(context.channel.id)

                                context.sendMessage("${context.getAsMention()} Usuário punido com sucesso!")
                            }

                            "\uD83D\uDE45" -> {
                                message.delete().queue()
                                ayla.messageInteractionCache.remove(context.channel.id)

                                context.sendMessage("${context.getAsMention()} Punição cancelada!")
                            }
                        }
                    }
                }
            }
        }
    }
}