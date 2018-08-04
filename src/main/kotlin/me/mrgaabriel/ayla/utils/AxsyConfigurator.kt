package me.mrgaabriel.ayla.utils

import ch.qos.logback.classic.jul.*
import org.slf4j.bridge.*
import java.util.logging.*

class AxsyConfigurator: LevelChangePropagator() {

    init {
        if(!SLF4JBridgeHandler.isInstalled()) {
            LogManager.getLogManager().reset()
            SLF4JBridgeHandler.install()
        }
        setResetJUL(true)
    }

}