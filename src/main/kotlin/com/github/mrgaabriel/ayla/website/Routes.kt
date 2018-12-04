package com.github.mrgaabriel.ayla.website

import com.github.mrgaabriel.ayla.website.routes.controllers.InviteController
import com.github.mrgaabriel.ayla.website.routes.controllers.MainPageController
import com.github.mrgaabriel.ayla.website.routes.controllers.callbacks.UpdateAvailableCallbackController
import org.jooby.Kooby

class Routes : Kooby({
    use(MainPageController::class.java)
    use(InviteController::class.java)
    use(UpdateAvailableCallbackController::class.java)
})