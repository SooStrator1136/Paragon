package com.paragon.client.systems.ui.panel.impl.setting;

import com.paragon.Paragon;
import com.paragon.api.event.client.SettingUpdateEvent;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.client.systems.module.setting.Setting;
import com.paragon.client.systems.ui.panel.impl.module.ModuleButton;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

public class KeybindComponent extends SettingComponent<AtomicInteger> {

    private boolean isListening = false;

    public KeybindComponent(ModuleButton moduleButton, Setting<AtomicInteger> setting, float offset, float height) {
        super(moduleButton, setting, offset, height);
    }

    @Override
    public void renderSetting(int mouseX, int mouseY) {
        RenderUtil.drawRect(getModuleButton().getPanel().getX(), getModuleButton().getOffset() + getOffset(), getModuleButton().getPanel().getWidth(), getHeight(), isMouseOver(mouseX, mouseY) ? new Color(23, 23, 23).brighter().getRGB() : new Color(23, 23, 23).getRGB());

        String key = Keyboard.getKeyName(getSetting().getValue().get());
        GL11.glPushMatrix();
        GL11.glScalef(0.65f, 0.65f, 0.65f);
        float scaleFactor = 1 / 0.65f;
        renderText(getSetting().getName() + formatCode(TextFormatting.GRAY) + (isListening ?  " ..." : " " + key), (getModuleButton().getPanel().getX() + 4) * scaleFactor, (getModuleButton().getOffset() + getOffset() + 4) * scaleFactor, -1);
        GL11.glPopMatrix();

        super.renderSetting(mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            if (isMouseOver(mouseX, mouseY)) {
                // Set listening
                isListening = !isListening;
                SettingUpdateEvent settingUpdateEvent = new SettingUpdateEvent(getSetting());
                Paragon.INSTANCE.getEventBus().post(settingUpdateEvent);
            }
        }

        if (isExpanded()) {
            getSettingComponents().forEach(settingComponent -> {
                if (settingComponent.getSetting().isVisible()) {
                    settingComponent.mouseClicked(mouseX, mouseY, mouseButton);
                }
            });
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (isListening) {
            isListening = false;

            if (keyCode == Keyboard.KEY_DELETE || keyCode == Keyboard.KEY_BACK) {
                getSetting().getValue().set(0);
                return;
            }

            getSetting().getValue().set(keyCode);
        }

        if (isExpanded()) {
            getSettingComponents().forEach(settingComponent -> settingComponent.keyTyped(typedChar, keyCode));
        }
    }

    @Override
    public float getAbsoluteHeight() {
        float subsettingHeight = 0;

        for (SettingComponent settingComponent : getSettingComponents()) {
            subsettingHeight += settingComponent.getHeight();
        }

        return isExpanded() ? getHeight() + subsettingHeight : getHeight();
    }
}
