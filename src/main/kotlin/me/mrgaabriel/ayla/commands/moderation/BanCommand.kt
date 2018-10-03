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

class BanCommand : AbstractCommand("ban", category = CommandCategory.MODERATION, description = "Bane alguém do seu servidor", usage = "usuário motivo") {

    @Subcommand
    @SubcommandPermissions([Permission.BAN_MEMBERS], botPermissions = [Permission.BAN_MEMBERS])
    fun ban(context: CommandContext, @InjectArgument(ArgumentType.USER) user: User?, @InjectArgument(ArgumentType.ARGUMENT_LIST) reason: String?) {
        if (context.args.isEmpty()) {
            return context.explain()
        }

        if (user == null) {
            return context.sendMessage(context.getAsMention(true) + "Usuário não encontrado!")
        }

        val member = context.guild.getMember(user)
        if (member != null) {
            if (!context.guild.selfMember.canInteract(member)) {
                return context.sendMessage(context.getAsMention(true) + "Eu não posso punir este usuário, meus cargos são menores que os dele!")
            }

            if (!context.member.canInteract(member)) {
                return context.sendMessage(context.getAsMention(true) + "Você não pode punir este usuário, seus cargos são menores que os dele!")
            }
        }

        context.sendMessage(context.getAsMention(true) + "Você tem certeza que quer punir o usuário ${user.asMention} (`${user.tag} - ${user.id}`) do seu servidor pelo motivo `${reason ?: "Sem motivo definido"}`? Clique no :white_check_mark: para confirmar e no :no_good: para cancelar!") { message ->
            message.addReaction("✅").queue()
            message.addReaction("\uD83D\uDE45").queue()

            message.onReactionAdd(false) { ev ->
                if (!ev.user.isBot && ev.user.id == context.user.id) {

                    when (ev.reactionEmote.name) {
                        "✅" -> {
                            message.delete().queue()

                            if (context.guild.isMember(user) && (!user.isBot && !user.isFake)) {
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

                            context.guild.controller.ban(user, 0, "Punido por ${context.user.tag} - Motivo: ${reason ?: "Sem motivo definido"}").queue()

                            context.sendMessage(context.getAsMention(true) + "Usuário punido com sucesso!")
                        }

                        "\uD83D\uDE45" -> {
                            message.delete().queue()

                            context.sendMessage(context.getAsMention(true) + "Punição cancelada!")
                        }
                    }

                }
            }
        }
    }
}