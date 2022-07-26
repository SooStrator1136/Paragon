package com.paragon.client.managers.social

import net.minecraft.util.text.TextFormatting

/**
 * @author Surge
 */
enum class Relationship(val textFormatting: TextFormatting) {

    /**
     * Do not attack them
     */
    FRIEND(TextFormatting.GREEN),

    /**
     * Attack them, but do display them as neutral instead of enemy
     */
    NEUTRAL(TextFormatting.GRAY),

    /**
     * Attack them and display them as an enemy
     */
    ENEMY(TextFormatting.RED);

}