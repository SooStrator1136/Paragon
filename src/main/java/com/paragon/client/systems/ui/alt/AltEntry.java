package com.paragon.client.systems.ui.alt;

import com.paragon.api.util.render.GuiUtil;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.render.TextRenderer;
import com.paragon.client.managers.alt.Alt;
import net.minecraft.client.Minecraft;

public class AltEntry implements TextRenderer {

    private Alt alt;
    private float offset;

    public AltEntry(Alt alt, float offset) {
        this.alt = alt;
        this.offset = offset;
    }

    public void drawAlt(int mouseX, int mouseY, int screenWidth) {
        RenderUtil.drawRect(0, offset, screenWidth, 20, 0x95000000);
        renderCenteredString(alt.getEmail(), screenWidth / 2f, offset + 10, -1, true);
    }

    public void clicked(int mouseX, int mouseY, int screenWidth) {
        if (GuiUtil.mouseOver(0, offset, screenWidth, offset + 20, mouseX, mouseY)) {
            alt.login();
        }
    }

    public float getOffset() {
        return offset;
    }

    public void setOffset(float newOffset) {
        this.offset = newOffset;
    }

}
