package com.paragon.client.managers.notifications;

import java.awt.*;

public enum NotificationType {
    INFO(Color.GREEN.getRGB()),
    WARNING(Color.ORANGE.getRGB()),
    ERROR(Color.RED.getRGB());

    private final int colour;

    NotificationType(int colour) {
        this.colour = colour;
    }

    public int getColour() {
        return this.colour;
    }
}
