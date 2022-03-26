package com.paragon.client.systems.ui.panel.impl.setting;

import com.paragon.Paragon;
import com.paragon.api.event.client.SettingUpdateEvent;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.client.systems.ui.panel.impl.module.ModuleButton;
import com.paragon.client.systems.module.impl.client.Colours;
import com.paragon.client.systems.module.settings.impl.BooleanSetting;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class BooleanComponent extends SettingComponent {

    public BooleanComponent(ModuleButton moduleButton, BooleanSetting setting, float offset, float height) {
        super(moduleButton, setting, offset, height);
    }

    @Override
    public void renderSetting(int mouseX, int mouseY) {
        // Background
        RenderUtil.drawRect(getModuleButton().getPanel().getX(), getModuleButton().getOffset() + getOffset(), getModuleButton().getPanel().getWidth(), getHeight(), isMouseOver(mouseX, mouseY) ? new Color(23, 23, 23).brighter().getRGB() : new Color(23, 23, 23).getRGB());

        // Render setting name
        GL11.glPushMatrix();
        GL11.glScalef(0.7f, 0.7f, 0.7f);
        float scaleFactor = 1 / 0.7f;
        renderText(getSetting().getName(), (getModuleButton().getPanel().getX() + 5) * scaleFactor, (getModuleButton().getOffset() + getOffset() + 4) * scaleFactor, ((BooleanSetting) getSetting()).isEnabled() ? Colours.mainColour.getColour().getRGB() : -1);
        GL11.glPopMatrix();

        super.renderSetting(mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            if (isMouseOver(mouseX, mouseY)) {
                // Toggle setting
                ((BooleanSetting) getSetting()).setEnabled(!((BooleanSetting) getSetting()).isEnabled());

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
}
