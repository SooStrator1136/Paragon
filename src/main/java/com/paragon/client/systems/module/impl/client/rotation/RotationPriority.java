package com.paragon.client.systems.module.impl.client.rotation;

/**
 * @author Wolfsurge
 * @since 23/03/22
 */
public enum RotationPriority {
    HIGHEST(2),
    HIGH(1),
    NORMAL(0),
    LOW(-1),
    LOWEST(-2);

    private int priority = 0;

    RotationPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
