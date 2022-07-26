package com.paragon.client.managers.rotation

/**
 * @author Surge
 * @since 23/03/22
 */
enum class RotationPriority(priority: Int) {
    HIGHEST(2), HIGH(1), NORMAL(0), LOW(-1), LOWEST(-2);

    var priority = 0

    init {
        this.priority = priority
    }
}