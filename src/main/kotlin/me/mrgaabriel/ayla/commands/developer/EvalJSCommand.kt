package me.mrgaabriel.ayla.commands.developer

import me.mrgaabriel.ayla.utils.commands.*
import me.mrgaabriel.ayla.utils.commands.annotations.*
import net.dv8tion.jda.core.*
import org.apache.commons.lang3.exception.*
import java.awt.*
import java.time.*
import javax.script.*

class EvalJSCommand : AbstractCommand(
        "evaljs",
        CommandCategory.DEVELOPER,
        "Executa códigos em JavaScript",
        "código"
) {

    @Subcommand
    @SubcommandPermissions([], true)
    fun onExecute(context: CommandContext, @InjectArgument(ArgumentType.ARGUMENT_LIST) code: String) {
        if (context.args.isEmpty()) {
            context.explain()
            return
        }

        val scriptEngine = ScriptEngineManager().getEngineByName("nashorn")

        try {
            val start = System.currentTimeMillis()
            scriptEngine.put("context", context)

            val evaluated = scriptEngine.eval(code)

            context.sendMessage("```diff\n+ $evaluated\n\nOK! Processado com sucesso em ${System.currentTimeMillis() - start}ms```")
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