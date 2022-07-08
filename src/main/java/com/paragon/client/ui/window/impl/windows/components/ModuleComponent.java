package com.paragon.client.ui.window.impl.windows.components;

import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.module.Module;
import com.paragon.client.systems.module.impl.client.Colours;
import com.paragon.api.setting.Bind;
import com.paragon.api.setting.Setting;
import com.paragon.client.ui.window.impl.Window;
import com.paragon.client.ui.window.impl.windows.Component;
import com.paragon.client.ui.window.impl.windows.components.settings.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ModuleComponent extends Component {

    private final Module module;

    private final List<SettingComponent<?>> settingComponents = new ArrayList<>();

    private boolean open;

    public ModuleComponent(Window window, Module module, float x, float y, float width, float height) {
        super(window, x, y, width, height);

        this.module = module;

        float offset = 0;
        for (Setting<?> setting : module.getSettings()) {
            if (setting.getValue() instanceof Boolean) {
                settingComponents.add(new BooleanComponent(window, (Setting<Boolean>) setting, x + 2, y + offset, width - 4, 15));
                offset += 15;
            }

            else if (setting.getValue() instanceof Bind) {
                settingComponents.add(new KeybindComponent(window, (Setting<Bind>) setting, x + 2, y + offset, width - 4, 15));
                offset += 15;
            }

            else if (setting.getValue() instanceof Number) {
                settingComponents.add(new SliderComponent(window, (Setting<Number>) setting, x + 2, y + offset, width - 4, 20));
                offset += 20;
            }

            else if (setting.getValue() instanceof Enum<?>) {
                settingComponents.add(new ModeComponent(window, (Setting<Enum<?>>) setting, x + 2, y + offset, width - 4, 15));
                offset += 15;
            }

            else if (setting.getValue() instanceof Color) {
                settingComponents.add(new ColourComponent(window, (Setting<Color>) setting, x + 2, y + offset, width - 4, 15));
                offset += 15;
            }

            else if (setting.getValue() instanceof String) {
                settingComponents.add(new StringComponent(window, (Setting<String>) setting, x + 2, y + offset, width - 4, 15));
                offset += 15;
            }
        }
    }

    @Override
    public void drawComponent(int mouseX, int mouseY) {
        RenderUtil.drawRect(getX(), getY(), getWidth(), getTotalHeight(), 0x90000000);

        renderText(getModule().getName(), getX() + 4, getY() + 4, getModule().isEnabled() ? Colours.mainColour.getValue().getRGB() : -1);

        if (open) {
            float offset = getY() + getHeight() + 3;

            for (SettingComponent<?> settingComponent : settingComponents) {
                if (settingComponent.getSetting().isVisible()) {
                    settingComponent.setX(getX() + 2);
                    settingComponent.setY(offset);

                    settingComponent.drawComponent(mouseX, mouseY);

                    offset += settingComponent.getTotalHeight() + 3;
                }
            }
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (open) {
            for (SettingComponent<?> settingComponent : settingComponents) {
                if (settingComponent.getSetting().isVisible()) {
                    settingComponent.mouseClicked(mouseX, mouseY, button);
                }
            }
        }

        if (isHovered(mouseX, mouseY) && isWithinWindowBounds(getWindow().getY() + 40, getWindow().getY() + getWindow().getHeight())) {
            if (button == 0) {
                getModule().toggle();
            } else if (button == 1) {
                open = !open;
            }
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
        if (open) {
            for (SettingComponent<?> settingComponent : settingComponents) {
                if (settingComponent.getSetting().isVisible()) {
                    settingComponent.mouseReleased(mouseX, mouseY, button);
                }
            }
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (open) {
            for (SettingComponent<?> settingComponent : settingComponents) {
                if (settingComponent.getSetting().isVisible()) {
                    settingComponent.keyTyped(typedChar, keyCode);
                }
            }
        }
    }

    public float getTotalHeight() {
        float height = 2;
        for (SettingComponent<?> settingComponent : settingComponents) {
            if (settingComponent.getSetting().isVisible()) {
                height += settingComponent.getTotalHeight() + 3;
            }
        }

        return open ? getHeight() + height : getHeight();
    }

    public Module getModule() {
        return module;
    }

    public Component getLastComponent() {
        if (!open) {
            return this;
        }

        return this.settingComponents.get(this.settingComponents.size() - 1);
    }

}
