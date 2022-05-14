package com.paragon.client.systems.ui.window.impl.windows.components.settings;

import com.paragon.Paragon;
import com.paragon.api.event.client.SettingUpdateEvent;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.client.systems.module.impl.client.Colours;
import com.paragon.client.systems.module.setting.Setting;
import com.paragon.client.systems.ui.window.impl.Window;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

import java.util.concurrent.atomic.AtomicInteger;

public class KeybindComponent extends SettingComponent<AtomicInteger> {

    private boolean isListening = false;

    public KeybindComponent(Window window, Setting<AtomicInteger> setting, float x, float y, float width, float height) {
        super(window, setting, x, y, width, height);
    }

    @Override
    public void drawComponent(int mouseX, int mouseY) {
        RenderUtil.drawRect(getX(), getY(), getWidth(), getHeight(), 0x90000000);

        renderText(getSetting().getName() + " " + TextFormatting.GRAY + (isListening ? "..." : Keyboard.getKeyName(getSetting().getValue().get())), getX() + 4, getY() + 4, -1);

        super.drawComponent(mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (button == 0 && isWithinWindowBounds(getWindow().getY() + 40, getWindow().getY() + getWindow().getHeight()) && isHovered(mouseX, mouseY)) {
            isListening = true;
            SettingUpdateEvent settingUpdateEvent = new SettingUpdateEvent(getSetting());
            Paragon.INSTANCE.getEventBus().post(settingUpdateEvent);
        }

        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {

    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (isListening) {
            if (keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_DELETE) {
                getSetting().getValue().set(0);
                isListening = false;
                return;
            }

            getSetting().getValue().set(keyCode);
            isListening = false;
        }
    }
}
