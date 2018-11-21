package com.github.mrgaabriel.ayla.commands.developer

import com.github.mrgaabriel.ayla.commands.AbstractCommand
import com.github.mrgaabriel.ayla.commands.CommandContext
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color
import java.io.PrintWriter
import java.io.StringWriter
import java.time.OffsetDateTime
import javax.script.ScriptContext
import javax.script.ScriptEngineManager

class EvalCommand : AbstractCommand("eval") {

    override fun onlyOwner(): Boolean {
        return true
    }

    override suspend fun run(context: CommandContext) {
        val scriptEngine = ScriptEngineManager().getEngineByName("kotlin")

        scriptEngine.put("scriptContext", scriptEngine.context)
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

        val script = context.args.joinToString(" ")

        val code = """
import com.github.mrgaabriel.ayla.*
import com.github.mrgaabriel.ayla.commands.*
import com.github.mrgaabriel.ayla.config.*
import com.github.mrgaabriel.ayla.dao.*
import com.github.mrgaabriel.ayla.events.*
import com.github.mrgaabriel.ayla.listeners.*
import com.github.mrgaabriel.ayla.tables.*
import com.github.mrgaabriel.ayla.threads.*
import com.github.mrgaabriel.ayla.utils.*
import com.github.mrgaabriel.ayla.utils.extensions.*

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

import java.io.PrintWriter
import java.io.StringWriter
import java.time.OffsetDateTime
import java.awt.Color

$bindings
val deferred = GlobalScope.async {
    try {
        $script
    } catch (e: Exception) {
        exception(e)
    }
}

GlobalScope.launch {
    try {
        val await = deferred.await()

        context.sendMessage("```xl\n" + await + "```")
    } catch (e: Exception) {
        exception(e)
    }
}

fun exception(e: Exception) {
    GlobalScope.launch {
        val stringWriter = StringWriter()
        val printWriter = PrintWriter(stringWriter)
        e.printStackTrace(printWriter)

        val builder = net.dv8tion.jda.core.EmbedBuilder()

        builder.setAuthor("Whoops! \uD83D\uDE2D")
        builder.setDescription("```" + stringWriter.toString().trim() + "```")

        builder.setTimestamp(OffsetDateTime.now())
        builder.setFooter("#trost", null)

        builder.setColor(Color.RED)
        context.sendMessage(builder.build(), context.event.author.asMention)
    }
}
        """.trimIndent()

        try {
            scriptEngine.eval(code)
        } catch (e: Exception) {
            val stringWriter = StringWriter()
            val printWriter = PrintWriter(stringWriter)
            e.printStackTrace(printWriter)

            val builder = net.dv8tion.jda.core.EmbedBuilder()

            builder.setAuthor("Whoops! \uD83D\uDE2D")
            builder.setDescription("```${stringWriter.toString().trim()}```")

            builder.setTimestamp(OffsetDateTime.now())
            builder.setFooter("#trost", null)

            builder.setColor(Color.RED)
            context.sendMessage(builder.build(), context.event.author.asMention)
        }
    }

}