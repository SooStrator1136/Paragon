package com.paragon.client.managers;

import com.paragon.Paragon;
import com.paragon.client.managers.alt.Alt;
import com.paragon.client.managers.social.Player;
import com.paragon.client.managers.social.Relationship;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.hud.HUDModule;
import com.paragon.client.systems.module.setting.Setting;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Wolfsurge
 * @since 2/2/22
 */
@SuppressWarnings("all") // FUCK warnings
public class StorageManager {

    public File mainFolder = new File("paragon");
    public File modulesFolder = new File("paragon/modules/");
    public File socialFolder = new File("paragon/social");

    /**
     * Saves a module config to a file
     * @param moduleIn The module
     */
    public void saveModuleConfiguration(Module moduleIn) {
        if (!modulesFolder.exists()) {
            modulesFolder.mkdirs();
        }

        try {
            File file = new File("paragon/modules/" + moduleIn.getName() + ".json");
            JSONObject jsonObject = new JSONObject();
            file.createNewFile();
            FileWriter fileWriter = new FileWriter(file);

            try {
                jsonObject.put("enabled", moduleIn.isEnabled());
                jsonObject.put("visible", moduleIn.isVisible());

                if (moduleIn instanceof HUDModule) {
                    jsonObject.put("x position", ((HUDModule) moduleIn).getX());
                    jsonObject.put("y position", ((HUDModule) moduleIn).getY());
                }

                for (Setting<?> setting : moduleIn.getSettings()) {
                    if (setting.getValue() instanceof Color) {
                        jsonObject.put(setting.getName(), ((Color) setting.getValue()).getRed() + ":" + ((Color) setting.getValue()).getGreen() + ":" + ((Color) setting.getValue()).getBlue() + ":" + ((Color) setting.getValue()).getAlpha() + ":" + setting.isRainbow() + ":" + setting.getRainbowSpeed() + ":" + setting.getRainbowSaturation() + ":" + setting.isSync());
                    }

                    else if (setting.getValue() instanceof AtomicInteger) {
                        jsonObject.put(setting.getName(), ((AtomicInteger) setting.getValue()).get());
                    }

                    else {
                        jsonObject.put(setting.getName(), setting.getValue());
                    }

                    if (!setting.getSubsettings().isEmpty()) {
                        for (Setting<?> subsetting : setting.getSubsettings()) {
                            String name = subsetting.getParentSetting().getName() + " " + subsetting.getName();

                            if (subsetting.getValue() instanceof Color) {
                                jsonObject.put(name, ((Color) subsetting.getValue()).getRed() + ":" + ((Color) subsetting.getValue()).getGreen() + ":" + ((Color) subsetting.getValue()).getBlue() + ":" + ((Color) subsetting.getValue()).getAlpha() + ":" + subsetting.isRainbow() + ":" + subsetting.getRainbowSpeed() + ":" + subsetting.getRainbowSaturation() + ":" + subsetting.isSync());
                            }

                            else if (subsetting.getValue() instanceof AtomicInteger) {
                                jsonObject.put(name, ((AtomicInteger) subsetting.getValue()).get());
                            }

                            else {
                                jsonObject.put(name, subsetting.getValue());
                            }
                        }
                    }
                }

                fileWriter.write(jsonObject.toString(4));
            } catch (IOException e) {
                e.printStackTrace();
            }

            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads a module's config
     */
    public void loadModuleConfiguration(Module moduleIn) {
        if (!modulesFolder.exists()) {
            modulesFolder.mkdirs();
        }

        try {
            JSONObject jsonObject = loadExistingConfiguration(new File("paragon/modules/" + moduleIn.getName() + ".json"));

            // Toggle module if it's enabled
            if (jsonObject.getBoolean("enabled") && !moduleIn.isConstant()) {
                moduleIn.toggle();
            }

            moduleIn.setVisible(jsonObject.getBoolean("visible"));

            if (moduleIn instanceof HUDModule) {
                ((HUDModule) moduleIn).setX(jsonObject.getFloat("x position"));
                ((HUDModule) moduleIn).setY(jsonObject.getFloat("y position"));
            }

            for (Setting<?> setting : moduleIn.getSettings()) {
                try {
                    if (setting.getValue() instanceof Boolean) {
                        ((Setting<Boolean>) setting).setValue(jsonObject.getBoolean(setting.getName()));
                    }

                    else if (setting.getValue() instanceof AtomicInteger) {
                        ((Setting<AtomicInteger>) setting).getValue().set(jsonObject.getInt(setting.getName()));
                    }

                    else if (setting.getValue() instanceof Float) {
                        ((Setting<Float>) setting).setValue(jsonObject.getFloat(setting.getName()));
                    }

                    else if (setting.getValue() instanceof Double) {
                        ((Setting<Double>) setting).setValue(jsonObject.getDouble(setting.getName()));
                    }

                    else if (setting.getValue() instanceof Enum<?>) {
                        Enum<?> value = Enum.valueOf(((Enum) setting.getValue()).getClass(), jsonObject.getString(setting.getName()));
                        ((Setting<Enum<?>>) setting).setValue(value);
                    }

                    else if (setting.getValue() instanceof Color) {
                        String[] values = jsonObject.getString(setting.getName()).split(":");

                        Color colour = new Color(Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]), Integer.parseInt(values[3]));

                        ((Setting<Color>) setting).setValue(colour);
                        setting.setAlpha(colour.getAlpha());
                        setting.setRainbow(Boolean.parseBoolean(values[4]));
                        setting.setRainbowSpeed(Float.parseFloat(values[5]));
                        setting.setRainbowSaturation(Float.parseFloat(values[6]));
                        setting.setSync(Boolean.parseBoolean(values[7]));
                    }
                } catch (Exception e) {
                    System.out.println("Failed to load setting " + setting.getName());
                    e.printStackTrace();
                }

                if (!setting.getSubsettings().isEmpty()) {
                    for (Setting<?> subsetting : setting.getSubsettings()) {
                        String name = setting.getName() + " " + subsetting.getName();

                        try {
                            if (subsetting.getValue() instanceof Boolean) {
                                ((Setting<Boolean>) subsetting).setValue(jsonObject.getBoolean(name));
                            }

                            else if (subsetting.getValue() instanceof AtomicInteger) {
                                ((Setting<AtomicInteger>) subsetting).getValue().set(jsonObject.getInt(name));
                            }

                            else if (subsetting.getValue() instanceof Float) {
                                ((Setting<Float>) subsetting).setValue(jsonObject.getFloat(name));
                            }

                            else if (subsetting.getValue() instanceof Double) {
                                ((Setting<Double>) subsetting).setValue(jsonObject.getDouble(name));
                            }

                            else if (subsetting.getValue() instanceof Enum<?>) {
                                Enum<?> value = Enum.valueOf(((Enum) subsetting.getValue()).getClass(), jsonObject.getString(name));
                                ((Setting<Enum<?>>) subsetting).setValue(value);
                            }

                            else if (subsetting.getValue() instanceof Color) {
                                String[] values = jsonObject.getString(name).split(":");

                                Color colour = new Color(Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]), Integer.parseInt(values[3]));

                                ((Setting<Color>) subsetting).setValue(colour);
                                subsetting.setAlpha(colour.getAlpha());
                                subsetting.setRainbow(Boolean.parseBoolean(values[4]));
                                subsetting.setRainbowSpeed(Float.parseFloat(values[5]));
                                subsetting.setRainbowSaturation(Float.parseFloat(values[6]));
                                subsetting.setSync(Boolean.parseBoolean(values[7]));
                            }
                        } catch (Exception e) {
                            System.out.println("Failed to load setting " + name);
                            e.printStackTrace();
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves the social interactions
     */
    public void saveSocial() {
        // Create friends folder if it doesn't already exist
        if (!socialFolder.exists()) {
            socialFolder.mkdirs();
        }

        try {
            // Create new friends json
            File file = new File("paragon/social/social_interactions.json");

            // Create file
            file.createNewFile();

            // Create new JSON object
            JSONObject jsonObject = new JSONObject();

            // Create file writer
            FileWriter fileWriter = new FileWriter(file);

            try {
                // Create a new array (for player info)
                JSONArray array = new JSONArray();

                for (Player player : Paragon.INSTANCE.getSocialManager().players) {
                    // Put the player's info in the array - name:relationship
                    array.put(player.getName() + ":" + player.getRelationship().name());
                }

                // Add array to json object
                jsonObject.putOpt("acquaintances", array);

                // Write JSON to file
                fileWriter.write(jsonObject.toString(4));
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Flush file writer
            fileWriter.flush();

            // Close file writer
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the social interactions
     */
    public void loadSocial() {
        // Create friends folder if it doesn't already exist
        if (!socialFolder.exists()) {
            socialFolder.mkdirs();
        }

        try {
            // Load JSON
            JSONObject jsonObject = loadExistingConfiguration(new File("paragon/social/social_interactions.json"));

            // Load array
            JSONArray acquaintances = jsonObject.getJSONArray("acquaintances");

            // For every value in array, create a new player
            for (int i = 0; i < acquaintances.length(); i++) {
                String[] info = String.valueOf(acquaintances.get(i)).split(":");
                Player player = new Player(info[0], Relationship.valueOf(info[1]));
                Paragon.INSTANCE.getSocialManager().addPlayer(player);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveAlts() {
        // Create main folder if it doesn't already exist
        if (!mainFolder.exists()) {
            mainFolder.mkdirs();
        }

        try {
            // Create new friends json
            File file = new File("paragon/alts.json");

            // Create file
            file.createNewFile();

            // Create new JSON object
            JSONObject jsonObject = new JSONObject();

            // Create file writer
            FileWriter fileWriter = new FileWriter(file);

            try {
                // Create a new array (for alt info)
                JSONArray array = new JSONArray();

                for (Alt alt : Paragon.INSTANCE.getAltManager().getAlts()) {
                    // Put the player's info in the array - email:password
                    array.put(alt.getEmail() + ":" + alt.getPassword());
                }

                // Add array to json object
                jsonObject.putOpt("alts", array);

                // Write JSON to file
                fileWriter.write(jsonObject.toString(4));
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Flush file writer
            fileWriter.flush();

            // Close file writer
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadAlts() {
        // Create friends folder if it doesn't already exist
        if (!mainFolder.exists()) {
            mainFolder.mkdirs();
        }

        try {
            // Load JSON
            JSONObject jsonObject = loadExistingConfiguration(new File("paragon/alts.json"));

            // Load array
            JSONArray alts = jsonObject.getJSONArray("alts");

            // For every value in array, create a new player
            for (int i = 0; i < alts.length(); i++) {
                String[] info = String.valueOf(alts.get(i)).split(":");
                Alt alt = new Alt(info[0], info[1]);
                Paragon.INSTANCE.getAltManager().addAlt(alt);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("all")
    public static JSONObject loadExistingConfiguration(File file) throws IOException, JSONException {
        return new JSONObject(FileUtils.readFileToString(file, Charsets.UTF_8));
    }

}
