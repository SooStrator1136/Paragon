package com.paragon.client.systems.ui.panel.impl.setting;

import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.string.EnumFormatter;
import com.paragon.client.systems.ui.panel.impl.module.ModuleButton;
import com.paragon.client.systems.module.settings.impl.ModeSetting;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import java.awt.*;

@SuppressWarnings("unchecked")
public class ModeComponent extends SettingComponent {

    public ModeComponent(ModuleButton moduleButton, ModeSetting<Enum<?>> setting, float offset, float height) {
        super(moduleButton, setting, offset, height);
    }

    @Override
    public void renderSetting(int mouseX, int mouseY) {
        RenderUtil.drawRect(getModuleButton().getPanel().getX(), getModuleButton().getOffset() + getOffset(), getModuleButton().getPanel().getWidth(), getHeight(), isMouseOver(mouseX, mouseY) ? new Color(23, 23, 23).brighter().getRGB() : new Color(23, 23, 23).getRGB());

        String mode = EnumFormatter.getFormattedText(((ModeSetting<Enum<?>>) getSetting()).getCurrentMode());
        GL11.glPushMatrix();
        GL11.glScalef(0.7f, 0.7f, 0.7f);
        float scaleFactor = 1 / 0.7f;
        renderText(getSetting().getName() + formatCode(TextFormatting.GRAY) + " " + mode, (getModuleButton().getPanel().getX() + 4) * scaleFactor, (getModuleButton().getOffset() + getOffset() + 4f) * scaleFactor, -1);
        GL11.glPopMatrix();

        super.renderSetting(mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            if (isMouseOver(mouseX, mouseY)) {
                // Cycle mode
                ((ModeSetting<?>) getSetting()).cycleMode();
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
