package com.github.mrgaabriel.ayla.utils

import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.net.HttpURLConnection
import java.util.*

object WebsiteUtils {

    fun allowMethods(vararg methods: String) {
        try {
            val methodsField = HttpURLConnection::class.java.getDeclaredField("methods")

            val modifiersField = Field::class.java.getDeclaredField("modifiers")
            modifiersField.isAccessible = true
            modifiersField.setInt(methodsField, methodsField.modifiers and Modifier.FINAL.inv())

            methodsField.isAccessible = true

            val oldMethods = methodsField.get(null) as Array<String>
            val methodsSet = LinkedHashSet(Arrays.asList(*oldMethods))
            methodsSet.addAll(Arrays.asList(*methods))
            val newMethods = methodsSet.toTypedArray()

            methodsField.set(null, newMethods)/*static field*/
        } catch (e: NoSuchFieldException) {
            throw IllegalStateException(e)
        } catch (e: IllegalAccessException) {
            throw IllegalStateException(e)
        }
    }
}