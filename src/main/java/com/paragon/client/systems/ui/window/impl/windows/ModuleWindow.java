package com.paragon.client.systems.ui.window.impl.windows;

import com.paragon.api.util.render.GuiUtil;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.impl.client.ClickGUI;
import com.paragon.client.systems.module.impl.client.Colours;
import com.paragon.client.systems.ui.window.impl.Window;
import com.paragon.client.systems.ui.window.impl.windows.components.CategoryComponent;
import com.paragon.client.systems.ui.window.impl.windows.components.ModuleComponent;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;

public class ModuleWindow extends Window {

    private final List<CategoryComponent> categoryComponents = new ArrayList<>();
    private Category selected;
    private CategoryComponent selectedComponent;
    private boolean dragging;

    public ModuleWindow() {
        super("Paragon", 100, 100, 400, 300);

        float x = getX() + 2;
        for (Category category : Category.values()) {
            categoryComponents.add(new CategoryComponent(this, category, x, getY() + 20, getMCFontRenderer().getStringWidth(category.getName()) + 8, 15));
            x += getMCFontRenderer().getStringWidth(category.getName()) + 10;
        }

        selected = Category.COMBAT;
    }

    @Override
    public void drawWindow(int mouseX, int mouseY) {
        if (dragging) {
            setX(mouseX - getLastX());
            setY(mouseY - getLastY());
        }

        RenderUtil.drawRect(getX(), getY(), getWidth(), getHeight(), 0x90000000);
        RenderUtil.drawBorder(getX(), getY(), getWidth(), getHeight(), 1, Colours.mainColour.getValue().getRGB());
        RenderUtil.drawRect(getX(), getY() + 17, getWidth(), 1, Colours.mainColour.getValue().getRGB());

        renderCenteredString(getTitle(), getX() + getWidth() / 2f, getY() + 8.5f, -1, true);

        float x = getX() + 2;
        for (CategoryComponent categoryComponent : categoryComponents) {
            categoryComponent.setX(x);
            categoryComponent.setY(getY() + 20);

            categoryComponent.drawComponent(mouseX, mouseY);

            x += getMCFontRenderer().getStringWidth(categoryComponent.getCategory().getName()) + 10;

            if (isSelected(categoryComponent.getCategory())) {
                selectedComponent = categoryComponent;
            }
        }

        if (selectedComponent != null) {
            RenderUtil.startGlScissor(getX(), getY() + 39, getWidth(), getHeight() - 40);

            ModuleComponent firstComponent = selectedComponent.getModuleComponents().get(0);
            ModuleComponent lastComponent = selectedComponent.getModuleComponents().get(selectedComponent.getModuleComponents().size() - 1);

            int mouse = Mouse.getDWheel();

            if (mouse > 0) {
                for (ModuleComponent moduleComponent : selectedComponent.getModuleComponents()) {
                    if (firstComponent.getY() + 1 > getY() + 40 && ClickGUI.scrollClamp.getValue()) {
                        continue;
                    }

                    moduleComponent.setY(moduleComponent.getY() + 15);
                }
            } else if (mouse < 0) {
                for (ModuleComponent moduleComponent : selectedComponent.getModuleComponents()) {
                    if (lastComponent.getY() < getY() + getHeight() - lastComponent.getTotalHeight() && ClickGUI.scrollClamp.getValue()) {
                        continue;
                    }

                    moduleComponent.setY(moduleComponent.getY() - 15);
                }
            }

            if (firstComponent.getY() > getY() + 40) {
                for (ModuleComponent moduleComponent : selectedComponent.getModuleComponents()) {
                    moduleComponent.setY(moduleComponent.getY() - 1);
                }
            }

            if (lastComponent.getY() < getY() + getHeight() - (lastComponent.getTotalHeight() + 3) && !firstComponent.isWithinWindowBounds(getY() + 40, getY() + getHeight())) {
                for (ModuleComponent moduleComponent : selectedComponent.getModuleComponents()) {
                    moduleComponent.setY(moduleComponent.getY() + 1);
                }
            }

            float y = firstComponent.getY();
            for (ModuleComponent moduleComponent : selectedComponent.getModuleComponents()) {
                moduleComponent.setX(getX() + 2);
                moduleComponent.setY(y);

                moduleComponent.drawComponent(mouseX, mouseY);

                y += moduleComponent.getTotalHeight() + 3;
            }

            RenderUtil.endGlScissor();
        }

        RenderUtil.drawRect(getX(), getY() + 37, getWidth(), 1, Colours.mainColour.getValue().getRGB());
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && isMouseOverHeader(mouseX, mouseY)) {
            setLastX(mouseX - getX());
            setLastY(mouseY - getY());

            dragging = true;
        }

        for (CategoryComponent categoryComponent : categoryComponents) {
            categoryComponent.mouseClicked(mouseX, mouseY, mouseButton);

            if (isSelected(categoryComponent.getCategory())) {
                for (ModuleComponent moduleComponent : categoryComponent.getModuleComponents()) {
                    moduleComponent.mouseClicked(mouseX, mouseY, mouseButton);
                }
            }
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        dragging = false;

        if (selectedComponent != null) {
            for (ModuleComponent moduleComponent : selectedComponent.getModuleComponents()) {
                moduleComponent.mouseReleased(mouseX, mouseY, mouseButton);
            }
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (selectedComponent != null) {
            for (ModuleComponent moduleComponent : selectedComponent.getModuleComponents()) {
                moduleComponent.keyTyped(typedChar, keyCode);
            }
        }
    }

    public boolean isSelected(Category category) {
        return category.equals(selected);
    }

    public void setSelected(Category category) {
        selected = category;
    }

    public boolean isMouseOverHeader(int mouseX, int mouseY) {
        return GuiUtil.mouseOver(getX(), getY(), getX() + getWidth(), getY() + 17, mouseX, mouseY);
    }

}
