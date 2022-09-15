package com.paragon.impl.managers.notifications

import java.awt.Color

/**
 * @author Surge
 */
enum class NotificationType(val colour: Int) {

    INFO(Color.GREEN.rgb), WARNING(Color.ORANGE.rgb), ERROR(Color.RED.rgb)

}