package com.github.mrgaabriel.ayla.utils.extensions

import com.github.mrgaabriel.ayla.AylaLauncher
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.requests.RestAction
import net.dv8tion.jda.core.utils.MiscUtil
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun <T> RestAction<T>.await(): T {
    return suspendCoroutine { cont ->
        this.queue({ cont.resume(it) }, { cont.resumeWithException(it) })
    }
}

val User.tag get() = "${this.name}#${this.discriminator}"
val ayla = AylaLauncher.ayla

fun String.isValidSnowflake(): Boolean {
    try {
        MiscUtil.parseSnowflake(this)
        return true
    } catch (e: Exception) {
        return false
    }
}