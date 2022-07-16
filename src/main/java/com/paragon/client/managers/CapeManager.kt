package com.paragon.client.managers

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.util.*

class CapeManager {

    private val capedPlayers: MutableMap<String, Cape> = HashMap()

    fun isCaped(username: String) = capedPlayers.containsKey(username) || username.startsWith("Player")

    fun getCape(username: String) = if (username.startsWith("Player")) Cape.BASED else capedPlayers[username]

    init {
        try {
            val reader = BufferedReader(InputStreamReader(URL("https://ParagonBot.wolfsurge.repl.co/capes").openStream()))

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val data = line!!.split(":".toRegex()).toTypedArray()

                capedPlayers[data[0]] = java.lang.Enum.valueOf(Cape::class.java, data[1].uppercase(Locale.getDefault()))
            }

            reader.close()
        } catch (e: IOException) {
            e.printStackTrace()
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
        BASED("textures/cape/based.png");
    }

}