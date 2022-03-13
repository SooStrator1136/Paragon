package com.paragon.client.systems.ui.alt;

import com.paragon.Paragon;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.render.TextRenderer;
import com.paragon.client.managers.alt.Alt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Mouse;
import java.io.IOException;
import java.util.ArrayList;

public class AltManagerGUI extends GuiScreen implements TextRenderer {

    private ArrayList<AltEntry> altEntries = new ArrayList<>();

    @Override
    public void initGui() {
        altEntries.clear();

        float offset = 150;
        for (Alt alt : Paragon.INSTANCE.getAltManager().getAlts()) {
            altEntries.add(new AltEntry(alt, offset));

            offset += 20;
        }

        this.buttonList.add(new GuiButton(0, 5, 5, 75, 20, "Back"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        // refreshOffsets();
        scroll();

        RenderUtil.drawRect(0, 150, width, 200, 0x90000000);
        RenderUtil.startGlScissor(0, 150, width, 200);

        altEntries.forEach(altEntry -> {
            altEntry.drawAlt(mouseX, mouseY, width);
        });

        RenderUtil.endGlScissor();

        renderText("Logged in as " + TextFormatting.GRAY + Minecraft.getMinecraft().session.getUsername(), 5, 30, -1);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public void refreshOffsets() {
        if (altEntries.isEmpty()) {
            return;
        }

        float offset = altEntries.get(0).getOffset();

        for (AltEntry altEntry : altEntries) {
            altEntry.setOffset(offset);
            offset += 20;
        }
    }

    public void scroll() {
        int scroll = Mouse.getDWheel();
        if (scroll > 0) {
            if (altEntries.get(0).getOffset() < 150) {
                for (AltEntry altEntry : altEntries) {
                    altEntry.setOffset(altEntry.getOffset() + 10);
                }
            }
            return;
        }

        if (scroll < 0) {
            if (altEntries.get(altEntries.size() - 1).getOffset() > 340) {
                for (AltEntry altEntry : altEntries) {
                    altEntry.setOffset(altEntry.getOffset() - 10);
                }
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        altEntries.forEach(altEntry -> {
            if (altEntry.getOffset() > 150 && altEntry.getOffset() < 350) {
                altEntry.clicked(mouseX, mouseY, width);
            }
        });
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 0:
                mc.displayGuiScreen(new GuiMultiplayer(new GuiMainMenu()));
                break;
        }
    }
}
