package com.paragon.client.systems.module.impl.misc

import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import net.minecraft.util.text.TextComponentString
import net.minecraftforge.client.event.ClientChatEvent
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*
import kotlin.random.Random

/**
 * @author Surge
 * @since 08/07/22
 */
object Cryptic : Module("Cryptic", Category.MISC, "Encrypts and decrypts messages that can only be seen by other Paragon users") {

    private val requirePrefix = Setting(
        "RequirePrefix",
        true
    ) describedBy "Require a prefix ('crypt <message>') to be used before the message"

    private val cancel = Setting(
        "Cancel",
        false
    ) describedBy "Cancel showing the original chat message"

    const val alpha = "abcdefghijklmnopqrstuvwxyz"
    private val suffixes: Array<String> = arrayOf(
        "1p421p",
        "74jcd9",
        "ju9287",
        "odsn21",
        "98h421",
        "i9j89g",
        "i98h23",
        "jkind7",
        "o082jf",
        "prgn12",
        "fckyou",
        "retard",
        "u8r2h3",
        "h8h7y3"
    )

    @SubscribeEvent
    fun onChatSend(event: ClientChatEvent) {
        if (requirePrefix.value && !event.message.startsWith("crypt ")) {
            return
        }

        val rand = Random.nextInt(8) + 1
        val suffix = Random.nextInt(suffixes.size)

        event.message = encrypt(event.message.replace("crypt ", ""), rand) + rand + suffixes[suffix]
    }

    @SubscribeEvent
    fun onChatReceive(event: ClientChatReceivedEvent) {
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

    private fun encrypt(message: String, shiftKey: Int): String? {
        var cipherText: String? = ""

        for (element in message.lowercase(Locale.getDefault())) {
            cipherText += if (alpha.contains(element)) {
                val charPosition: Int = alpha.indexOf(element)
                val keyVal = (shiftKey + charPosition) % 26
                val replaceVal: Char = alpha[keyVal]

                replaceVal
            } else {
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
                var keyVal = (charPosition - shiftKey) % 26

                if (keyVal < 0) {
                    keyVal += alpha.length
                }

                val replaceVal: Char = alpha[keyVal]
                message += replaceVal
            } else {
                message += element
            }
        }

        return message
    }

}