package com.github.mrgaabriel.ayla

import com.github.mrgaabriel.ayla.audio.AudioManager
import com.github.mrgaabriel.ayla.commands.AbstractCommand
import com.github.mrgaabriel.ayla.commands.misc.VideoChatCommand
import com.github.mrgaabriel.ayla.commands.music.*
import com.github.mrgaabriel.ayla.config.AylaConfig
import com.github.mrgaabriel.ayla.debug.DebugLog
import com.github.mrgaabriel.ayla.listeners.DiscordListeners
import com.github.mrgaabriel.ayla.managers.AylaCommandManager
import com.github.mrgaabriel.ayla.tables.Giveaways
import com.github.mrgaabriel.ayla.tables.GuildConfigs
import com.github.mrgaabriel.ayla.tables.SubReddits
import com.github.mrgaabriel.ayla.tables.UserProfiles
import com.github.mrgaabriel.ayla.threads.GameUpdateThread
import com.github.mrgaabriel.ayla.utils.GiveawayUtils
import com.github.mrgaabriel.ayla.utils.RedditUtils
import com.github.mrgaabriel.ayla.utils.extensions.ayla
import com.github.mrgaabriel.ayla.utils.logger
import com.github.mrgaabriel.ayla.website.Website
import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.asCoroutineDispatcher
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.OnlineStatus
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.concurrent.thread

class Ayla(var config: AylaConfig) {

    lateinit var shardManager: ShardManager
    lateinit var website: Website

    var commandManager = AylaCommandManager()

    val hikariConfig by lazy {
        val config = HikariConfig()
        config.jdbcUrl =
                "jdbc:postgresql://${ayla.config.postgreIp}:${ayla.config.postgrePort}/${ayla.config.postgreDatabaseName}"
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

    fun createThreadPool(name: String): ExecutorService {
        return Executors.newCachedThreadPool(ThreadFactoryBuilder().setNameFormat(name).build())
    }

    val redditTasksPool = createThreadPool("Reddit Task %d")
    val redditTasksDispatcher = redditTasksPool.asCoroutineDispatcher()

    val giveawayTasksPool = createThreadPool("Giveaway Task %d")
    val giveawayTasksDispatcher = giveawayTasksPool.asCoroutineDispatcher()

    val defaultPool = createThreadPool("Coroutine Dispatcher Task %d")
    val defaultCoroutineDispatcher = defaultPool.asCoroutineDispatcher()

    val dataSource by lazy { HikariDataSource(hikariConfig) }
    val database by lazy { Database.connect(dataSource) }

    lateinit var audioManager: AudioManager

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

        audioManager = AudioManager()
        shardManager.addEventListener(audioManager.lavalink)

        initPostgre()
        initWebsite()

        GiveawayUtils.spawnTasks()
        RedditUtils.spawnTasks()

        DebugLog.startCommandHandler()

        GameUpdateThread().start()
    }

    fun initPostgre() {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(
                GuildConfigs,
                Giveaways,
                SubReddits,
                UserProfiles
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
}