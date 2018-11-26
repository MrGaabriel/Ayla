package com.github.mrgaabriel.ayla.website.routes.controllers

import com.github.mrgaabriel.ayla.utils.extensions.ayla
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.GET
import org.jooby.mvc.Path

@Path("/invite")
class InviteController {

    @GET
    fun handle(req: Request, res: Response) {
        res.status(301)
        res.redirect("https://discordapp.com/api/oauth2/authorize?client_id=${ayla.config.clientId}&permissions=2013266102&redirect_uri=https%3A%2F%2Fayla.space&response_type=code&scope=bot%20identify%20email")
    }
}