package me.mrgaabriel.ayla.commands.developer

import me.mrgaabriel.ayla.commands.*
import net.dv8tion.jda.core.*
import org.apache.commons.lang3.exception.*
import org.jetbrains.kotlin.script.jsr223.*
import java.awt.*
import java.time.*
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

        val scriptEngine = KotlinJsr223JvmDaemonLocalEvalScriptEngineFactory().scriptEngine

        val code = """
            import me.mrgaabriel.ayla.*
            import me.mrgaabriel.ayla.commands.*
            import me.mrgaabriel.ayla.data.*
            import me.mrgaabriel.ayla.listeners.*
            import me.mrgaabriel.ayla.utils.*

            import kotlin.concurrent.*

            fun eval(context: CommandContext): Any? {
            ${context.args.joinToString(" ")}

            return null
            }
        """.trimIndent()

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

            builder.setTitle("oopsie woopsie")
            builder.setDescription("```$message```")
            builder.setColor(Color.RED)

            builder.setFooter("we made a fucky wucky \uD83D\uDE22", null)
            builder.setTimestamp(OffsetDateTime.now())

            context.sendMessage(builder.build(), context.getAsMention())
        }
    }
}