package com.paragon.client.systems.module.impl.misc

import com.mojang.realmsclient.gui.ChatFormatting.GRAY
import com.paragon.Paragon
import com.paragon.api.util.system.backgroundThread
import com.paragon.api.util.system.mainThread
import com.paragon.client.managers.CommandManager
import com.paragon.client.managers.notifications.Notification
import com.paragon.client.managers.notifications.NotificationType
import com.paragon.client.systems.module.Category
import com.paragon.client.systems.module.Module
import com.paragon.client.systems.module.setting.Setting
import me.bush.translator.Language
import me.bush.translator.Translation
import me.bush.translator.Translator
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
    private val incoming = Setting("Incoming", true)
        .setDescription("Automatically translate incoming messages")

    private val suffix = Setting("Mark Translation", true)
        .setDescription("Suffix translated messages with \"[Translated]\"")
        .setVisibility { incoming.getValue() }

    private val incomingLang = Setting("In Lang", Language.ENGLISH)
        .setDescription("Language to translate incoming messages to")
        .setVisibility { incoming.getValue() }

    private val outgoing = Setting("Outgoing", false)
        .setDescription("Automatically translate outgoing messages")

    private val outgoingLang = Setting("Out Lang", Language.ENGLISH)
        .setDescription("Language to translate outgoing messages to")
        .setVisibility { outgoing.getValue() }

    private val translator = Translator()

    @SubscribeEvent(priority = EventPriority.LOWEST) // We are cancelling the event, so let other listeners do their thing first
    fun onChatReceived(event: ClientChatReceivedEvent) {
        if (!incoming.getValue()) {
            return
        }

        event.isCanceled = true

        // Run translation on another thread
        backgroundThread {
            translate(event.message.unformattedText, incomingLang.getValue()) {
                // Send chat on main thread
                mainThread {
                    val messageSuffix = if (suffix.getValue() && sourceLanguage != targetLanguage)
                        "$GRAY [Translated]" else ""
                    mc.ingameGUI?.chatGUI?.printChatMessage(TextComponentString(translatedText + messageSuffix))
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onChatSend(event: ClientChatEvent) {
        if (!outgoing.getValue() || event.message.startsWith("/") || Paragon.INSTANCE.commandManager.startsWithPrefix(event.message)) {
            return
        }

        event.isCanceled = true

        backgroundThread {
            translate(event.message, outgoingLang.getValue()) {
                mainThread {
                    mc.player?.sendChatMessage(translatedText)
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
}
