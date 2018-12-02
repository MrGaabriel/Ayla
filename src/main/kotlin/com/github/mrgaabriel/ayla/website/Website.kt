package com.github.mrgaabriel.ayla.website

import com.github.mrgaabriel.ayla.utils.extensions.ayla
import com.github.mrgaabriel.ayla.utils.logger
import org.jooby.Err
import org.jooby.Kooby
import org.jooby.Request
import java.io.File

class Website(val websiteUrl: String) : Kooby({
    val logger by logger()

    port(ayla.config.websitePort)
    assets("/**", File("frontend").toPath()).onMissing(0)

    before { req, rsp ->
        req.set("start", System.currentTimeMillis())

        logger.info("${req.ip} -> ${req.method()} ${req.path()} (${req.userAgent()})")
    }

    complete("*") { req, rsp, cause ->
        if (cause.isPresent) {
            val cause = cause.get()

            if (cause is Err) {
                if (cause.statusCode() == 404)
                    return@complete
            }

            logger.error("${req.ip} -> ${req.method()} ${req.path()} (${req.userAgent()}) - ERROR!", cause)
            return@complete
        }

        val start = req.get<Long>("start")
        logger.info("${req.ip} -> ${req.method()} ${req.path()} (${req.userAgent()}) - OK! ${System.currentTimeMillis() - start}ms")
    }

    err { req, rsp, err ->
        if (err.statusCode() == 404) {
            rsp.send("Erro 404!!!")
        }
    }

    use(Routes())
})

fun Request.userAgent(): String {
    return this.header("UserProfile-Agent").value()
}

val Request.ip: String
    get() {
        val forwardedForHeader = this.header("X-Forwarded-For")
        return if (forwardedForHeader.isSet)
            forwardedForHeader.value()
        else
            this.ip()
    }

val Request.path: String
    get() {
        val queryString = if (this.queryString().isPresent)
            "?" + this.queryString().get()
        else
            ""

        return this.path() + queryString
    }