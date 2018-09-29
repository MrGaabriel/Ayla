package me.mrgaabriel.ayla

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import com.github.benmanes.caffeine.cache.Caffeine
import com.mongodb.MongoClient
import com.mongodb.MongoClientOptions
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import me.mrgaabriel.ayla.audio.AudioManager
import me.mrgaabriel.ayla.data.AylaConfig
import me.mrgaabriel.ayla.data.AylaGuildConfig
import me.mrgaabriel.ayla.data.AylaUser
import me.mrgaabriel.ayla.listeners.DiscordListeners
import me.mrgaabriel.ayla.listeners.EventLogListeners
import me.mrgaabriel.ayla.threads.GameUpdateThread
import me.mrgaabriel.ayla.threads.RedditPostSyncThread
import me.mrgaabriel.ayla.threads.RemoveCachedMessagesThread
import me.mrgaabriel.ayla.threads.UpdateBotStatsThread
import me.mrgaabriel.ayla.utils.AylaUtils
import me.mrgaabriel.ayla.utils.MessageInteraction
import me.mrgaabriel.ayla.utils.commands.AbstractCommand
import me.mrgaabriel.ayla.utils.eventlog.StoredMessage
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.utils.cache.CacheFlag
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.TimeUnit

class Ayla(var config: AylaConfig) {

    val logger = LoggerFactory.getLogger(Ayla::class.java)

    val flags = EnumSet.of(CacheFlag.EMOTE, CacheFlag.GAME)
    val builder = JDABuilder(AccountType.BOT)
            .setToken(config.clientToken)
            .setStatus(OnlineStatus.ONLINE)
            .setDisabledCacheFlags(flags)
            .addEventListener(DiscordListeners())
            .addEventListener(EventLogListeners())

    lateinit var audioManager: AudioManager

    val commandMap = mutableListOf<AbstractCommand>()
    val shards = mutableListOf<JDA>()

    lateinit var mongo: MongoClient
    lateinit var mongoDatabase: MongoDatabase

    lateinit var usersColl: MongoCollection<AylaUser>
    lateinit var guildsColl: MongoCollection<AylaGuildConfig>
    lateinit var storedMessagesColl: MongoCollection<StoredMessage>

    val messageInteractionCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(2000)
            .build<String, MessageInteraction>()
            .asMap()

    val commandCooldownCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .maximumSize(5000L)
            .build<String, Long>()
            .asMap()

    fun start() {
        loadMongo()
        loadCommands()

        for (idx in 0..(config.shardCount - 1)) {
            logger.info("Iniciando shard $idx...")

            val shard = builder.useSharding(idx, config.shardCount)
                    .build().awaitReady()
            shards.add(shard)
        }

        audioManager = AudioManager()

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

        AylaUtils.getClasses("me.mrgaabriel.ayla.commands").forEach { clazz ->
            try {
                if (AbstractCommand::class.java.isAssignableFrom(clazz)) {
                    val command = clazz.newInstance() as AbstractCommand

                    commandMap.add(command)
                    logger.info("Comando ${clazz.simpleName} carregado com sucesso")
                }
            } catch (e: Exception) {
                logger.warn("Erro ao carregar o comando ${clazz.simpleName}!")
            }
        }
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

    fun getTextChannelById(id: String): TextChannel? {
        var channel: TextChannel? = null

        shards.forEach {
            channel = it.getTextChannelById(id)
        }

        return channel
    }

    fun getGuildById(id: String): Guild? {
        var guild: Guild? = null

        shards.forEach {
            guild = it.getGuildById(id)
        }

        return guild
    }

}