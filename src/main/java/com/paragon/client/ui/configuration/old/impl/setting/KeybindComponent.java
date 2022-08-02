package com.paragon.client.ui.configuration.old.impl.setting;

import com.paragon.Paragon;
import com.paragon.api.event.client.SettingUpdateEvent;
import com.paragon.api.setting.Bind;
import com.paragon.api.setting.Setting;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.render.font.FontUtil;
import com.paragon.client.ui.configuration.old.impl.module.ModuleButton;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class KeybindComponent extends SettingComponent<Bind> {

    private boolean isListening = false;

    public KeybindComponent(ModuleButton moduleButton, Setting<Bind> setting, float offset, float height) {
        super(moduleButton, setting, offset, height);
    }

    @Override
    public void renderSetting(int mouseX, int mouseY) {
        RenderUtil.drawRect(getModuleButton().getPanel().getX(), getModuleButton().getOffset() + getOffset(), getModuleButton().getPanel().getWidth(), getHeight(), isMouseOver(mouseX, mouseY) ? new Color(23, 23, 23).brighter().getRGB() : new Color(23, 23, 23).getRGB());

        GL11.glPushMatrix();
        GL11.glScalef(0.65f, 0.65f, 0.65f);

        {
            float scaleFactor = 1 / 0.65f;
            FontUtil.drawStringWithShadow(getSetting().getName(), (getModuleButton().getPanel().getX() + 5) * scaleFactor, (getModuleButton().getOffset() + getOffset() + 4.5f) * scaleFactor, -1);

            float side = (getModuleButton().getPanel().getX() + getModuleButton().getPanel().getWidth() - (FontUtil.getStringWidth(isListening ? " ..." : " " + getSetting().getValue().getButtonName()) * 0.65f) - 5) * scaleFactor;
            FontUtil.drawStringWithShadow(TextFormatting.GRAY + (isListening ? " ..." : " " + getSetting().getValue().getButtonName()), side, (getModuleButton().getOffset() + getOffset() + 4.5f) * scaleFactor, -1);
        }

        GL11.glPopMatrix();

        super.renderSetting(mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isListening) {
            isListening = false;

            getSetting().getValue().setDevice(Bind.Device.MOUSE);
            getSetting().getValue().setButtonCode(mouseButton);

            return;
        }

        if (isMouseOver(mouseX, mouseY) && mouseButton == 0) {
            // Set listening
            isListening = !isListening;
            SettingUpdateEvent settingUpdateEvent = new SettingUpdateEvent(getSetting());
            Paragon.INSTANCE.getEventBus().post(settingUpdateEvent);
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

            getSetting().getValue().setDevice(Bind.Device.KEYBOARD);

            if (keyCode == Keyboard.KEY_DELETE || keyCode == Keyboard.KEY_BACK) {
                getSetting().getValue().setButtonCode(0);
                return;
            }

            getSetting().getValue().setButtonCode(keyCode);
        }

        if (isExpanded()) {
            getSettingComponents().forEach(settingComponent -> settingComponent.keyTyped(typedChar, keyCode));
        }
    }

    @Override
    public float getAbsoluteHeight() {
        float subsettingHeight = 0;

        for (SettingComponent<?> settingComponent : getSettingComponents()) {
            subsettingHeight += settingComponent.getHeight();
        }

        return isExpanded() ? getHeight() + subsettingHeight : getHeight();
    }
}
