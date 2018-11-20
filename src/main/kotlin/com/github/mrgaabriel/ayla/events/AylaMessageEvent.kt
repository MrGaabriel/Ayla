package com.github.mrgaabriel.ayla.events

import net.dv8tion.jda.core.entities.Message

class AylaMessageEvent(val message: Message) {

    val channel = message.channel
    val textChannel = message.textChannel ?: null

    val author = message.author
    val member = message.member ?: null

    val guild = message.guild ?: null

    val jda = message.jda
}