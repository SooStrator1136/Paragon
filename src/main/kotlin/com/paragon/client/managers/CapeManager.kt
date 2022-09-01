package com.paragon.client.managers

import com.paragon.Paragon
import com.paragon.api.util.system.ResourceUtil
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import java.util.*

class CapeManager {

    private val capedPlayers: MutableMap<String, Cape> = HashMap()

    fun isCaped(username: String) = capedPlayers.containsKey(username)// || username.startsWith("Player")

    fun getCape(username: String) = capedPlayers[username]

    init {
        runCatching {
            runBlocking {
                String(
                    ResourceUtil.client.get("https://ParagonBot.wolfsurge.repl.co/capes").readBytes()
                ).split(System.lineSeparator()).forEach {
                    val data = it.split(":")
                    capedPlayers[data[0]] = Cape.valueOf(data[1].uppercase(Locale.getDefault()))
                }
            }
        }.onFailure {
            Paragon.INSTANCE.logger.error("Couldn't fetch capes! Looks like the host is down.")
        }
    }

    enum class Cape(val path: String) {
        /**
         * Cape for cool ppl (boosters etc.)
         */
        COOL("textures/cape/cool.png"),

        /**
         * Cape for based ppl (contributors etc.)
         */
        BASED("textures/cape/based.png")
    }

}