package com.github.mrgaabriel.ayla

import com.github.mrgaabriel.ayla.commands.AbstractCommand
import com.github.mrgaabriel.ayla.commands.developer.EvalCommand
import com.github.mrgaabriel.ayla.commands.developer.ReloadCommand
import com.github.mrgaabriel.ayla.commands.images.IsThisCommand
import com.github.mrgaabriel.ayla.commands.utils.PingCommand
import com.github.mrgaabriel.ayla.config.AylaConfig
import com.github.mrgaabriel.ayla.listeners.DiscordListeners
import com.github.mrgaabriel.ayla.tables.Guilds
import com.github.mrgaabriel.ayla.threads.GameUpdateThread
import com.github.mrgaabriel.ayla.utils.extensions.ayla
import com.github.mrgaabriel.ayla.utils.logger
import com.github.mrgaabriel.ayla.website.Website
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.OnlineStatus
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.Executors
import kotlin.concurrent.thread

class Ayla(var config: AylaConfig) {

    lateinit var shardManager: ShardManager
    lateinit var website: Website

    val hikariConfig by lazy {
        val config = HikariConfig()
        config.jdbcUrl = "jdbc:postgresql://${ayla.config.postgreIp}:${ayla.config.postgrePort}/${ayla.config.postgreDatabaseName}"
        config.username = ayla.config.postgreUsername
        if (ayla.config.postgrePassword.isNotEmpty())
            config.password = ayla.config.postgrePassword
        config.driverClassName = "org.postgresql.Driver"

        config.addDataSourceProperty("cachePrepStmts", "true")
        config.addDataSourceProperty("prepStmtCacheSize", "250")
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        config.maximumPoolSize = 150
        return@lazy config
    }

    val dataSource by lazy { HikariDataSource(hikariConfig) }
    val database by lazy { Database.connect(dataSource) }

    val builder = DefaultShardManagerBuilder()
        .setToken(config.clientToken)
        .setCallbackPool(Executors.newSingleThreadExecutor())
        .setBulkDeleteSplittingEnabled(true)
        .setStatus(OnlineStatus.valueOf(config.onlineStatus))
        .setShardsTotal(config.shardsCount)
        .addEventListeners(DiscordListeners())

    val logger by logger()

    val commandMap = mutableListOf<AbstractCommand>()

    fun start() {
        logger.info("Iniciando a Ayla (discord bot)...")
        shardManager = builder.build()
        logger.info("OK! Ayla (discord bot) iniciada com sucesso!")

        initPostgre()
        loadCommands()
        initWebsite()

        GameUpdateThread().start()
    }

    fun initPostgre() {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(
                Guilds
            )
        }
    }

    fun initWebsite() {
        website = Website(config.websiteUrl)

        thread(name = "Website Thread") {
            org.jooby.run({
                website
            })
        }
    }

    fun loadCommands() {
        commandMap.clear()

        // ==[ UTILS ]==
        commandMap.add(PingCommand())

        // ==[ DEVELOPER ]==
        commandMap.add(ReloadCommand())
        commandMap.add(EvalCommand())

        // ==[ IMAGES ]==
        commandMap.add(IsThisCommand())
    }
}