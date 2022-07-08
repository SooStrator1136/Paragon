package com.paragon.client.ui.window.impl.windows.components.settings;

import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.string.StringUtil;
import com.paragon.api.setting.Setting;
import com.paragon.client.ui.window.impl.Window;
import net.minecraft.util.text.TextFormatting;

public class ModeComponent extends SettingComponent<Enum<?>> {

    public ModeComponent(Window window, Setting<Enum<?>> setting, float x, float y, float width, float height) {
        super(window, setting, x, y, width, height);
    }

    @Override
    public void drawComponent(int mouseX, int mouseY) {
        RenderUtil.drawRect(getX(), getY(), getWidth(), getHeight(), 0x90000000);

        renderText(getSetting().getName() + " " + TextFormatting.GRAY + StringUtil.getFormattedText(getSetting().getValue()), getX() + 4, getY() + 4, -1);

        super.drawComponent(mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (isHovered(mouseX, mouseY) && isWithinWindowBounds(getWindow().getY() + 40, getWindow().getY() + getWindow().getHeight()) && button == 0) {
            getSetting().setValue(getSetting().getNextMode());
        }

        super.mouseClicked(mouseX, mouseY, button);
    }
}
