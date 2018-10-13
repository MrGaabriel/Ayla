package me.mrgaabriel.ayla.threads

import me.mrgaabriel.ayla.utils.ayla
import me.mrgaabriel.ayla.utils.config
import me.mrgaabriel.ayla.utils.saveConfig

class TempbanThread : Thread("Unban Tempbanned Users Thread") {

    override fun run() {
        while (true) {
            checkUsers()

            Thread.sleep(10000)
        }
    }

    fun checkUsers() {
        ayla.shardManager.guilds.forEach { guild ->
            val config = guild.config
            val bannedUsers = config.userData.filter { it.banned }

            bannedUsers.forEach {
                if (it.bannedUntil < System.currentTimeMillis()) { // O ban expirou!
                    guild.controller.unban(it.userId).reason("Tempban expirado").queue()

                    it.banned = false
                    it.bannedUntil = 0.toLong()

                    config.saveUserData(it)
                    guild.saveConfig(config)
                }
            }
        }
    }
}