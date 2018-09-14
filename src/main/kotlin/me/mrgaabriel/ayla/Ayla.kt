package me.mrgaabriel.ayla

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.mongodb.MongoClient
import com.mongodb.MongoClientOptions
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import me.mrgaabriel.ayla.commands.config.BadWordsCommand
import me.mrgaabriel.ayla.commands.config.EventLogCommand
import me.mrgaabriel.ayla.commands.config.PrefixCommand
import me.mrgaabriel.ayla.commands.config.RedditCommand
import me.mrgaabriel.ayla.commands.config.WelcomeCommand
import me.mrgaabriel.ayla.commands.developer.BashCommand
import me.mrgaabriel.ayla.commands.developer.EvalCommand
import me.mrgaabriel.ayla.commands.developer.EvalJSCommand
import me.mrgaabriel.ayla.commands.developer.ReloadCommand
import me.mrgaabriel.ayla.commands.utils.HelpCommand
import me.mrgaabriel.ayla.commands.utils.PingCommand
import me.mrgaabriel.ayla.data.AylaConfig
import me.mrgaabriel.ayla.data.AylaGuildConfig
import me.mrgaabriel.ayla.data.AylaUser
import me.mrgaabriel.ayla.listeners.DiscordListeners
import me.mrgaabriel.ayla.listeners.EventLogListeners
import me.mrgaabriel.ayla.threads.GameUpdateThread
import me.mrgaabriel.ayla.threads.RedditPostSyncThread
import me.mrgaabriel.ayla.threads.RemoveCachedMessagesThread
import me.mrgaabriel.ayla.threads.UpdateBotStatsThread
import me.mrgaabriel.ayla.utils.MessageInteractionWrapper
import me.mrgaabriel.ayla.utils.commands.AbstractCommand
import me.mrgaabriel.ayla.utils.eventlog.StoredMessage
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.User
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import org.slf4j.LoggerFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Ayla(var config: AylaConfig) {

    val logger = LoggerFactory.getLogger(Ayla::class.java)

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
            logger.info("Iniciando shard $idx...")

            val shard = builder.useSharding(idx, config.shardCount)
                    .buildBlocking()
            shards.add(shard)
        }

        GameUpdateThread().start()
        RemoveCachedMessagesThread().start()
        RedditPostSyncThread().start()
        UpdateBotStatsThread().start()

        logger.info("OK! - Ayla inicializada com sucesso!")
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

        val client = MongoClient(config.mongoHostname, options)
        mongo = client

        val database = client.getDatabase(config.mongoDatabaseName)
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
        commandMap.add(RedditCommand())
        commandMap.add(HelpCommand())
        commandMap.add(BashCommand())
        commandMap.add(BadWordsCommand())
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