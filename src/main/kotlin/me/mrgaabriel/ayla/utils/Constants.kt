package me.mrgaabriel.ayla.utils

import com.mongodb.client.model.UpdateOptions
import java.awt.Color

object Constants {

    val UPDATE_OPTIONS = UpdateOptions().upsert(true)
    val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:62.0) Gecko/20100101 Firefox/62.0"

    val REDDIT_ORANGE_RED = Color(255, 67, 0)
}