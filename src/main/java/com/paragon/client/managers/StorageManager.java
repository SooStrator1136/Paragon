package com.paragon.client.managers;

import com.paragon.Paragon;
import com.paragon.client.managers.alt.Alt;
import com.paragon.client.managers.social.Player;
import com.paragon.client.managers.social.Relationship;
import com.paragon.client.systems.module.hud.HUDModule;
import com.paragon.client.systems.module.setting.Bind;
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
    public File configFolder = new File("paragon/configs/");
    public File socialFolder = new File("paragon/social");

    @SuppressWarnings("all")
    public static JSONObject getJSON(File file) throws IOException, JSONException {
        return new JSONObject(FileUtils.readFileToString(file, Charsets.UTF_8));
    }

    public void saveModules(String configName) {
        // Create configs folder if it doesn't already exist
        if (!configFolder.exists()) {
            configFolder.mkdirs();
        }

        File saveConfigFolder = new File("paragon/configs/" + configName);

        // Create the folder if it doesn't exist
        if (!saveConfigFolder.exists()) {
            saveConfigFolder.mkdirs();
        }

        Paragon.INSTANCE.getModuleManager().getModules().forEach(module -> {
            try {
                JSONObject jsonObject = new JSONObject();

                jsonObject.put("enabled", module.isEnabled());

                if (module instanceof HUDModule) {
                    jsonObject.put("x", ((HUDModule) module).getX());
                    jsonObject.put("y", ((HUDModule) module).getY());
                }

                for (Setting<?> setting : module.getSettings()) {
                    if (setting.getValue() instanceof Color) {
                        jsonObject.put(setting.getName(), ((Color) setting.getValue()).getRed() + ":" + ((Color) setting.getValue()).getGreen() + ":" + ((Color) setting.getValue()).getBlue() + ":" + setting.getAlpha() + ":" + setting.isRainbow() + ":" + setting.getRainbowSpeed() + ":" + setting.getRainbowSaturation() + ":" + setting.isSync());
                    } else if (setting.getValue() instanceof Bind) {
                        jsonObject.put(setting.getName(), ((Bind) setting.getValue()).getButtonCode() + ":" + ((Bind) setting.getValue()).getDevice());
                    } else {
                        jsonObject.put(setting.getName(), setting.getValue());
                    }

                    if (!setting.getSubsettings().isEmpty()) {
                        for (Setting<?> subsetting : setting.getSubsettings()) {
                            String subsettingName = subsetting.getParentSetting().getName() + " " + subsetting.getName();

                            if (subsetting.getValue() instanceof Color) {
                                jsonObject.put(subsettingName, ((Color) subsetting.getValue()).getRed() + ":" + ((Color) subsetting.getValue()).getGreen() + ":" + ((Color) subsetting.getValue()).getBlue() + ":" +  subsetting.getAlpha() + ":" + subsetting.isRainbow() + ":" + subsetting.getRainbowSpeed() + ":" + subsetting.getRainbowSaturation() + ":" + subsetting.isSync());
                            } else if (subsetting.getValue() instanceof Bind) {
                                jsonObject.put(subsettingName, ((Bind) subsetting.getValue()).getButtonCode() + ":" + ((Bind) subsetting.getValue()).getDevice());
                            } else {
                                jsonObject.put(subsettingName, subsetting.getValue());
                            }
                        }
                    }
                }

                // Write to file
                FileWriter fileWriter = new FileWriter(new File(saveConfigFolder, module.getName() + ".json"));
                fileWriter.write(jsonObject.toString(4));
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void loadModules(String configName) {
        File loadFolder = new File("paragon/configs/" + configName);

        if (!loadFolder.exists()) {
            loadFolder.mkdirs();
        }

        try {
            Paragon.INSTANCE.getModuleManager().getModules().forEach(module -> {
                try {
                    JSONObject moduleJSON = getJSON(new File(loadFolder, module.getName() + ".json"));

                    if (moduleJSON.has("x") && moduleJSON.has("y")) {
                        ((HUDModule) module).setX(moduleJSON.getInt("x"));
                        ((HUDModule) module).setY(moduleJSON.getInt("y"));
                    }

                    for (Setting<?> setting : module.getSettings()) {
                        try {
                            if (setting.getValue() instanceof Boolean) {
                                ((Setting<Boolean>) setting).setValue(moduleJSON.getBoolean(setting.getName()));
                            }

                            else if (setting.getValue() instanceof Bind) {
                                String[] parts = moduleJSON.getString(setting.getName()).split(":");

                                ((Bind) setting.getValue()).setButtonCode(Integer.parseInt(parts[0]));
                                ((Bind) setting.getValue()).setDevice(Enum.valueOf(Bind.Device.class, parts[1]));
                            }

                            else if (setting.getValue() instanceof Float) {
                                ((Setting<Float>) setting).setValue(moduleJSON.getFloat(setting.getName()));
                            }

                            else if (setting.getValue() instanceof Double) {
                                ((Setting<Double>) setting).setValue(moduleJSON.getDouble(setting.getName()));
                            }

                            else if (setting.getValue() instanceof Enum<?>) {
                                Enum<?> value = Enum.valueOf(((Enum) setting.getValue()).getClass(), moduleJSON.getString(setting.getName()));

                                int i = 0;
                                for (Enum<?> enumValue : ((Enum<?>) setting.getValue()).getClass().getEnumConstants()) {
                                    if (enumValue.name().equals(value.name())) {
                                        setting.setIndex(i);
                                        break;
                                    }

                                    i++;
                                }

                                ((Setting<Enum<?>>) setting).setValue(value);
                            }

                            else if (setting.getValue() instanceof Color) {
                                String[] values = moduleJSON.getString(setting.getName()).split(":");

                                Color colour = new Color(Integer.parseInt(values[0]) / 255f, Integer.parseInt(values[1]) / 255f, Integer.parseInt(values[2]) / 255f, Float.parseFloat(values[3]) / 255f);

                                ((Setting<Color>) setting).setValue(colour);
                                setting.setRainbow(Boolean.parseBoolean(values[4]));
                                setting.setRainbowSpeed(Float.parseFloat(values[5]));
                                setting.setRainbowSaturation(Float.parseFloat(values[6]));
                                setting.setSync(Boolean.parseBoolean(values[7]));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        
                        for (Setting<?> subsetting : setting.getSubsettings()) {
                            String subsettingName = setting.getName() + " " + subsetting.getName();

                            try {
                                if (subsetting.getValue() instanceof Boolean) {
                                    ((Setting<Boolean>) subsetting).setValue(moduleJSON.getBoolean(subsettingName));
                                }

                                else if (subsetting.getValue() instanceof Bind) {
                                    String[] parts = moduleJSON.getString(subsettingName).split(":");

                                    ((Bind) subsetting.getValue()).setButtonCode(Integer.parseInt(parts[0]));
                                    ((Bind) subsetting.getValue()).setDevice(Enum.valueOf(Bind.Device.class, parts[1]));
                                }

                                else if (subsetting.getValue() instanceof Float) {
                                    ((Setting<Float>) subsetting).setValue(moduleJSON.getFloat(subsettingName));
                                }

                                else if (subsetting.getValue() instanceof Double) {
                                    ((Setting<Double>) subsetting).setValue(moduleJSON.getDouble(subsettingName));
                                }

                                else if (subsetting.getValue() instanceof Enum<?>) {
                                    Enum<?> value = Enum.valueOf(((Enum) subsetting.getValue()).getClass(), moduleJSON.getString(subsettingName));

                                    int i = 0;
                                    for (Enum<?> enumValue : ((Enum<?>) subsetting.getValue()).getClass().getEnumConstants()) {
                                        if (enumValue.name().equals(value.name())) {
                                            subsetting.setIndex(i);
                                            break;
                                        }

                                        i++;
                                    }

                                    ((Setting<Enum<?>>) subsetting).setValue(value);
                                }

                                else if (subsetting.getValue() instanceof Color) {
                                    String[] values = moduleJSON.getString(subsettingName).split(":");

                                    Color colour = new Color(Integer.parseInt(values[0]) / 255f, Integer.parseInt(values[1]) / 255f, Integer.parseInt(values[2]) / 255f, Float.parseFloat(values[3]) / 255f);

                                    ((Setting<Color>) subsetting).setValue(colour);
                                    subsetting.setRainbow(Boolean.parseBoolean(values[4]));
                                    subsetting.setRainbowSpeed(Float.parseFloat(values[5]));
                                    subsetting.setRainbowSaturation(Float.parseFloat(values[6]));
                                    subsetting.setSync(Boolean.parseBoolean(values[7]));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (moduleJSON.getBoolean("enabled") == !module.isEnabled()) {
                        module.toggle();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
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
            JSONObject jsonObject = getJSON(new File("paragon/social/social_interactions.json"));

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
        // Create main folder if it doesn't already exist
        if (!mainFolder.exists()) {
            mainFolder.mkdirs();
        }

        try {
            // Load JSON
            JSONObject jsonObject = getJSON(new File("paragon/alts.json"));

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

    public void saveOther() {
        if (!mainFolder.exists()) {
            mainFolder.mkdirs();
        }

        try {
            File file = new File("paragon/client.json");
            file.createNewFile();

            JSONObject jsonObject = new JSONObject();

            FileWriter fileWriter = new FileWriter(file);

            try {
                jsonObject.put("mainmenu", Paragon.INSTANCE.isParagonMainMenu());
                fileWriter.write(jsonObject.toString(4));
            } catch (IOException exception) {
                exception.printStackTrace();
            }

            fileWriter.flush();
            fileWriter.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void loadOther() {
        if (!mainFolder.exists()) {
            mainFolder.mkdirs();
        }

        try {
            // Load JSON
            JSONObject jsonObject = getJSON(new File("paragon/client.json"));

            Paragon.INSTANCE.setParagonMainMenu(jsonObject.getBoolean("mainmenu"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
