package com.github.mrgaabriel.ayla.commands.developer

import com.github.mrgaabriel.ayla.commands.*
import com.github.mrgaabriel.ayla.config.AylaConfig
import com.github.mrgaabriel.ayla.managers.AylaCommandManager
import com.github.mrgaabriel.ayla.utils.Static
import com.github.mrgaabriel.ayla.utils.extensions.ayla
import net.perfectdreams.commands.annotation.Subcommand
import java.io.File
import kotlin.contracts.ExperimentalContracts

class ReloadCommand : AylaCommand("reload") {

    override val onlyOwner: Boolean
        get() = true

    override val category: CommandCategory
        get() = CommandCategory.DEVELOPER

    @Subcommand
    suspend fun root(context: AylaCommandContext) {
        commands(context)
        config(context)
        website(context)
        bot(context)
    }

    @Subcommand(["commands"])
    suspend fun commands(context: AylaCommandContext) {
        ayla.commandManager = AylaCommandManager()

        context.reply("Comandos recarregados com sucesso! ${ayla.commandManager.getRegisteredCommands().size} comandos registrados")
    }

    @Subcommand(["shard"])
    @ExperimentalContracts
    suspend fun shard(context: AylaCommandContext, shardId: Int?) {
        notNull(shardId, "\"shardId\" is null!")

        context.reply("Reiniciando shard $shardId!!!")
        ayla.shardManager.restart(shardId)

        try {
            context.reply("Shard $shardId reinicializada com sucesso!")
        } catch (e: Exception) {}
    }

    @Subcommand(["bot"])
    suspend fun bot(context: AylaCommandContext) {
        context.sendMessage("${context.event.author.asMention} Reiniciando o bot!!!")

        ayla.shardManager.restart()
    }

    @Subcommand(["config"])
    suspend fun config(context: AylaCommandContext) {
        val file = File("config.yml")
        ayla.config = Static.YAML_MAPPER.readValue(file, AylaConfig::class.java)

        context.reply("Configuração recarregada com sucesso!!!")
    }

    @Subcommand(["website"])
    suspend fun website(context: AylaCommandContext) {
        ayla.website.stop()
        ayla.initWebsite()

        context.reply("Website recarregado com sucesso!!!")
    }
}