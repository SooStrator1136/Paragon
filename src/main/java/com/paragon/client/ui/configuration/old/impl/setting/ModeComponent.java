package com.paragon.client.ui.configuration.old.impl.setting;

import com.paragon.Paragon;
import com.paragon.api.event.client.SettingUpdateEvent;
import com.paragon.api.setting.Setting;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.string.StringUtil;
import com.paragon.client.ui.configuration.old.impl.module.ModuleButton;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class ModeComponent extends SettingComponent<Enum<?>> {

    public ModeComponent(ModuleButton moduleButton, Setting<Enum<?>> setting, float offset, float height) {
        super(moduleButton, setting, offset, height);
    }

    @Override
    public void renderSetting(int mouseX, int mouseY) {
        RenderUtil.drawRect(getModuleButton().getPanel().getX(), getModuleButton().getOffset() + getOffset(), getModuleButton().getPanel().getWidth(), getHeight(), isMouseOver(mouseX, mouseY) ? new Color(23, 23, 23).brighter().getRGB() : new Color(23, 23, 23).getRGB());

        String mode = StringUtil.getFormattedText(getSetting().getValue());
        GL11.glPushMatrix();
        GL11.glScalef(0.65f, 0.65f, 0.65f);

        {
            float scaleFactor = 1 / 0.65f;
            renderText(getSetting().getName(), (getModuleButton().getPanel().getX() + 5) * scaleFactor, (getModuleButton().getOffset() + getOffset() + 4.5f) * scaleFactor, -1);

            float side = (getModuleButton().getPanel().getX() + getModuleButton().getPanel().getWidth() - (getStringWidth(mode) * 0.65f) - 5) * scaleFactor;
            renderText(formatCode(TextFormatting.GRAY) + " " + mode, side, (getModuleButton().getOffset() + getOffset() + 4.5f) * scaleFactor, -1);
        }

        GL11.glPopMatrix();

        super.renderSetting(mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            if (isMouseOver(mouseX, mouseY)) {
                // Cycle mode
                getSetting().setValue(getSetting().getNextMode());

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
    public float getAbsoluteHeight() {
        float subsettingHeight = 0;

        for (SettingComponent settingComponent : getSettingComponents()) {
            subsettingHeight += settingComponent.getHeight();
        }

        return isExpanded() ? getHeight() + subsettingHeight : getHeight();
    }
}
