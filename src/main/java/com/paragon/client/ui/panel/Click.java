package com.paragon.client.ui.panel;

public enum Click {

    LEFT(0),
    RIGHT(1),
    MIDDLE(2),
    SIDE_ONE(3),
    SIDE_TWO(4);

    private int button;

    Click(int button) {
        this.button = button;
    }

    public int getButton() {
        return button;
    }

    public static Click getClick(int button) {
        return Click.values()[button];
    }

}
