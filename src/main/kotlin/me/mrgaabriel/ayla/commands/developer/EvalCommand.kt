package me.mrgaabriel.ayla.commands.developer

import com.github.kevinsawicki.http.*
import me.mrgaabriel.ayla.*
import me.mrgaabriel.ayla.utils.commands.*
import me.mrgaabriel.ayla.utils.commands.annotations.*
import net.dv8tion.jda.core.*
import org.apache.commons.lang3.exception.*
import org.jetbrains.kotlin.script.jsr223.*
import java.awt.*
import java.nio.file.*
import java.time.*
import java.util.*
import java.util.jar.*
import javax.script.*

class EvalCommand : AbstractCommand(
        "eval",
        CommandCategory.DEVELOPER,
        "Executa códigos em Kotlin",
        "código",
        listOf("evaluate", "evalkt", "evaluatekt")
) {

    @Subcommand
    @SubcommandPermissions([], true)
    fun onExecute(context: CommandContext, @InjectArgument(ArgumentType.ARGUMENT_LIST) code: String) {
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

        val scriptEngine = ScriptEngineManager().getEngineByName("kotlin") as KotlinJsr223JvmLocalScriptEngine

        val script = """
            import me.mrgaabriel.ayla.commands.*
            import me.mrgaabriel.ayla.data.*
            import me.mrgaabriel.ayla.listeners.*
            import me.mrgaabriel.ayla.threads.*
            import me.mrgaabriel.ayla.utils.*

            fun eval(context: me.mrgaabriel.ayla.utils.commands.CommandContext): Any? {
                $code

                return null
            }
        """.trimIndent()

        try {
            val start = System.currentTimeMillis()
            scriptEngine.eval(script)

            val invocable = scriptEngine as Invocable
            val evaluated = invocable.invokeMethod(this, "eval", context)

            context.sendMessage("```\n$evaluated\n\nOK! Processado com sucesso em ${System.currentTimeMillis() - start}ms```")
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