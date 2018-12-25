package com.github.mrgaabriel.ayla.website.routes.controllers.callbacks

import com.github.kevinsawicki.http.HttpRequest
import com.github.mrgaabriel.ayla.utils.Constants
import com.github.mrgaabriel.ayla.utils.Static
import com.github.mrgaabriel.ayla.utils.extensions.await
import com.github.mrgaabriel.ayla.utils.extensions.ayla
import com.github.salomonbrys.kotson.*
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.GET
import org.jooby.mvc.Path
import java.io.File
import java.net.URL
import kotlin.concurrent.thread

@Path("/api/update-available")
class UpdateAvailableCallbackController {

    @GET
    fun handle(req: Request, res: Response) {
        res.type(MediaType.json)

        // TODO: Mudar o sistema de autenticação
        val header = req.header("Authorization")
        if (!header.isSet) {
            res.send(jsonObject(
                "api:code" to 1,
                "error" to "Needs authorization"
            ))

            return
        }

        val content = header.value()

        if (content != ayla.config.websiteMasterToken) {
            res.send(jsonObject(
                "api:code" to 1,
                "error" to "Needs valid authorization"
            ))

            return
        }

        thread {
            Thread.sleep(5000)

            val body = HttpRequest.get("https://jenkins.mrgaabriel.space/job/Ayla/lastSuccessfulBuild/api/json")
                .userAgent(Constants.USER_AGENT)
                .body()

            val payload = Static.JSON_PARSER.parse(body).obj

            val items = payload["changeSet"]["items"].array

            var message = "Chegaram novidades para mim! Novidades:"

            if (items.size() == 0) {
                message += "\nNada! (apenas um rebuild)..."
            } else {
                for (item in items) {
                    message += "\n - `${item["comment"].string}`"
                }
            }

            message += "\nVou reiniciar e já volto!.."

            val channel = ayla.shardManager.getTextChannelById("521782715066875907")
            channel.sendMessage(message.substring(0..2000)).queue()

            val artifacts = payload["artifacts"].array
            val firstArtifact = artifacts.first()
            val relativePath = firstArtifact["relativePath"].string

            val bytes = HttpRequest.get("https://jenkins.mrgaabriel.space/job/Ayla/lastSuccessfulBuild/artifact/$relativePath")
                .userAgent(Constants.USER_AGENT)
                .bytes()

            File("Ayla-Update.jar").writeBytes(bytes)

            ayla.shardManager.shutdown()
            System.exit(0)
        }

        res.send("{}")
    }
}
