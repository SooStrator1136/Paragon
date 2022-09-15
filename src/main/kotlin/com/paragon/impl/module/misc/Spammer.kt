package com.paragon.impl.module.misc

import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.impl.module.Category
import com.paragon.util.anyNull
import java.io.File

object Spammer : Module("Spammer", Category.MISC, "Spams messages in chat (defined in spammer.txt)") {

    private val delay = Setting(
        "Delay", 1f, 0.1f, 10f, 0.1f
    ) describedBy "Delay between messages (in minutes)"

    private var lines: Array<String> = arrayOf()
    private var lastMS: Long = 0L

    override fun onEnable() {
        loadLines()
    }

    override fun onTick() {
        if (minecraft.anyNull) {
            return
        }

        if (System.currentTimeMillis() - lastMS > (delay.value * 60) * 1000) {
            lastMS = System.currentTimeMillis()

            if (lines.isEmpty()) {
                loadLines()
            }

            minecraft.player.sendChatMessage(lines[(Math.random() * lines.size).toInt()])
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