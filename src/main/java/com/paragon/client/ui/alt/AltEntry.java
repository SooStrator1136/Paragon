package com.paragon.client.ui.alt;

import com.paragon.api.util.Wrapper;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.render.ITextRenderer;
import com.paragon.client.managers.alt.Alt;

public final class AltEntry implements ITextRenderer, Wrapper {

    private final Alt alt;
    private float offset;

    public AltEntry(Alt alt, float offset) {
        this.alt = alt;
        this.offset = offset;
    }

    public void drawAlt(int mouseX, int mouseY, int screenWidth) {
        RenderUtil.drawRect(0, offset, screenWidth, 20, AltManagerGUI.selectedAltEntry == this ? 0x95111111 : 0x95000000);
        renderCenteredString(alt.getEmail(), screenWidth / 2f, offset + 10, -1, true);
    }

    public void clicked(int mouseX, int mouseY, int screenWidth) {
        if (isHovered(0, offset, screenWidth, 20, mouseX, mouseY)) {
            alt.login();
        }
    }

    public float getOffset() {
        return offset;
    }

    public void setOffset(float newOffset) {
        this.offset = newOffset;
    }

    public Alt getAlt() {
        return alt;
    }

}
