package com.paragon.client.systems.module.impl.misc

import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import java.io.File

object Spammer : Module("Spammer", Category.MISC, "Spams messages in chat (defined in spammer.txt)") {

    private val delay = Setting("Delay", 1f, 0.1f, 10f, 0.1f)
        .setDescription("Delay between messages (in minutes)")

    private var lines: Array<String> = arrayOf()
    private var lastMS: Long = 0L

    override fun onEnable() {
        loadLines()
    }

    override fun onTick() {
        if (nullCheck()) {
            return
        }

        if (System.currentTimeMillis() - lastMS > (delay.getValue() * 60) * 1000) {
            lastMS = System.currentTimeMillis()

            if (lines.isEmpty()) {
                loadLines()
            }

            mc.player.sendChatMessage(lines[(Math.random() * lines.size).toInt()])
        }
    }

    private fun loadLines() {
        val file = File("paragon/spammer.txt")

        if (!file.exists()) {
            file.createNewFile()
            file.writeText("spammed by paragon client, llc")
        }

        lines = file.readLines().toTypedArray()
    }

}