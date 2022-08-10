package com.paragon.client.ui.configuration.zeroday.element;

import com.paragon.api.util.Wrapper;

import com.paragon.client.systems.module.impl.client.ClickGUI;
import com.paragon.client.ui.configuration.zeroday.panel.CategoryPanel;
import me.surge.animation.Animation;
import me.surge.animation.Easing;
import com.paragon.client.ui.util.Click;

import java.util.ArrayList;
import java.util.List;

public abstract class Element implements Wrapper {

    private float x;
    private float y;
    private float width;
    private float height;

    private float lastX;
    private float lastY;

    private final int layer;

    private final Animation hover = new Animation(() -> 200f, false, () -> Easing.LINEAR);

    private List<Element> subelements = new ArrayList<>();

    private CategoryPanel parent;
    private final Animation animation = new Animation(ClickGUI.getAnimationSpeed()::getValue, false, ClickGUI.getEasing()::getValue);
    private boolean open;

    public Element(int layer, float x, float y, float width, float height) {
        this.layer = layer;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.lastX = x;
        this.lastY = y;
    }

    public void render(int mouseX, int mouseY, int dWheel) {
        hover.setState(isHovered(mouseX, mouseY));

        if (animation.getAnimationFactor() > 0) {
            float offset = getY() + getHeight();

            for (Element subElement : getSubElements()) {
                subElement.setX(getX());
                subElement.setY(offset);

                subElement.render(mouseX, mouseY, dWheel);

                offset += subElement.getTotalHeight();
            }
        } else {
            for (Element subElement : getSubElements()) {
                subElement.getHover().setState(false);
            }
        }
    }

    public void mouseClicked(int mouseX, int mouseY, Click click) {
        if (isHovered(mouseX, mouseY) && parent != null && parent.isElementVisible(this)) {
            if (click.equals(Click.RIGHT)) {
                open = !open;
                animation.setState(open);
                return;
            }
        }

        if (open) {
            getSubElements().forEach(subelement -> subelement.mouseClicked(mouseX, mouseY, click));
        }
    }

    public void mouseReleased(int mouseX, int mouseY, Click click) {
        if (open) {
            getSubElements().forEach(subelement -> subelement.mouseReleased(mouseX, mouseY, click));
        }
    }

    public void keyTyped(int keyCode, char keyChar) {
        if (open) {
            getSubElements().forEach(subelement -> subelement.keyTyped(keyCode, keyChar));
        }
    }

    public boolean isHovered(int mouseX, int mouseY) {
        return mouseX >= getX() && mouseX <= getX() + getWidth() && mouseY > getY() && mouseY <= getY() + getHeight();
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public float getSubElementsHeight() {
        float height = 0;

        for (Element element : subelements) {
            height += element.getTotalHeight();
        }

        return height;
    }

    public float getTotalHeight() {
        return (float) (getHeight() + (getSubElementsHeight() * animation.getAnimationFactor()));
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getLastX() {
        return lastX;
    }

    public void setLastX(float lastX) {
        this.lastX = lastX;
    }

    public float getLastY() {
        return lastY;
    }

    public void setLastY(float lastY) {
        this.lastY = lastY;
    }

    public List<Element> getSubElements() {
        return subelements;
    }

    public void addSubElement(Element element) {
        subelements.add(element);
    }

    public CategoryPanel getParent() {
        return parent;
    }

    public void setParent(CategoryPanel parent) {
        this.parent = parent;
    }

    public int getLayer() {
        return layer;
    }

    public Animation getAnimation() {
        return animation;
    }

    public Animation getHover() {
        return hover;
    }

}
