package me.mrgaabriel.ayla.commands.misc

import me.mrgaabriel.ayla.utils.AylaUtils
import me.mrgaabriel.ayla.utils.ayla
import me.mrgaabriel.ayla.utils.commands.AbstractCommand
import me.mrgaabriel.ayla.utils.commands.CommandCategory
import me.mrgaabriel.ayla.utils.commands.CommandContext
import me.mrgaabriel.ayla.utils.commands.annotations.Subcommand
import me.mrgaabriel.ayla.utils.tag
import net.dv8tion.jda.core.EmbedBuilder

class BotInfoCommand : AbstractCommand("botinfo", category = CommandCategory.MISC, description = "Veja as informações sobre a Ayla") {

    @Subcommand
    fun botInfo(context: CommandContext) {
        val builder = EmbedBuilder()

        builder.setColor(AylaUtils.randomColor())
        builder.setAuthor(context.user.tag, null, context.user.effectiveAvatarUrl)

        builder.setTitle("Informações sobre mim")
        builder.setDescription("""
            👋 Olá, eu sou a Ayla, mais um bot de terras tupiniquins para aperfeiçoar seu servidor!
            Sou feita em [Kotlin](https://jetbrains.com/kotlin) e uso a library [JDA](https://github.com/Dv8FromTheWorld/JDA) para interagir com o Discord!
        """.trimIndent())

        builder.addField("Quer me ajudar?", """
            Se você quiser me ajudar, você pode votar na [Discord Bots List](https://discordbots.org/bot/${ayla.config.clientId}), [Bots on Discord](https://bots.ondiscord.xyz/bot/${ayla.config.clientId}) ou [Whistler Bot List](https://bots.perfectdreams.net/bot/${ayla.config.clientId}) para me promover para cada vez mais servidores!
        """.trimIndent(), true)
        builder.addField("Agradecimentos", """
            `MrGaabriel#2430` Se não fosse por ele, eu nem existiria!
            `Natan Moreira Mano#0572` Por ter feito a minha foto de perfil!
            `${context.user.tag}` Por estar falando comigo!
        """.trimIndent(), true)

        builder.setFooter("Informações da Ayla", null)

        context.sendMessage(builder.build(), context.getAsMention())
    }
}