package me.mrgaabriel.ayla.commands.developer

import me.mrgaabriel.ayla.commands.*
import net.dv8tion.jda.core.*
import org.apache.commons.lang3.exception.*
import java.awt.*
import java.time.*
import javax.script.*

class EvalJSCommand : AbstractCommand() {

    init {
        this.label = "evaljs"
        this.description = "Executa códigos em JavaScript"
        this.usage = "código"

        this.category = CommandCategory.DEVELOPER
        this.onlyOwner = true
    }

    override fun execute(context: CommandContext) {
        if (context.args.isEmpty()) {
            context.explain()
            return
        }

        val scriptEngine = ScriptEngineManager().getEngineByName("nashorn")

        val code = """
            function eval(context) {
            ${context.args.joinToString(" ")}
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