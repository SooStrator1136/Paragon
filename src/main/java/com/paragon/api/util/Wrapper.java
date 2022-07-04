package com.paragon.api.util;

import net.minecraft.client.Minecraft;

public interface Wrapper {

    Minecraft mc = Minecraft.getMinecraft();

    default boolean nullCheck() {
        return mc.player == null || mc.world == null;
    }

    default boolean isHovered(float x, float y, float width, float height, int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

}
