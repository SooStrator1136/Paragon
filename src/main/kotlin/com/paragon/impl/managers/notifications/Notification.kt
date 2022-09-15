package com.paragon.impl.managers.notifications

import com.paragon.util.render.font.FontUtil
import com.paragon.impl.module.hud.impl.Notifications
import com.paragon.util.render.RenderUtil
import me.surge.animation.Animation
import me.surge.animation.Easing

/**
 * @author Surge
 */
class Notification(val message: String, val type: NotificationType) {

    val animation: Animation = Animation({ 500f }, false, { Easing.EXPO_IN_OUT })
    private var started = false
    private var reachedFirst = false
    private var renderTicks = 0

    fun render(y: Float) {
        if (!started) {
            animation.state = true
            started = true
        }

        val width = FontUtil.getStringWidth(message) + 10
        val x = Notifications.x

        RenderUtil.pushScissor(Notifications.x + (150 - 150) * animation.getAnimationFactor(), y.toDouble(), 300 * animation.getAnimationFactor(), 45.0)
        RenderUtil.drawRect(x + 150 - width / 2f, y, width, 30f, -0x70000000)
        FontUtil.renderCenteredString(message, x + 150, y + 15f, -1, true)
        RenderUtil.drawRect(x + 150 - width / 2f, y, width, 1f, type.colour)
        RenderUtil.popScissor()

        if (animation.getAnimationFactor() == 1.0 && !reachedFirst) {
            reachedFirst = true
        }
        if (reachedFirst) {
            renderTicks++
        }
        if (renderTicks == 300) {
            animation.state = false
        }
    }

    fun hasFinishedAnimating() = animation.getAnimationFactor() == 0.0 && reachedFirst

}