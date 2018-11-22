package com.github.mrgaabriel.ayla.website

import com.github.mrgaabriel.ayla.website.routes.controllers.MainPageController
import org.jooby.Kooby

class Routes : Kooby({
    use(MainPageController::class.java)
})