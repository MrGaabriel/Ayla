package com.github.mrgaabriel.ayla.utils

import com.github.ajalt.mordant.TermColors
import com.github.mrgaabriel.ayla.Ayla
import com.github.mrgaabriel.ayla.AylaLauncher
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun <R : Any> R.logger(): Lazy<Logger> {
    return lazy { LoggerFactory.getLogger(getClassName(this.javaClass)) }
}

fun <T : Any> getClassName(clazz: Class<T>): String {
    return clazz.name.removeSuffix("\$Companion")
}

val t = TermColors()