package me.mrgaabriel.ayla.commands.developer

import me.mrgaabriel.ayla.utils.commands.AbstractCommand
import me.mrgaabriel.ayla.utils.commands.CommandCategory
import me.mrgaabriel.ayla.utils.commands.CommandContext
import me.mrgaabriel.ayla.utils.commands.annotations.ArgumentType
import me.mrgaabriel.ayla.utils.commands.annotations.InjectArgument
import me.mrgaabriel.ayla.utils.commands.annotations.Subcommand
import me.mrgaabriel.ayla.utils.commands.annotations.SubcommandPermissions
import net.dv8tion.jda.core.EmbedBuilder
import org.apache.commons.lang3.exception.ExceptionUtils
import java.awt.Color
import java.time.OffsetDateTime
import javax.script.ScriptEngineManager

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
            scriptEngine.put("context", context)

            val evaluated = scriptEngine.eval(code)

            context.sendMessage("```js\n$evaluated```")
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