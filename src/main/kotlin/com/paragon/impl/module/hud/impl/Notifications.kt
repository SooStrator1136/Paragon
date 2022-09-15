package com.paragon.impl.module.hud.impl

import com.paragon.Paragon
import com.paragon.impl.module.hud.HUDEditorGUI
import com.paragon.impl.module.hud.HUDModule
import com.paragon.impl.setting.Setting
import com.paragon.util.render.RenderUtil.drawRect
import com.paragon.util.render.font.FontUtil
import java.awt.Color

object Notifications : HUDModule("Notifications", "Where the notifications will render") {

    private val renderType = Setting("Render Type", RenderType.DISPLAY, null, null, null) describedBy "The way to render the notifications"
    private val direction = Setting("Direction", Direction.DOWN, null, null, null) describedBy "The vertical direction of the notifications"
    private val limit = Setting("Limit", 3f, 1f, 20f, 1f) describedBy "The limit to the amount of notifications displayed" visibleWhen { renderType.value == RenderType.DISPLAY }

    override fun render() {
        if (minecraft.currentScreen is HUDEditorGUI) {
            drawRect(x, y, width, height, Color(23, 23, 23, 200).rgb)
            FontUtil.drawStringWithShadow("[Notifications]", x + 5, y + 5, -1)
        }

        else {
            if (renderType.value == RenderType.DISPLAY) {
                var y = this.y

                for (notification in Paragon.INSTANCE.notificationManager.notifications) {
                    if (Paragon.INSTANCE.notificationManager.notifications.size >= limit.value + 1 && Paragon.INSTANCE.notificationManager.notifications[limit.value.toInt()] == notification) {
                        break
                    }

                    notification.render(y)

                    when (direction.value) {
                        Direction.UP -> y += (-35 * notification.animation.getAnimationFactor()).toFloat()
                        Direction.DOWN -> y += (35 * notification.animation.getAnimationFactor()).toFloat()
                        else -> {}
                    }
                }

                // bad code 2: electric boogaloo
                var i = 0
                while (i < limit.value && i < Paragon.INSTANCE.notificationManager.notifications.size - 1) {
                    if (Paragon.INSTANCE.notificationManager.notifications[i].hasFinishedAnimating()) {
                        Paragon.INSTANCE.notificationManager.notifications.remove(Paragon.INSTANCE.notificationManager.notifications[i])
                    }

                    i++
                }
            }
            else if (renderType.value == RenderType.CHAT) {
                for (notification in Paragon.INSTANCE.notificationManager.notifications) {
                    Paragon.INSTANCE.commandManager.sendClientMessage(notification.message, false)
                }

                Paragon.INSTANCE.notificationManager.notifications.clear()
            }
        }
    }

    override var width: Float = 300f

    override var height: Float = 30f

    enum class RenderType {
        /**
         * Displays a rect
         */
        DISPLAY,

        /**
         * Sends a chat message
         */
        CHAT
    }

    enum class Direction {
        /**
         * Notification Y will decrease
         */
        UP,

        /**
         * Notification Y will increase
         */
        DOWN
    }

}