package com.paragon.client.managers.alt;

import com.paragon.asm.mixins.accessor.IMinecraft;
import com.paragon.client.ui.alt.AltManagerGUI;
import fr.litarvan.openauth.microsoft.MicrosoftAuthResult;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticationException;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticator;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import net.minecraft.util.text.TextFormatting;

public class Alt {

    private String email;
    private String password;
    private Session session;

    public Alt(String email, String password) {
        this.email = email;
        this.password = password;
    }

    /**
     * Gets the email of the alt
     *
     * @return The email of the alt
     */
    public String getEmail() {
        return email;
    }

    /**
     * Gets the password of the alt
     *
     * @return The password of the alt
     */
    public String getPassword() {
        return password;
    }

    /**
     * Logs the user into an alt account
     */
    public boolean login() {
        if (session == null) {
            // Authenticator
            MicrosoftAuthenticator auth = new MicrosoftAuthenticator();
            System.out.println("logging in as " + this.email);
            try {
                // Get auth result
                MicrosoftAuthResult result = auth.loginWithCredentials(this.email, this.password);
                // Set alt session
                this.session = new Session(result.getProfile().getName(), result.getProfile().getId(), result.getAccessToken(), "legacy");
            } catch (MicrosoftAuthenticationException microsoftAuthenticationException) {
                microsoftAuthenticationException.printStackTrace();
            }
        }

        // Return false if the session is null
        if (this.session == null) {
            AltManagerGUI.renderString = TextFormatting.RED + "Unsuccessful Login!";
            return false;
        }

        // Set Minecraft session
        ((IMinecraft) Minecraft.getMinecraft()).setSession(this.session);
        AltManagerGUI.renderString = TextFormatting.GREEN + "Successful Login!";

        return true;
    }
}
