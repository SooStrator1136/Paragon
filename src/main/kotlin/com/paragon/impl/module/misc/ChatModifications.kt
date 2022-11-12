package com.paragon.impl.module.misc

import com.paragon.Paragon
import com.paragon.impl.module.Aliases
import com.paragon.impl.module.Category
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.util.anyNull
import net.minecraft.util.text.TextComponentString
import net.minecraftforge.client.event.ClientChatEvent
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.io.File
import java.util.*
import kotlin.random.Random


/**
 * @author Surge
 */
@Aliases(["Spammer", "Cryptic", "Encrypt"])
object ChatModifications : Module("ChatModifications", Category.MISC, "Changes the way you send messages") {

    private val coloured = Setting("Coloured", false) describedBy "Adds a '>' before the message"
    private val suffix = Setting("Suffix", true) describedBy "Adds a Paragon suffix to the end of the message"

    private val spammer = Setting("Spammer", false) describedBy "Sends messages in chat (defined in paragon/spammer.txt)"
    private val spammerDelay = Setting("Delay", 1f, 0.1f, 10f, 0.1f) describedBy "Delay between messages (in minutes)" subOf spammer

    val cryptic = Setting("Cryptic", false) describedBy "Encrypts and decrypts messages"
    val requirePrefix = Setting("RequirePrefix", true) describedBy "Require a prefix ('crypt <message>') to be used before the message" subOf cryptic
    private val cancel = Setting("Cancel", false) describedBy "Cancel showing the original chat message" subOf cryptic

    private var lines: Array<String> = arrayOf()
    private var lastMS: Long = 0L

    const val alpha = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private val suffixes: Array<String> = arrayOf(
        "yLdhiv",
        "pTu5mw",
        "9Ebk8f",
        "Hd5agt",
        "FNjlYy",
        "SmI5KV",
        "NhDvvP",
        "ZKMxOp",
        "wkc9Ly",
        "D0BoQt",
        "TyUOUE",
        "kfMs1F",
        "gGNFhF",
        "nJrfKn",
        "zgMqiN",
        "EAuPbt",
        "H8bjLg",
        "Ryy5UJ",
        "5tzUtY",
        "paA9e3",
        "YkecGe",
        "8yrqHF",
        "vrbu7W",
        "vZg1AG",
        "9WI9c2",
        "OVfCyP",
        "w91h1d",
        "FWjhBe",
        "AtNi8R",
        "rIYvCU",
        "vZCy5e",
        "dIwW0s",
        "MpPcsX",
        "wy0J6Q",
        "YGG4Ku",
        "lPwaiN",
        "CNjfvG",
        "6hiCT6",
        "d5Yuv1",
        "b1KNMC",
        "mtb4Ie",
        "5SLfbC",
        "e0Enlj",
        "HQyG25",
        "QhJEqy",
        "2ppBSV",
        "f5rHgK",
        "FzQh0h",
        "lL7esI",
        "F0nkYp",
        "X93GYo",
        "ixCAsZ",
        "SCGvQE",
        "MEVIO2",
        "AACBkP",
        "v6ZghF",
        "cdVQf3",
        "bkC9OE",
        "XKNrnt",
        "xWP6CA",
        "l2R4TP",
        "7vBg9i",
        "5d8t20",
        "E6Q6w9",
        "5zuONf",
        "C6Hd6r",
        "jBQmw3",
        "Q9Uytu",
        "xLiFXR",
        "qdf3eC",
        "OJ9zxE",
        "2vugXy",
        "0Zk61E",
        "7rijaU",
        "TwCFjE",
        "mnFEqF",
        "4QkZdp",
        "8Ui6Fz",
        "seEuG9",
        "TCHrTU",
        "ec5iU9",
        "5Tta37",
        "ExgT1T",
        "P0GmzK",
        "JTA8lg",
        "cOKwPu",
        "D59VyI",
        "9XIHeV",
        "CIf1gn",
        "jMvMWZ",
        "kTEaaC",
        "u9drvb",
        "fgGPBQ",
        "HAYzcD",
        "ruKvpT",
        "vNMCO1",
        "PHiRo2",
        "j9LOEF",
        "QOMIK3",
        "bUq4p1"
    )

    override fun onEnable() {
        loadLines()
    }

    override fun onTick() {
        if (minecraft.anyNull || !spammer.value) {
            return
        }

        if (System.currentTimeMillis() - lastMS > (spammerDelay.value * 60) * 1000) {
            lastMS = System.currentTimeMillis()

            if (lines.isEmpty()) {
                loadLines()
            }

            minecraft.player.sendChatMessage(lines[(Math.random() * lines.size).toInt()])
        }
    }

    @SubscribeEvent
    fun onChat(event: ClientChatEvent) {
        if (event.message.startsWith("/")) {
            return
        }

        if (!Paragon.INSTANCE.commandManager.startsWithPrefix(event.message)) {
            if (coloured.value) {
                event.message = "> " + event.message
            }

            if (suffix.value) {
                event.message = event.message + " | Paragon"
            }
        }

        if (cryptic.value) {
            if (requirePrefix.value && !event.message.startsWith("crypt ") || Paragon.INSTANCE.commandManager.startsWithPrefix(event.message)) {
                return
            }

            val rand = Random.nextInt(8) + 1
            val suffix = Random.nextInt(suffixes.size)

            event.message = encrypt(event.message.replace("crypt ", ""), rand) + rand + suffixes[suffix]
        }
    }

    @SubscribeEvent
    fun onChatReceive(event: ClientChatReceivedEvent) {
        if (cryptic.value) {
            val message: String = event.message.unformattedText

            // Ends with keyboard mash
            if (suffixes.contains(message.substring(message.length - 6, message.length))) {
                val player = event.message.unformattedText.substring(0, event.message.unformattedText.indexOf(">"))

                val rawMessage = message.substring(message.indexOf('>'), message.length - 7)
                val decoded: String = player + decrypt(rawMessage, message.substring(message.length - 7, message.length - 6).toInt())

                if (player.equals(minecraft.player.name, true)) {
                    return
                }

                if (cancel.value) {
                    event.isCanceled = true
                }

                minecraft.ingameGUI.chatGUI.printChatMessage(TextComponentString("$decoded [Decrypted by Paragon]"))
            }
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

    private fun encrypt(message: String, shiftKey: Int): String? {
        var cipherText: String? = ""

        for (element in message.lowercase(Locale.getDefault())) {
            cipherText += if (alpha.contains(element)) {
                val charPosition: Int = alpha.indexOf(element)
                val keyVal = (shiftKey + charPosition) % alpha.length
                val replaceVal: Char = alpha[keyVal]

                replaceVal
            }
            else {
                element
            }
        }

        return cipherText
    }

    private fun decrypt(cipherText: String, shiftKey: Int): String? {
        var message: String? = ""

        for (element in cipherText.lowercase(Locale.getDefault())) {
            if (alpha.contains(element)) {
                val charPosition: Int = alpha.indexOf(element)
                var keyVal = (charPosition - shiftKey) % alpha.length

                if (keyVal < 0) {
                    keyVal += alpha.length
                }

                val replaceVal: Char = alpha[keyVal]
                message += replaceVal
            }
            else {
                message += element
            }
        }

        return message
    }

}