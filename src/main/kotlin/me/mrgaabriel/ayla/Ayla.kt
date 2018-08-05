package me.mrgaabriel.ayla

import ch.qos.logback.classic.*
import com.google.common.flogger.*
import com.google.common.util.concurrent.*
import com.mongodb.MongoClient
import com.mongodb.client.*
import me.mrgaabriel.ayla.commands.*
import me.mrgaabriel.ayla.commands.developer.*
import me.mrgaabriel.ayla.commands.utils.*
import me.mrgaabriel.ayla.data.*
import me.mrgaabriel.ayla.listeners.*
import net.dv8tion.jda.core.*
import org.bson.codecs.configuration.*
import org.bson.codecs.pojo.*
import org.slf4j.*
import java.util.concurrent.*

class Ayla(val config: AylaConfig) {

    val logger = FluentLogger.forEnclosingClass()

    private lateinit var builder: JDABuilder

    val commandMap = mutableListOf<AbstractCommand>()

    lateinit var mongo: MongoClient
    lateinit var mongoDatabase: MongoDatabase

    lateinit var usersColl: MongoCollection<AylaUser>
    lateinit var guildsColl: MongoCollection<AylaGuildConfig>

    fun createThreadPool(name: String): ExecutorService {
        return Executors.newCachedThreadPool(ThreadFactoryBuilder().setNameFormat(name).build())
    }

    val executor = createThreadPool("Executor Thread %d")

    fun start() {
        loadMongo()
        loadCommands()

        builder = JDABuilder(AccountType.BOT)
                .setToken(config.clientToken)
                .setStatus(OnlineStatus.ONLINE)
                .addEventListener(DiscordListeners())

        for (idx in 0..(config.shardCount - 1)) {
            logger.atInfo().log("Iniciando shard $idx...")

            builder.useSharding(idx, config.shardCount)
                    .buildBlocking()
        }

        logger.atInfo().log("OK! - Ayla inicializada com sucesso!")
    }

    fun loadMongo() {
        val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
        val logger = loggerContext.getLogger("org.mongodb.driver")

        logger.level = Level.OFF

        val pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()))

        val client = MongoClient("127.0.0.1:27017")
        mongo = client

        val database = client.getDatabase("ayla")
                .withCodecRegistry(pojoCodecRegistry)
        mongoDatabase = database

        val users = database.getCollection("users", AylaUser::class.java)
        usersColl = users

        val guilds = database.getCollection("guilds", AylaGuildConfig::class.java)
        guildsColl = guilds
    }

    fun loadCommands() {
        commandMap.clear()

        commandMap.add(PingCommand())
        commandMap.add(EvalCommand())
        commandMap.add(ReloadCommand())
    }

}