package com.paragon.client.managers;

import com.paragon.Paragon;
import com.paragon.api.util.render.font.FontRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.awt.*;
import java.io.*;
import java.net.URL;

@SideOnly(Side.CLIENT)
public class FontManager {

    private final FontRenderer fontRenderer;
    private float yIncrease;

    public FontManager() {
        fontRenderer = new FontRenderer(getFont());
    }

    public FontRenderer getFontRenderer() {
        return fontRenderer;
    }

    public float getYIncrease() {
        return yIncrease;
    }

    public Font getFont() {
        File fontDir = new File("paragon/font/");

        if (!fontDir.exists()) {
            fontDir.mkdirs();
        }

        // We need to download default font
        if (fontDir.listFiles().length < 2) {
            Paragon.INSTANCE.getLogger().info("Downloading default font...");

            String defaultFont = "https://www.dropbox.com/s/wki4fxll36znfu4/font.ttf?dl=1";

            try {
                BufferedInputStream in = new BufferedInputStream(new URL(defaultFont).openStream());
                FileOutputStream fileOutputStream = new FileOutputStream("paragon/font/font.ttf");
                byte[] dataBuffer = new byte[1024];

                int bytesRead;
                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                }

                Paragon.INSTANCE.getLogger().info("Downloaded default font");
            } catch (IOException e) {
                Paragon.INSTANCE.getLogger().error("Failed to download default font");
            }

            File fontConfigFile = new File("paragon/font/font_config.json");

            try {
                JSONObject json = new JSONObject();
                FileWriter fileWriter = new FileWriter(fontConfigFile);

                try {
                    json.put("size", 40);
                    json.put("y_offset", 0);

                    fileWriter.write(json.toString(4));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        float size = 40;

        try {
            JSONObject jsonObject = new JSONObject(FileUtils.readFileToString(new File("paragon/font/font_config.json"), Charsets.UTF_8));

            size = jsonObject.getInt("size");
            yIncrease = jsonObject.getFloat("y_offset");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            InputStream fontStream = new FileInputStream("paragon/font/font.ttf");

            Font font = Font.createFont(0, fontStream);
            fontStream.close();

            return font.deriveFont(Font.PLAIN, size);
        } catch (Exception exception) {
            exception.printStackTrace();
            return new Font("default", Font.PLAIN, (int) size);
        }
    }

}
