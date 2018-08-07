package me.mrgaabriel.ayla.commands.developer

import me.mrgaabriel.ayla.*
import me.mrgaabriel.ayla.commands.*
import net.dv8tion.jda.core.*
import org.apache.commons.lang3.exception.*
import org.jetbrains.kotlin.script.jsr223.*
import java.awt.*
import java.nio.file.*
import java.time.*
import java.util.jar.*
import javax.script.*


class EvalCommand : AbstractCommand() {

    init {
        this.label = "eval"
        this.description = "Executa códigos em Kotlin"
        this.usage = "código"

        this.aliases = mutableListOf("evaluate", "evalkt", "evaluatekt")

        this.category = CommandCategory.DEVELOPER

        this.onlyOwner = true
    }

    override fun execute(context: CommandContext) {
        if (context.args.isEmpty()) {
            context.explain()
            return
        }

        // https://www.reddit.com/r/Kotlin/comments/8qdd4x/kotlin_script_engine_and_your_classpaths_what/
        val path = this::class.java.protectionDomain.codeSource.location.path
        val jar = JarFile(path)
        val mf = jar.manifest
        val mattr = mf.mainAttributes
        // Yes, you SHOULD USE Attributes.Name.CLASS_PATH! Don't try using "Class-Path", it won't work!
        val manifestClassPath = mattr[Attributes.Name.CLASS_PATH] as String

        // The format within the Class-Path attribute is different than the one expected by the property, so let's fix it!
        // By the way, don't forget to append your original JAR at the end of the string!
        val clazz = AylaLauncher::class.java
        val protectionDomain = clazz.protectionDomain
        val propClassPath = manifestClassPath.replace(" ", ":") + ":${Paths.get(protectionDomain.codeSource.location.toURI()).fileName}"

        // Now we set it to our own classpath
        System.setProperty("kotlin.script.classpath", propClassPath)

        val code = """
            import me.mrgaabriel.ayla.*
            import me.mrgaabriel.ayla.commands.*
            import me.mrgaabriel.ayla.data.*
            import me.mrgaabriel.ayla.listeners.*
            import me.mrgaabriel.ayla.utils.*
            import me.mrgaabriel.ayla.threads.*

            import kotlin.concurrent.*

            fun eval(context: CommandContext): Any? {
            ${context.args.joinToString(" ")}

            return null
            }
        """.trimIndent()

        val scriptEngine = ScriptEngineManager().getEngineByName("kotlin")
        try {
            val start = System.currentTimeMillis()

            scriptEngine.eval(code)

            val invocable = scriptEngine as Invocable
            val value = invocable.invokeFunction("eval", context)

            if (value != null) {
                context.sendMessage("```$value\n\nOK! Processado com sucesso em ${System.currentTimeMillis() - start}ms```")
            } else {
                context.sendMessage("```OK! Processado com sucesso em ${System.currentTimeMillis() - start}ms```")
            }
        } catch (e: Exception) {
            val message = if (e.message != null) {
                e.message
            } else {
                if (ExceptionUtils.getStackTrace(e).length > 2000) {
                    ExceptionUtils.getStackTrace(e).substring(0, 2000)
                } else {
                    ExceptionUtils.getStackTrace(e)
                }
            }

            val builder = EmbedBuilder()

            builder.setTitle("Oopsie Woopsie")
            builder.setDescription("```$message```")
            builder.setColor(Color.RED)

            builder.setFooter("We made a Fucky Wucky \uD83D\uDE22", null)
            builder.setTimestamp(OffsetDateTime.now())

            context.sendMessage(builder.build(), context.getAsMention())
        }
    }
}