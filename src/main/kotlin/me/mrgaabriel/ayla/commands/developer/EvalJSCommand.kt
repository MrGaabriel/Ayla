package me.mrgaabriel.ayla.commands.developer

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.set
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import me.mrgaabriel.ayla.utils.Constants
import me.mrgaabriel.ayla.utils.ayla
import me.mrgaabriel.ayla.utils.commands.AbstractCommand
import me.mrgaabriel.ayla.utils.commands.CommandCategory
import me.mrgaabriel.ayla.utils.commands.CommandContext
import me.mrgaabriel.ayla.utils.commands.annotations.ArgumentType
import me.mrgaabriel.ayla.utils.commands.annotations.InjectArgument
import me.mrgaabriel.ayla.utils.commands.annotations.Subcommand
import me.mrgaabriel.ayla.utils.commands.annotations.SubcommandPermissions
import org.apache.commons.lang3.exception.ExceptionUtils
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
            val payload = JsonObject()

            payload["description"] = "Erro ao executar o código do Eval"
            payload["public"] = false

            val error = JsonObject()
            error["content"] = ExceptionUtils.getStackTrace(e)

            val files = JsonObject()
            files["error.txt"] = error

            payload["files"] = files

            val requestBody = HttpRequest.post("https://api.github.com/gists")
                    .userAgent(Constants.USER_AGENT)
                    .authorization("token ${ayla.config.gistToken}")
                    .send(payload.toString())
                    .body()

            val receivedPayload = JsonParser().parse(requestBody)

            val url = receivedPayload["html_url"].string

            context.sendMessage(context.getAsMention(true) + "Erro ao executar!\n$url")
        }
    }
}