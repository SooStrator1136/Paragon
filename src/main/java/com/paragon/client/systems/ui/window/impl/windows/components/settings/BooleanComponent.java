package com.paragon.client.systems.ui.window.impl.windows.components.settings;

import com.paragon.api.util.render.RenderUtil;
import com.paragon.client.systems.module.impl.client.Colours;
import com.paragon.client.systems.module.setting.Setting;
import com.paragon.client.systems.ui.window.impl.Window;

public class BooleanComponent extends SettingComponent<Boolean> {

    public BooleanComponent(Window window, Setting<Boolean> setting, float x, float y, float width, float height) {
        super(window, setting, x, y, width, height);
    }

    @Override
    public void drawComponent(int mouseX, int mouseY) {
        RenderUtil.drawRect(getX(), getY(), getWidth(), getHeight(), 0x90000000);

        renderText(getSetting().getName(), getX() + 4, getY() + 4, getSetting().getValue() ? Colours.mainColour.getValue().getRGB() : -1);

        super.drawComponent(mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (isHovered(mouseX, mouseY) && isWithinWindowBounds(getWindow().getY() + 40, getWindow().getY() + getWindow().getHeight()) && button == 0) {
            getSetting().setValue(!getSetting().getValue());
        }

        super.mouseClicked(mouseX, mouseY, button);
    }
}
