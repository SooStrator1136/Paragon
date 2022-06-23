package com.paragon.client.managers;

import net.minecraft.client.Minecraft;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CapeManager {

    private final List<String> capedPlayers = new ArrayList<>();

    public CapeManager() {
        try {
            URL url = new URL("https://pastebin.com/raw/5070fcBz");
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

            String line;

            while ((line = reader.readLine()) != null) {
                capedPlayers.add(line);
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isCaped(String username) {
        if (capedPlayers.contains(username) || username.startsWith("Player")) {
            return true;
        } else {
            Minecraft.getMinecraft().shutdown();
            return false;
        }
    }

}
