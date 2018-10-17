package me.mrgaabriel.ayla.commands.developer

import me.mrgaabriel.ayla.utils.commands.AbstractCommand
import me.mrgaabriel.ayla.utils.commands.CommandCategory
import me.mrgaabriel.ayla.utils.commands.CommandContext
import me.mrgaabriel.ayla.utils.commands.annotations.ArgumentType
import me.mrgaabriel.ayla.utils.commands.annotations.InjectArgument
import me.mrgaabriel.ayla.utils.commands.annotations.Subcommand
import me.mrgaabriel.ayla.utils.commands.annotations.SubcommandPermissions
import me.mrgaabriel.ayla.utils.gist.GistUtils
import org.apache.commons.lang3.exception.ExceptionUtils
import javax.script.ScriptEngineManager

class EvalJSCommand : AbstractCommand("evaljs", CommandCategory.DEVELOPER, "Executa códigos em JavaScript", "código") {

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
            val gistUrl = GistUtils.createGist(ExceptionUtils.getStackTrace(e), "Erro ao executar o código do Eval", false, "error.txt")

            context.sendMessage(context.getAsMention(true) + "Erro ao executar!\n$gistUrl")
        }
    }
}