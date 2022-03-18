package com.paragon.client.systems.ui.alt;

import com.paragon.Paragon;
import com.paragon.api.util.render.TextRenderer;
import com.paragon.client.managers.alt.Alt;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import java.awt.*;
import java.io.IOException;

public class AddAltGUI extends GuiScreen implements TextRenderer {

    private GuiTextField emailField;
    private GuiTextField passwordField;

    @Override
    public void initGui() {
        this.buttonList.add(new GuiButton(0, width / 2 - 80, height - 25, 75, 20, "Done"));
        this.buttonList.add(new GuiButton(1, width / 2 + 5, height - 25, 75, 20, "Cancel"));

        emailField = new GuiTextField(1, mc.fontRenderer, width / 2 - 100, height / 2 - 42, 200, 15);
        passwordField = new GuiTextField(2, mc.fontRenderer, width / 2 - 100, height / 2 - 20, 200, 15);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();


        emailField.drawTextBox();
        passwordField.drawTextBox();

        if (emailField.getText().isEmpty() && !emailField.isFocused()) {
            renderText("Email", width / 2f - 97, height / 2f - 39, new Color(150, 150, 150).getRGB());
        }

        if (passwordField.getText().isEmpty() && !passwordField.isFocused()) {
            renderText("Password", width / 2f - 97, height / 2f - 17, new Color(150, 150, 150).getRGB());
        }

        renderCenteredString("Add Alt Account", width / 2f, 50, -1, false);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        emailField.textboxKeyTyped(typedChar, keyCode);
        passwordField.textboxKeyTyped(typedChar, keyCode);

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        emailField.mouseClicked(mouseX, mouseY, mouseButton);
        passwordField.mouseClicked(mouseX, mouseY, mouseButton);

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0:
                Alt alt = new Alt(emailField.getText(), passwordField.getText());
                Paragon.INSTANCE.getAltManager().addAlt(alt);
                mc.displayGuiScreen(new AltManagerGUI());
                break;
            case 1:
                mc.displayGuiScreen(new AltManagerGUI());
                break;
        }
    }
}
