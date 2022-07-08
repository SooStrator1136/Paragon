package com.paragon.client.systems.ui.panel.panel;

import com.paragon.Paragon;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.render.TextRenderer;
import com.paragon.api.util.string.StringUtil;
import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.impl.client.ClickGUI;
import com.paragon.client.systems.module.impl.client.ClientFont;
import com.paragon.client.systems.module.impl.client.Colours;
import com.paragon.client.systems.ui.animation.Animation;
import com.paragon.client.systems.ui.panel.Click;
import com.paragon.client.systems.ui.panel.element.Element;
import com.paragon.client.systems.ui.panel.element.module.ModuleElement;
import com.paragon.client.systems.ui.panel.element.setting.*;
import com.paragon.client.systems.ui.window.impl.windows.components.ModuleComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.Sys;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL11.*;

public class CategoryPanel extends Panel implements TextRenderer {

    private final Category category;

    private List<Element> elements = new ArrayList<>();

    private final float barHeight;

    private float leftHue;

    private final Animation animation = new Animation(() -> ClickGUI.animationSpeed.getValue(), true, () -> ClickGUI.easing.getValue());
    private boolean open = true;

    private float scrollFactor = 0;
    private float moduleHeight = 0;

    private float hover;

    private boolean dragging;

    public CategoryPanel(Category category, float x, float y, float width, float height, float barHeight) {
        super(x, y, width, height);

        this.category = category;
        this.barHeight = barHeight;

        float offset = getY() + barHeight;
        for (Module module : Paragon.INSTANCE.getModuleManager().getModulesInCategory(category)) {
            elements.add(new ModuleElement(this, module, getX(), offset, getWidth(), 16));
            offset += 16;
        }
    }

    public void render(int mouseX, int mouseY, int dWheel) {
        hover = MathHelper.clamp(hover + (isHoveredOverBar(mouseX, mouseY) ? 0.02f : -0.02f), 0, 1);

        if (dragging) {
            this.setX(mouseX - getLastX());
            this.setY(mouseY - getLastY());

            scrollFactor = getY() < getLastY() ? 0 : 1;
        }

        leftHue = (getX() / RenderUtil.getScreenWidth()) * 360;
        float rightHue = (getX() + getWidth()) / RenderUtil.getScreenWidth() * 360;

        leftHue /= 5;
        leftHue += 270;

        rightHue /= 5;
        rightHue += 270;

        leftHue = MathHelper.clamp(leftHue, 0, 360);
        rightHue = MathHelper.clamp(rightHue, 0, 360);

        RenderUtil.drawHorizontalGradientRect(getX(), getY(), getWidth(), barHeight, Color.HSBtoRGB(getLeftHue() / 360, 1, 0.75f + (0.25f * hover)), Color.HSBtoRGB(rightHue / 360, 1, 0.75f + (0.25f * hover)));

        glScalef(1.25f, 1.25f, 1.25f);
        {
            float scaleFactor = 1 / 1.25f;

            renderText(category.getName(), (getX() + 30) * scaleFactor, (getY() + barHeight * scaleFactor / 2 - 2f) * scaleFactor, 0xFFFFFFFF);

            glScalef(scaleFactor, scaleFactor, scaleFactor);
        }

        RenderUtil.drawRect(getX(), getY(), barHeight, barHeight, 0x90000000);

        // Eye of ender is offset weirdly...
        if (category.equals(Category.RENDER)) {
            glTranslated(-0.5, -0.5, 0);
        }

        RenderUtil.renderItemStack(new ItemStack(category.getIndicator()), getX() + 3.5f, getY() + 3.5f, false);

        if (category.equals(Category.RENDER)) {
            glTranslated(0.5, 0.5, 0);
        }

        Element lastModuleElement = elements.get(elements.size() - 1);
        Element lastElement = lastModuleElement.getAnimation().getAnimationFactor() > 0 ? lastModuleElement.getSubElements().get(lastModuleElement.getSubElements().size() - 1) : lastModuleElement;

        float height = 0;

        for (Element element : elements) {
            height += element.getTotalHeight();
        }

        moduleHeight = height;

        if (dWheel != 0 && isHovered(getX(), getY() + barHeight, getWidth(), moduleHeight, mouseX, mouseY)) {
            scrollFactor = dWheel > 0 ? 2 : -2;
        }

        else {
            if (scrollFactor != 0) {
                scrollFactor *= 0.95f;

                if (scrollFactor < 0.1 && scrollFactor > -0.1) {
                    scrollFactor = 0;
                }
            }
        }

        // int scissorHeight = (int) (MathHelper.clamp(moduleHeight, 0, 352) * animation.getAnimationFactor());

        float scissorHeight = (float) MathHelper.clamp(MathHelper.clamp(moduleHeight, 0,(lastElement.getY() + lastElement.getHeight()) - (getY() + barHeight)) * animation.getAnimationFactor(), 0, 352);

        if (scrollFactor != 0) {
            if (lastElement.getY() + lastElement.getTotalHeight() > getY() + barHeight + scissorHeight) {
                elements.forEach(element -> {
                    element.setY(element.getY() + scrollFactor);
                });
            }

            else {
                scrollFactor = 0;
                for (Element element : elements) {
                    element.setY(element.getY() + 1);
                }
            }
        }

        if (lastElement.getY() + lastElement.getTotalHeight() < getY() + barHeight + scissorHeight) {
            for (Element element : elements) {
                element.setY(element.getY() + 1);
            }
        }

        if (isElementVisible(elements.get(0)) && isElementVisible(lastElement) || elements.get(0).getY() > getY() + barHeight) {
            scrollFactor = 0;

            elements.get(0).setY(getY() + barHeight);
        }

        if (isElementVisible(lastElement) && lastElement.getY() < scissorHeight) {
            for (Element element : elements) {
                element.setY(element.getY() + 1);
            }
        }

        if (lastElement.getY() + lastElement.getHeight() < getY() + barHeight + MathHelper.clamp(moduleHeight, 0, 352) && moduleHeight > 352) {
            scrollFactor = 0;
            elements.get(0).setY(elements.get(0).getY() + 1);
        }

        float offset = elements.get(0).getY();
        for (Element element : elements) {
            element.setX(getX());
            element.setY(offset);

            offset += element.getTotalHeight();
        }

        RenderUtil.drawHorizontalGradientRect(getX(), getY() + barHeight + scissorHeight, getWidth(), 2, Color.HSBtoRGB(getLeftHue() / 360, 1, 1), Color.HSBtoRGB(rightHue / 360, 1, 1));

        RenderUtil.startGlScissor(getX(), getY() + barHeight, getWidth(), scissorHeight);

        for (Element element : elements) {
            element.render(mouseX, mouseY, dWheel);
        }

        RenderUtil.endGlScissor();

        if (ClickGUI.tooltips.getValue() && animation.getAnimationFactor() > 0) {
            for (Element element : elements) {
                if (element instanceof ModuleElement) {
                    {
                        if (((ModuleElement) element).getHover() > 0) {
                            float hover = ((ModuleElement) element).getHover();
                            String description = StringUtil.wrap(((ModuleElement) element).getModule().getDescription(), 20) + "\n\nBind: " + ((ModuleElement) element).getModule().getBind().getValue().getButtonName();

                            RenderUtil.startGlScissor(element.getX() + 2 + getWidth(), element.getY(), (getStringWidth(description) + 10) * hover, 100);

                            RenderUtil.drawRect(element.getX() + 2 + getWidth(), element.getY(), getStringWidth(description) + 10, (description.split("\n").length * getFontHeight()) + 2 + (ClientFont.INSTANCE.isEnabled() ? 0 : 3), Colours.mainColour.getValue().getRGB());

                            renderText(description, element.getX() + getWidth() + 5, element.getY() + 3, 0xFFFFFFFF);

                            RenderUtil.endGlScissor();
                        }
                    }

                    if (element.getAnimation().getAnimationFactor() > 0) {
                        element.getSubElements().forEach(subElement -> {
                            float hover = 0;
                            String description = "";

                            if (subElement instanceof BooleanElement) {
                                description = StringUtil.wrap(((BooleanElement) subElement).getSetting().getDescription(), 20);
                                hover = ((BooleanElement) subElement).getHover();
                            }

                            else if (subElement instanceof SliderElement) {
                                description = StringUtil.wrap(((SliderElement) subElement).getSetting().getDescription(), 20);
                                hover = ((SliderElement) subElement).getHover();
                            }

                            else if (subElement instanceof EnumElement) {
                                description = StringUtil.wrap(((EnumElement) subElement).getSetting().getDescription(), 20);
                                hover = ((EnumElement) subElement).getHover();
                            }

                            else if (subElement instanceof ColourElement) {
                                description = StringUtil.wrap(((ColourElement) subElement).getSetting().getDescription(), 20);
                                hover = ((ColourElement) subElement).getHover();
                            }

                            else if (subElement instanceof BindElement) {
                                description = StringUtil.wrap(((BindElement) subElement).getSetting().getDescription(), 20);
                                hover = ((BindElement) subElement).getHover();
                            }

                            if (hover == 0) {
                                return;
                            }

                            RenderUtil.startGlScissor(subElement.getX() + 2 + getWidth(), subElement.getY(), (getStringWidth(description) + 10) * hover, 100);

                            RenderUtil.drawRect(subElement.getX() + 2 + getWidth(), subElement.getY(), getStringWidth(description) + 10, (description.split("\n").length * getFontHeight()) + 2 + (ClientFont.INSTANCE.isEnabled() ? 0 : 3), Colours.mainColour.getValue().getRGB());

                            renderText(description, subElement.getX() + getWidth() + 5, subElement.getY() + 3, 0xFFFFFFFF);

                            RenderUtil.endGlScissor();

                            subElement.getSubElements().forEach(subSubElement -> {
                                float subHover = 0;
                                String subDesc = "";

                                if (subSubElement instanceof BooleanElement) {
                                    subDesc = StringUtil.wrap(((BooleanElement) subSubElement).getSetting().getDescription(), 20);
                                    subHover = ((BooleanElement) subSubElement).getHover();
                                }

                                else if (subSubElement instanceof SliderElement) {
                                    subDesc = StringUtil.wrap(((SliderElement) subSubElement).getSetting().getDescription(), 20);
                                    subHover = ((SliderElement) subSubElement).getHover();
                                }

                                else if (subSubElement instanceof EnumElement) {
                                    subDesc = StringUtil.wrap(((EnumElement) subSubElement).getSetting().getDescription(), 20);
                                    subHover = ((EnumElement) subSubElement).getHover();
                                }

                                else if (subSubElement instanceof ColourElement) {
                                    subDesc = StringUtil.wrap(((ColourElement) subSubElement).getSetting().getDescription(), 20);
                                    subHover = ((ColourElement) subSubElement).getHover();
                                }

                                else if (subSubElement instanceof BindElement) {
                                    subDesc = StringUtil.wrap(((BindElement) subSubElement).getSetting().getDescription(), 20);
                                    subHover = ((BindElement) subSubElement).getHover();
                                }

                                if (subHover == 0) {
                                    return;
                                }

                                RenderUtil.startGlScissor(subElement.getX() + 2 + getWidth(), subElement.getY(), (getStringWidth(subDesc) + 10) * subHover, 100);

                                RenderUtil.drawRect(subElement.getX() + 2 + getWidth(), subElement.getY(), getStringWidth(subDesc) + 10, (subDesc.split("\n").length * getFontHeight()) + 2 + (ClientFont.INSTANCE.isEnabled() ? 0 : 3), Colours.mainColour.getValue().getRGB());

                                renderText(subDesc, subElement.getX() + getWidth() + 5, subElement.getY() + 3, 0xFFFFFFFF);

                                RenderUtil.endGlScissor();
                            });
                        });
                    }
                }
            }
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, Click click) {
        if (isHoveredOverBar(mouseX, mouseY)) {
            if (click.equals(Click.LEFT)) {
                setLastX(mouseX - getX());
                setLastY(mouseY - getY());

                this.dragging = true;
            }

            else if (click.equals(Click.RIGHT)) {
                open = !open;
                animation.setState(open);
            }
        }

        if (open) {
            elements.forEach(element -> {
                element.mouseClicked(mouseX, mouseY, click);
            });
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, Click click) {
        dragging = false;

        if (open) {
            elements.forEach(element -> {
                element.mouseReleased(mouseX, mouseY, click);
            });
        }
    }

    @Override
    public void keyTyped(int keyCode, char keyChar) {
        if (open) {
            elements.forEach(element -> {
                element.keyTyped(keyCode, keyChar);
            });
        }
    }

    public boolean isElementVisible(Element element) {
        return element.getY() >= getY() + barHeight - 1 && element.getY() + element.getHeight() < getY() + 16 + barHeight + MathHelper.clamp(moduleHeight, 0, 352);
    }

    public boolean isHoveredOverBar(int mouseX, int mouseY) {
        return mouseX >= getX() && mouseX <= getX() + getWidth() && mouseY >= getY() && mouseY <= getY() + barHeight;
    }

    public float getLeftHue() {
        return leftHue;
    }
}
