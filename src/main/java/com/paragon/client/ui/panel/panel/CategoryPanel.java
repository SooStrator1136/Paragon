package com.paragon.client.ui.panel.panel;

import com.paragon.Paragon;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.render.TextRenderer;
import com.paragon.api.util.string.StringUtil;
import com.paragon.api.module.Category;
import com.paragon.api.module.Module;
import com.paragon.client.systems.module.impl.client.ClickGUI;
import com.paragon.client.systems.module.impl.client.ClientFont;
import com.paragon.client.systems.module.impl.client.Colours;
import com.paragon.client.ui.animation.Animation;
import com.paragon.client.ui.panel.Click;
import com.paragon.client.ui.panel.element.Element;
import com.paragon.client.ui.panel.element.module.ModuleElement;
import com.paragon.client.ui.panel.element.setting.*;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

// fuck warnings :)
@SuppressWarnings("all")
public class CategoryPanel extends Panel implements TextRenderer {

    private final Category category;

    private final List<Element> elements = new ArrayList<>();

    private final float barHeight;

    private float leftHue;

    private final Animation animation = new Animation(() -> ClickGUI.getAnimationSpeed().getValue(), true, () -> ClickGUI.getEasing().getValue());
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
        for (Module module : Paragon.INSTANCE.getModuleManager().getModulesThroughPredicate(module -> module.getCategory().equals(category))) {
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

        RenderUtil.drawHorizontalRoundedRect(getX(), getY(), getWidth(), barHeight, ClickGUI.getRadius().getValue(), ClickGUI.getRadius().getValue(), 1, 1, Color.HSBtoRGB(getLeftHue() / 360, 1, 0.75f + (0.25f * hover)), Color.HSBtoRGB(rightHue / 360, 1, 0.75f + (0.25f * hover)));

        float titleOffset = 5;

        if (!ClickGUI.getIcon().getValue().equals(ClickGUI.Icon.NONE)) {
            if (ClickGUI.getIcon().getValue().equals(ClickGUI.Icon.BACKGROUND)) {
                RenderUtil.drawRoundedRect(getX(), getY(), barHeight, barHeight, ClickGUI.getRadius().getValue(), 1, 1, 1, 0x90000000);
            }

            // Eye of ender is offset weirdly...
            if (category.equals(Category.RENDER)) {
                glTranslated(-0.5, -0.5, 0);
            }

            RenderUtil.renderItemStack(new ItemStack(category.getIndicator()), getX() + 3.5f, getY() + 3.5f, false);

            if (category.equals(Category.RENDER)) {
                glTranslated(0.5, 0.5, 0);
            }

            titleOffset = 30;
        }

        glScalef(1.25f, 1.25f, 1.25f);
        {
            float scaleFactor = 1 / 1.25f;

            renderText(category.getName(), (getX() + titleOffset) * scaleFactor, (getY() + barHeight * scaleFactor / 2 - 2f) * scaleFactor, 0xFFFFFFFF);

            glScalef(scaleFactor, scaleFactor, scaleFactor);
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

        float scissorHeight = (float) MathHelper.clamp(MathHelper.clamp(moduleHeight, 0,(lastElement.getY() + lastElement.getHeight()) - (getY() + barHeight)) * animation.getAnimationFactor(), 0, 352);

        if (scrollFactor != 0) {
            if (lastElement.getY() + lastElement.getTotalHeight() > getY() + barHeight + scissorHeight) {
                elements.forEach(element -> element.setY(element.getY() + scrollFactor));
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

        if (lastElement.getY() + lastElement.getHeight() < getY() + barHeight + moduleHeight && moduleHeight <= 352 && !isElementVisible(elements.get(0))) {
            elements.get(0).setY(elements.get(0).getY() + 1);
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

        if (isElementVisible(elements.get(0)) && isElementVisible(lastElement) || elements.get(0).getY() > getY() + barHeight) {
            scrollFactor = 0;

            elements.get(0).setY(getY() + barHeight);
        }

        float offset = elements.get(0).getY();
        for (Element element : elements) {
            element.setX(getX());
            element.setY(offset);

            offset += element.getTotalHeight();
        }

        RenderUtil.drawHorizontalRoundedRect(getX(), getY() + barHeight + scissorHeight, getWidth(), 2, 1, 1, MathHelper.clamp(ClickGUI.getRadius().getValue(), 1, 2), MathHelper.clamp(ClickGUI.getRadius().getValue(), 1, 2), Color.HSBtoRGB(getLeftHue() / 360, 1, 0.75f + (0.25f * hover)), Color.HSBtoRGB(rightHue / 360, 1, 0.75f + (0.25f * hover)));

        RenderUtil.startGlScissor(getX(), getY() + barHeight, getWidth(), scissorHeight);

        for (Element element : elements) {
            element.render(mouseX, mouseY, dWheel);
        }

        RenderUtil.endGlScissor();

        if (ClickGUI.getTooltips().getValue() && animation.getAnimationFactor() > 0) {
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
                            boolean visible = true;

                            if (subElement instanceof BooleanElement) {
                                BooleanElement sElement = ((BooleanElement) subElement);
                                description = StringUtil.wrap(sElement.getSetting().getDescription(), 20);
                                hover = sElement.getHover();
                                visible = sElement.getSetting().isVisible();
                            }

                            else if (subElement instanceof SliderElement) {
                                SliderElement sElement = ((SliderElement) subElement);
                                description = StringUtil.wrap(sElement.getSetting().getDescription(), 20);
                                hover = sElement.getHover();
                                visible = sElement.getSetting().isVisible();
                            }

                            else if (subElement instanceof EnumElement) {
                                EnumElement sElement = ((EnumElement) subElement);
                                description = StringUtil.wrap(sElement.getSetting().getDescription(), 20);
                                hover = sElement.getHover();
                                visible = sElement.getSetting().isVisible();
                            }

                            else if (subElement instanceof ColourElement) {
                                ColourElement sElement = ((ColourElement) subElement);
                                description = StringUtil.wrap(sElement.getSetting().getDescription(), 20);
                                hover = sElement.getHover();
                                visible = sElement.getSetting().isVisible();
                            }

                            else if (subElement instanceof BindElement) {
                                BindElement sElement = ((BindElement) subElement);
                                description = StringUtil.wrap(sElement.getSetting().getDescription(), 20);
                                hover = sElement.getHover();
                                visible = sElement.getSetting().isVisible();
                            }

                            if (hover == 0 || !visible) {
                                return;
                            }

                            RenderUtil.startGlScissor(subElement.getX() + 2 + getWidth(), subElement.getY(), (getStringWidth(description) + 10) * hover, 100);

                            RenderUtil.drawRect(subElement.getX() + 2 + getWidth(), subElement.getY(), getStringWidth(description) + 10, (description.split("\n").length * getFontHeight()) + 2 + (ClientFont.INSTANCE.isEnabled() ? 0 : 3), Colours.mainColour.getValue().getRGB());

                            renderText(description, subElement.getX() + getWidth() + 5, subElement.getY() + 3, 0xFFFFFFFF);

                            RenderUtil.endGlScissor();

                            subElement.getSubElements().forEach(subSubElement -> {
                                float subHover = 0;
                                String subDesc = "";

                                boolean subVisible = true;

                                if (subSubElement instanceof BooleanElement) {
                                    BooleanElement ssElement = ((BooleanElement) subSubElement);
                                    subDesc = StringUtil.wrap(ssElement.getSetting().getDescription(), 20);
                                    subHover = ssElement.getHover();
                                    subVisible = ssElement.getSetting().isVisible();
                                }

                                else if (subSubElement instanceof SliderElement) {
                                    SliderElement ssElement = ((SliderElement) subSubElement);
                                    subDesc = StringUtil.wrap(ssElement.getSetting().getDescription(), 20);
                                    subHover = ssElement.getHover();
                                    subVisible = ssElement.getSetting().isVisible();
                                }

                                else if (subSubElement instanceof EnumElement) {
                                    EnumElement ssElement = ((EnumElement) subSubElement);
                                    subDesc = StringUtil.wrap(ssElement.getSetting().getDescription(), 20);
                                    subHover = ssElement.getHover();
                                    subVisible = ssElement.getSetting().isVisible();
                                }

                                else if (subSubElement instanceof ColourElement) {
                                    ColourElement ssElement = ((ColourElement) subSubElement);
                                    subDesc = StringUtil.wrap(ssElement.getSetting().getDescription(), 20);
                                    subHover = ssElement.getHover();
                                    subVisible = ssElement.getSetting().isVisible();
                                }

                                else if (subSubElement instanceof BindElement) {
                                    BindElement ssElement = ((BindElement) subSubElement);
                                    subDesc = StringUtil.wrap(ssElement.getSetting().getDescription(), 20);
                                    subHover = ssElement.getHover();
                                    subVisible = ssElement.getSetting().isVisible();
                                }

                                if (subHover == 0 || !subVisible) {
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
            elements.forEach(element -> element.mouseClicked(mouseX, mouseY, click));
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, Click click) {
        dragging = false;

        if (open) {
            elements.forEach(element -> element.mouseReleased(mouseX, mouseY, click));
        }
    }

    @Override
    public void keyTyped(int keyCode, char keyChar) {
        if (open) {
            elements.forEach(element -> element.keyTyped(keyCode, keyChar));
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

    public float getModuleHeight() {
        return moduleHeight;
    }

}
