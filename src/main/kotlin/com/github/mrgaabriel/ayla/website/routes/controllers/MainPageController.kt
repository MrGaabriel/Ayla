package com.github.mrgaabriel.ayla.website.routes.controllers

import com.github.mrgaabriel.ayla.frontend.MainPage
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.GET
import org.jooby.mvc.Path

@Path("/")
class MainPageController {

    @GET
    fun handle(req: Request, res: Response) {
        res.send(MainPage.generate())
    }
}