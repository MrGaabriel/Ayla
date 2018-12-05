package com.github.mrgaabriel.ayla.debug

import com.github.mrgaabriel.ayla.utils.extensions.ayla
import com.github.mrgaabriel.ayla.utils.logger
import kotlinx.coroutines.GlobalScope
import java.util.concurrent.ThreadPoolExecutor
import kotlin.concurrent.thread

object DebugLog {

    val logger by logger()

    fun startCommandHandler() {
        thread(name = "Debug Log Command Handler") {
            while (true) {
                try {
                    val line = readLine()!!.toLowerCase()

                    handleDebugLog(line)
                } catch (e: Exception) {
                    logger.error("Erro", e)
                }
            }
        }
    }

    fun handleDebugLog(line: String) {
        when (line) {
            "threads" -> {
                logger.info("defaultPool.activeCount                : ${(ayla.defaultPool as ThreadPoolExecutor).activeCount}")
                logger.info("defaultCoroutineDispatcher.activeCount : ${(ayla.defaultCoroutineDispatcher.executor as ThreadPoolExecutor).activeCount}")

                logger.info("Active Thread Count                    : ${Thread.getAllStackTraces().keys.size}")
            }

            "rs", "restart" -> {
                System.exit(0)
            }
        }
    }
}