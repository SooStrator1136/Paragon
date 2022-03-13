package com.paragon.client.managers.social;

import net.minecraft.util.text.TextFormatting;

/**
 * @author Wolfsurge
 */
public enum Relationship {
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

    private TextFormatting textFormatting;

    Relationship(TextFormatting formatting) {
        this.textFormatting = formatting;
    }

    public TextFormatting getTextFormatting() {
        return textFormatting;
    }
}
