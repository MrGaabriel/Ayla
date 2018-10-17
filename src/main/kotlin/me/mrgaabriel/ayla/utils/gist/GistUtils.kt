package me.mrgaabriel.ayla.utils.gist

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.set
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import me.mrgaabriel.ayla.utils.Constants
import me.mrgaabriel.ayla.utils.ayla

object GistUtils {

    /**
     * Creates a Gist using the token provided on config
     *
     * @param content = content of Gist
     * @param fileName = file name of Gist
     *
     * @return Gist URL
     */
    fun createGist(content: String, description: String, public: Boolean, fileName: String): String {
        val payload = JsonObject()

        payload["description"] = description
        payload["public"] = public

        val file = JsonObject()
        file["content"] = content

        val files = JsonObject()
        files[fileName] = file

        payload["files"] = files

        println(payload)
        val request = HttpRequest.get("https://api.github.com/gists")
                .userAgent(Constants.USER_AGENT)
                .authorization("token ${ayla.config.gistToken}")
                .send(payload.toString())

        val receivedPayload = JsonParser().parse(request.body())
        println(receivedPayload)

        val url = receivedPayload["html_url"].string

        return url
    }

}