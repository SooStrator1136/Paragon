package com.paragon.client.systems.ui.window.impl.windows.components.settings;

import com.paragon.client.systems.module.setting.Bind;
import com.paragon.client.systems.module.setting.Setting;
import com.paragon.client.systems.ui.window.impl.Window;
import com.paragon.client.systems.ui.window.impl.windows.Component;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SettingComponent<T> extends Component {

    private final List<SettingComponent<?>> subsettings = new ArrayList<>();
    private final Setting<T> setting;
    private boolean open = false;

    public SettingComponent(Window window, Setting<T> setting, float x, float y, float width, float height) {
        super(window, x, y, width, height);

        this.setting = setting;

        float offset = 0;
        for (Setting<?> subsetting : setting.getSubsettings()) {
            if (subsetting.getValue() instanceof Boolean) {
                subsettings.add(new BooleanComponent(window, (Setting<Boolean>) subsetting, x + 2, y + offset, width - 4, 15));
                offset += 15;
            }

            else if (subsetting.getValue() instanceof Bind) {
                subsettings.add(new KeybindComponent(window, (Setting<Bind>) subsetting, x + 2, y + offset, width - 4, 15));
                offset += 15;
            }

            else if (subsetting.getValue() instanceof Number) {
                subsettings.add(new SliderComponent(window, (Setting<Number>) subsetting, x + 2, y + offset, width - 4, 20));
                offset += 20;
            }

            else if (subsetting.getValue() instanceof Enum<?>) {
                subsettings.add(new ModeComponent(window, (Setting<Enum<?>>) subsetting, x + 2, y + offset, width - 4, 15));
                offset += 15;
            }

            else if (setting.getValue() instanceof Color) {
                subsettings.add(new ColourComponent(window, (Setting<Color>) subsetting, x + 2, y + offset, width - 4, 15));
                offset += 15;
            }
        }
    }

    @Override
    public void drawComponent(int mouseX, int mouseY) {
        if (open) {
            float offset = getY() + getHeight() + 3;

            for (SettingComponent<?> settingComponent : subsettings) {
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
            for (SettingComponent<?> settingComponent : subsettings) {
                settingComponent.mouseClicked(mouseX, mouseY, button);
            }
        }

        if (button == 1 && isHovered(mouseX, mouseY) && isWithinWindowBounds(getWindow().getY() + 40, getWindow().getY() + getWindow().getHeight())) {
            open = !open;
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
        if (open) {
            for (SettingComponent<?> settingComponent : subsettings) {
                settingComponent.mouseReleased(mouseX, mouseY, button);
            }
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (open) {
            for (SettingComponent<?> settingComponent : subsettings) {
                settingComponent.keyTyped(typedChar, keyCode);
            }
        }
    }

    public float getTotalHeight() {
        float height = 0;
        for (SettingComponent<?> setting : subsettings) {
            if (setting.getSetting().isVisible()) {
                height += setting.getTotalHeight() + 3;
            }
        }

        return getHeight() + (open ? height : 0);
    }

    public Setting<T> getSetting() {
        return setting;
    }
}
