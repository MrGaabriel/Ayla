package me.mrgaabriel.ayla.commands.developer

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.set
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import me.mrgaabriel.ayla.AylaLauncher
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
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngine
import java.nio.file.Paths
import java.util.jar.Attributes
import java.util.jar.JarFile
import javax.script.ScriptContext
import javax.script.ScriptEngineManager

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
        scriptEngine.put("engineContext", scriptEngine.context)
        scriptEngine.put("context", context)

        val bindings = buildString {
            scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE).forEach { key, value ->
                if ("." !in key) {
                    val name: String = value::class.qualifiedName!!
                    val bind = """val $key = bindings["$key"] as $name"""
                    appendln(bind)
                }
            }
        }

        val script = """
            import me.mrgaabriel.ayla.commands.*
            import me.mrgaabriel.ayla.data.*
            import me.mrgaabriel.ayla.listeners.*
            import me.mrgaabriel.ayla.threads.*
            import me.mrgaabriel.ayla.utils.*

            $bindings

            $code
        """.trimIndent()

        try {
            val evaluated = scriptEngine.eval(script, scriptEngine.context)

            context.sendMessage("```kotlin\n$evaluated```")
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