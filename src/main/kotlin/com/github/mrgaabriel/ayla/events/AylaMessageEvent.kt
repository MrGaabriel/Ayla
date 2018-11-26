package com.github.mrgaabriel.ayla.events

import net.dv8tion.jda.core.entities.Message

class AylaMessageEvent(val message: Message) {

    val channel = message.channel
    val textChannel = message.textChannel

    val author = message.author
    val member = message.member

    val guild = message.guild

    val jda = message.jda
}