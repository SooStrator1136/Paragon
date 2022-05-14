package com.paragon.client.systems.ui.window.impl.windows.components;

import com.paragon.Paragon;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.impl.client.Colours;
import com.paragon.client.systems.ui.window.impl.windows.Component;
import com.paragon.client.systems.ui.window.impl.windows.ModuleWindow;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryComponent extends Component {

    private final Category category;
    private final List<ModuleComponent> moduleComponents = new ArrayList<>();

    public CategoryComponent(ModuleWindow window, Category category, float x, float y, float width, float height) {
        super(window, x, y, width, height);

        this.category = category;

        float offset = y + height + 8;
        for (Module module : Paragon.INSTANCE.getModuleManager().getModulesInCategory(category)) {
            moduleComponents.add(new ModuleComponent(window, module, window.getX() + 2, offset, getWindow().getWidth() - 4, 15));
            offset += 19;
        }
    }

    @Override
    public void drawComponent(int mouseX, int mouseY) {
        RenderUtil.drawRect(getX(), getY(), getWidth(), getHeight(), isHovered(mouseX, mouseY) ? new Color(25, 25, 28, 150).getRGB() : new Color(23, 23, 25, 180).getRGB());

        if (((ModuleWindow) getWindow()).isSelected(category)) {
            RenderUtil.drawRect(getX(), getY() + getHeight() - 1, getWidth(), 1, Colours.mainColour.getValue().getRGB());
        }

        renderCenteredString(category.getName(), getX() + getWidth() / 2, getY() + getHeight() / 2, 0xFFFFFF, true);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (isHovered(mouseX, mouseY)) {
            ((ModuleWindow) getWindow()).setSelected(category);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {

    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {

    }

    public Category getCategory() {
        return category;
    }

    public List<ModuleComponent> getModuleComponents() {
        return moduleComponents;
    }
}
