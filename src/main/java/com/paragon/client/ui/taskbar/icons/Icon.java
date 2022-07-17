package com.paragon.client.ui.taskbar.icons;

import com.paragon.api.util.Wrapper;
import com.paragon.api.util.render.ColourUtil;
import com.paragon.api.util.render.ITextRenderer;
import com.paragon.client.systems.module.impl.client.ClientFont;
import com.paragon.client.systems.module.impl.client.Colours;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Supplier;

@SideOnly(Side.CLIENT)
public final class Icon implements Wrapper, ITextRenderer {

    private final String name;
    private final int x;
    private int y;
    private final Supplier<GuiScreen> guiScreenSupplier;

    public Icon(String name, final int x, Supplier<GuiScreen> whenClicked) {
        this.name = name;
        this.x = x;
        this.guiScreenSupplier = whenClicked;
    }

    public void draw(int mouseX, int mouseY) {
        ScaledResolution scaledResolution = new ScaledResolution(mc);

        float y = scaledResolution.getScaledHeight() - 16.5f;

        ColourUtil.setColour(-1);

        renderCenteredString(name, x + ((getStringWidth(name) + 6) / 2), y + (ClientFont.INSTANCE.isEnabled() ? 2 : 4), isHovered(x, y, getStringWidth(name) + 6, 18, mouseX, mouseY) ? Colours.mainColour.getValue().getRGB() : -1, false);
    }

    public void whenClicked(int mouseX, int mouseY) {
        if (isHovered(x, y, getStringWidth(name) + 6, 16, mouseX, mouseY)) {
            mc.displayGuiScreen(guiScreenSupplier.get());
        }
    }

}
