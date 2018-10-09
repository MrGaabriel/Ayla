package me.mrgaabriel.ayla.commands.moderation

import me.mrgaabriel.ayla.utils.commands.AbstractCommand
import me.mrgaabriel.ayla.utils.commands.CommandCategory
import me.mrgaabriel.ayla.utils.commands.CommandContext
import me.mrgaabriel.ayla.utils.commands.annotations.ArgumentType
import me.mrgaabriel.ayla.utils.commands.annotations.InjectArgument
import me.mrgaabriel.ayla.utils.commands.annotations.Subcommand
import me.mrgaabriel.ayla.utils.commands.annotations.SubcommandPermissions
import me.mrgaabriel.ayla.utils.onReactionAdd
import me.mrgaabriel.ayla.utils.tag
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.User
import java.awt.Color

class KickCommand : AbstractCommand("kick", category = CommandCategory.MODERATION, description = "Expulsa membros do seu servidor", usage = "usuário motivo", aliases = listOf("expulsar", "kickar")) {

    @Subcommand
    @SubcommandPermissions(permissions = [Permission.KICK_MEMBERS], botPermissions = [Permission.KICK_MEMBERS])
    fun kick(context: CommandContext, @InjectArgument(ArgumentType.USER) user: User?, @InjectArgument(ArgumentType.ARGUMENT_LIST) reason: String?) {
        if (context.args.isEmpty()) {
            return context.explain()
        }

        if (user == null) {
            return context.sendMessage("${context.getAsMention()} Usuário não encontrado!")
        }

        val effectiveReason = reason ?: "Sem motivo definido"

        val member = context.guild.getMember(user)
        if (member != null) {
            if (!context.member.canInteract(member)) {
                return context.sendMessage("${context.getAsMention()} Você não pode punir este membro, seus cargos são menores que os dele!")
            }

            if (!context.guild.selfMember.canInteract(member)) {
                return context.sendMessage("${context.getAsMention()} Eu não posso punir este membro, meus cargos são menores que os dele!")
            }


            context.sendMessage("${context.getAsMention()} Você tem certeza que quer punir o usuário ${user.asMention} (`${user.tag} - ${user.id}`) do seu servidor pelo motivo `$effectiveReason`. Clique no :white_check_mark: para confirmar e no :no_good: para cancelar!") { message ->
                message.addReaction("✅").queue()
                message.addReaction("\uD83D\uDE45").queue()

                message.onReactionAdd { event ->
                    when (event.reactionEmote.name) {
                        "✅" -> {
                            if (!user.isFake && !user.isBot) {
                                val builder = EmbedBuilder()

                                builder.setTitle("Você foi banido do servidor ${context.guild.name}!")
                                builder.setAuthor(context.user.tag, null, context.user.effectiveAvatarUrl)
                                builder.setThumbnail(context.user.effectiveAvatarUrl)

                                builder.addField("Quem puniu", context.user.tag, true)
                                builder.addField("Motivo", reason ?: "Sem motivo definido", true)

                                builder.setFooter("Esta mensagem foi enviada por um bot, então não responda!", null)
                                builder.setColor(Color.RED)

                                user.openPrivateChannel().queue({
                                    it.sendMessage(builder.build()).queue()
                                }, {})
                            }

                            context.guild.controller.kick(user.id, "Punido por ${context.user.tag} - Motivo: $effectiveReason").queue()

                            message.delete().queue()
                            context.sendMessage("${context.getAsMention()} Usuário punido com sucesso!")
                        }

                        "\uD83D\uDE45" -> {
                            message.delete().queue()

                            context.sendMessage("${context.getAsMention()} Punição cancelada!")
                        }
                    }
                }
            }
        } else {
            context.sendMessage("${context.getAsMention()} Este usuário não está no servidor!")
        }
    }
}