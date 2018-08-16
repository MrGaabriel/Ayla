package me.mrgaabriel.ayla

import ch.qos.logback.classic.*
import com.google.common.flogger.*
import com.google.common.util.concurrent.*
import com.mongodb.*
import com.mongodb.MongoClient
import com.mongodb.client.*
import me.mrgaabriel.ayla.commands.*
import me.mrgaabriel.ayla.commands.config.*
import me.mrgaabriel.ayla.commands.developer.*
import me.mrgaabriel.ayla.commands.utils.*
import me.mrgaabriel.ayla.data.*
import me.mrgaabriel.ayla.listeners.*
import me.mrgaabriel.ayla.threads.*
import me.mrgaabriel.ayla.utils.*
import me.mrgaabriel.ayla.utils.eventlog.*
import net.dv8tion.jda.core.*
import net.dv8tion.jda.core.entities.*
import org.bson.codecs.configuration.*
import org.bson.codecs.pojo.*
import org.slf4j.*
import java.util.concurrent.*

class Ayla(var config: AylaConfig) {

    val logger = FluentLogger.forEnclosingClass()

    private lateinit var builder: JDABuilder

    val commandMap = mutableListOf<AbstractCommand>()
    val shards = mutableListOf<JDA>()

    lateinit var mongo: MongoClient
    lateinit var mongoDatabase: MongoDatabase

    lateinit var usersColl: MongoCollection<AylaUser>
    lateinit var guildsColl: MongoCollection<AylaGuildConfig>
    lateinit var storedMessagesColl: MongoCollection<StoredMessage>

    val messageInteractionCache = mutableMapOf<String, MessageInteractionWrapper>()

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
                .addEventListener(EventLogListeners())

        for (idx in 0..(config.shardCount - 1)) {
            logger.atInfo().log("Iniciando shard $idx...")

            val shard = builder.useSharding(idx, config.shardCount)
                    .buildBlocking()
            shards.add(shard)
        }

        GameUpdateThread().start()
        RemoveCachedMessagesThread().start()

        logger.atInfo().log("OK! - Ayla inicializada com sucesso!")
    }

    fun loadMongo() {
        val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
        val logger = loggerContext.getLogger("org.mongodb.driver")

        logger.level = Level.OFF

        val pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()))

        val options = MongoClientOptions.builder()
                .codecRegistry(pojoCodecRegistry)
                .build()

        val client = MongoClient("127.0.0.1:27017", options)
        mongo = client

        val database = client.getDatabase("ayla")
        mongoDatabase = database

        val users = database.getCollection("users", AylaUser::class.java)
                .withCodecRegistry(pojoCodecRegistry)
        usersColl = users

        val guilds = database.getCollection("guilds", AylaGuildConfig::class.java)
                .withCodecRegistry(pojoCodecRegistry)
        guildsColl = guilds

        val storedMessages = database.getCollection("storedMessages", StoredMessage::class.java)
                .withCodecRegistry(pojoCodecRegistry)
        storedMessagesColl = storedMessages
    }

    fun loadCommands() {
        commandMap.clear()

        commandMap.add(PingCommand())
        commandMap.add(EvalCommand())
        commandMap.add(ReloadCommand())
        commandMap.add(EvalJSCommand())
        commandMap.add(EventLogCommand())
        commandMap.add(PrefixCommand())
        commandMap.add(WelcomeCommand())
    }

    fun setGame(game: Game) {
        shards.forEach {
            it.presence.game = game
        }
    }

    fun getUserById(id: String): User? {
        var user: User? = null

        shards.forEach {
            user = it.retrieveUserById(id).complete()
        }

        return user
    }

    fun getGuildById(id: String): Guild? {
        var guild: Guild? = null

        shards.forEach {
            guild = it.getGuildById(id)
        }

        return guild
    }

}