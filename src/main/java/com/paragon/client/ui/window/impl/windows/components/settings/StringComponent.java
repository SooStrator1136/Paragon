package com.paragon.client.ui.window.impl.windows.components.settings;

import com.paragon.api.setting.Setting;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.client.systems.module.impl.client.Colours;
import com.paragon.client.ui.window.impl.Window;
import net.minecraft.util.ChatAllowedCharacters;
import org.lwjgl.input.Keyboard;

public class StringComponent extends SettingComponent<String> {

    private boolean focused = false;

    public StringComponent(Window window, Setting<String> setting, float x, float y, float width, float height) {
        super(window, setting, x, y, width, height);
    }

    @Override
    public void drawComponent(int mouseX, int mouseY) {
        RenderUtil.drawRect(getX(), getY(), getWidth(), getHeight(), 0x90000000);

        renderText(getSetting().getName(), getX() + 4, getY() + 4, -1);
        renderText(getSetting().getValue() + (focused ? "_" : ""), getX() + getWidth() - getStringWidth(getSetting().getValue() + (focused ? "_" : "")) - 2, getY() + 4, -1);

        super.drawComponent(mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (isHovered(mouseX, mouseY) && isWithinWindowBounds(getWindow().getY() + 40, getWindow().getY() + getWindow().getHeight()) && button == 0) {
            focused = !focused;
        }

        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (isWithinWindowBounds(getWindow().getY() + 40, getWindow().getY() + getWindow().getHeight()) && focused) {
            if (keyCode == Keyboard.KEY_BACK) {
                if (getSetting().getValue().length() > 0){
                    getSetting().setValue(getSetting().getValue().substring(0, getSetting().getValue().length() - 1));
                }
            }

            else if (keyCode == Keyboard.KEY_RETURN) {
                focused = false;
            }

            else if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
                getSetting().setValue(getSetting().getValue() + typedChar);
            }
        }

        super.keyTyped(typedChar, keyCode);
    }
}
