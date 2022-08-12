package com.paragon.client.systems.module.impl.misc

import com.mojang.realmsclient.gui.ChatFormatting.GRAY
import com.paragon.Paragon
import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.system.backgroundThread
import com.paragon.api.util.system.mainThread
import com.paragon.client.managers.notifications.Notification
import com.paragon.client.managers.notifications.NotificationType
import me.bush.translator.Language
import me.bush.translator.Translation
import me.bush.translator.Translator
import me.bush.translator.languageOf
import net.minecraft.util.text.TextComponentString
import net.minecraftforge.client.event.ClientChatEvent
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
 * @author bush
 * @since 7/6/22
 */
object AutoTranslate : Module("AutoTranslate", Category.MISC, "Automatically translates incoming/outgoing messages") {

    private val incoming = Setting(
        "Incoming",
        true
    ) describedBy "Automatically translate incoming messages"

    private val suffix = Setting(
        "Mark Translation",
        true
    ) describedBy "Suffix translated messages with \"[Translated]\"" visibleWhen { incoming.value }

    private val incomingLang = Setting(
        "In Lang",
        "English"
    ) describedBy "Language to translate incoming messages to" visibleWhen { incoming.value }

    private val outgoing = Setting(
        "Outgoing",
        false
    ) describedBy "Automatically translate outgoing messages"

    private val outgoingLang = Setting(
        "Out Lang",
        "English"
    ) describedBy "Language to translate outgoing messages to" visibleWhen { outgoing.value }

    private val translator = Translator()

    @SubscribeEvent(priority = EventPriority.LOWEST) // We are cancelling the event, so let other listeners do their thing first
    fun onChatReceived(event: ClientChatReceivedEvent) {
        if (!incoming.value) {
            return
        }

        val language = getLanguage(incomingLang.value) ?: return

        event.isCanceled = true

        // Run translation on another thread
        backgroundThread {
            translate(event.message.unformattedText, language) {
                // Send chat on main thread
                mainThread {
                    val messageSuffix = if (suffix.value && sourceLanguage != targetLanguage)
                        "$GRAY [Translated]" else ""
                    minecraft.ingameGUI?.chatGUI?.printChatMessage(TextComponentString(translatedText + messageSuffix))
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onChatSend(event: ClientChatEvent) {
        if (!outgoing.value || Paragon.INSTANCE.commandManager.startsWithPrefix(event.message)) {
            return
        }

        val language = getLanguage(outgoingLang.value) ?: return

        event.isCanceled = true

        backgroundThread {
            translate(event.message, language) {
                mainThread {
                    minecraft.player?.sendChatMessage(translatedText)
                }
            }
        }
    }

    private suspend inline fun translate(text: String, language: Language, block: Translation.() -> Unit) {
        translator.translateCatching(text, language).onFailure {
            Paragon.INSTANCE.notificationManager.addNotification(Notification("Could not process translation request. Disabling AutoTranslate", NotificationType.ERROR))
            toggle()
        }.getOrNull()?.run(block)
    }

    private fun getLanguage(language: String) = languageOf(language).also {
        if (it == null) {
            Paragon.INSTANCE.notificationManager.addNotification(
                Notification("\"$language\" is not a valid language! Disabling AutoTranslate.", NotificationType.ERROR)
            )
            toggle()
        }
    }

}