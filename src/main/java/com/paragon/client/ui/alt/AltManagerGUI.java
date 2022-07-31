package com.paragon.client.ui.alt;

import com.paragon.Paragon;
import com.paragon.api.util.Wrapper;
import com.paragon.api.util.render.RenderUtil;

import com.paragon.api.util.render.font.FontUtil;
import com.paragon.asm.mixins.accessor.IMinecraft;
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

public final class AltManagerGUI extends GuiScreen implements Wrapper {

    public static AltEntry selectedAltEntry;
    public static String renderString = TextFormatting.GRAY + "Idle";
    private final ArrayList<AltEntry> altEntries = new ArrayList<>(3);

    @Override
    public void initGui() {
        renderString = TextFormatting.GRAY + "Idle";

        altEntries.clear();

        float offset = 150;
        for (Alt alt : Paragon.INSTANCE.getAltManager().getAlts()) {
            altEntries.add(new AltEntry(alt, offset));

            offset += 20.0F;
        }

        this.buttonList.add(new GuiButton(0, 5, 5, 75, 20, "Back"));
        this.buttonList.add(new GuiButton(1, width / 2 - 80, height - 25, 75, 20, "Add Alt"));
        this.buttonList.add(new GuiButton(2, width / 2 + 5, height - 25, 75, 20, "Delete"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        this.buttonList.get(2).enabled = selectedAltEntry != null;

        scroll();

        RenderUtil.drawRect(0, 150, width, 200, 0x90000000);
        RenderUtil.pushScissor(0, 150, width, 200);

        altEntries.forEach(altEntry -> altEntry.drawAlt(mouseX, mouseY, width));

        RenderUtil.popScissor();

        FontUtil.drawStringWithShadow("Logged in as " + TextFormatting.GRAY + ((IMinecraft) Minecraft.getMinecraft()).getSession().getUsername(), 5, 30, - 1);
        FontUtil.renderCenteredString("Paragon Alt Manager", width / 2f, 75, -1, false);
        FontUtil.renderCenteredString(renderString, width / 2f, 100, -1, false);

        super.drawScreen(mouseX, mouseY, partialTicks);
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
            if (isHovered(0, 150, width, 350, mouseX, mouseY)) {
                if (isHovered(0, altEntry.getOffset(), width, altEntry.getOffset() + 20, mouseX, mouseY)) {
                    if (selectedAltEntry == altEntry) {
                        renderString = "Logging in with the email: " + altEntry.getAlt().getEmail();
                        altEntry.clicked(mouseX, mouseY, width);
                    } else {
                        selectedAltEntry = altEntry;
                    }
                }
            }
        });

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0:
                Wrapper.mc.displayGuiScreen(new GuiMultiplayer(new GuiMainMenu()));
                break;
            case 1:
                Wrapper.mc.displayGuiScreen(new AddAltGUI());
                break;
            case 2:
                Paragon.INSTANCE.getAltManager().getAlts().removeIf(alt -> alt.getEmail().equals(selectedAltEntry.getAlt().getEmail()) && alt.getPassword().equals(selectedAltEntry.getAlt().getPassword()));
                altEntries.remove(selectedAltEntry);
                break;
        }
    }

    @Override
    public void onGuiClosed() {
        Paragon.INSTANCE.getStorageManager().saveAlts();
    }

}
