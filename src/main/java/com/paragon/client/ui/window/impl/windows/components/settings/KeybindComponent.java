package com.paragon.client.ui.window.impl.windows.components.settings;

import com.paragon.Paragon;
import com.paragon.api.event.client.SettingUpdateEvent;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.setting.Bind;
import com.paragon.api.setting.Setting;
import com.paragon.client.ui.window.impl.Window;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

public class KeybindComponent extends SettingComponent<Bind> {

    private boolean isListening = false;

    public KeybindComponent(Window window, Setting<Bind> setting, float x, float y, float width, float height) {
        super(window, setting, x, y, width, height);
    }

    @Override
    public void drawComponent(int mouseX, int mouseY) {
        RenderUtil.drawRect(getX(), getY(), getWidth(), getHeight(), 0x90000000);

        renderText(getSetting().getName() + " " + TextFormatting.GRAY + (isListening ? "..." : getSetting().getValue().getButtonName()), getX() + 4, getY() + 4, -1);

        super.drawComponent(mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (isListening) {
            isListening = false;

            getSetting().getValue().setDevice(Bind.Device.MOUSE);
            getSetting().getValue().setButtonCode(button);

            return;
        }

        if (button == 0 && isWithinWindowBounds(getWindow().getY() + 40, getWindow().getY() + getWindow().getHeight()) && isHovered(mouseX, mouseY)) {
            isListening = true;
            SettingUpdateEvent settingUpdateEvent = new SettingUpdateEvent(getSetting());
            Paragon.INSTANCE.getEventBus().post(settingUpdateEvent);
        }

        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (isListening) {
            isListening = false;

            getSetting().getValue().setDevice(Bind.Device.KEYBOARD);

            if (keyCode == Keyboard.KEY_DELETE || keyCode == Keyboard.KEY_BACK) {
                getSetting().getValue().setButtonCode(0);
                return;
            }

            getSetting().getValue().setButtonCode(keyCode);
        }
    }
}
