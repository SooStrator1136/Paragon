package com.paragon.client.managers;

import com.paragon.Paragon;
import com.paragon.client.managers.alt.Alt;
import com.paragon.client.managers.social.Player;
import com.paragon.client.managers.social.Relationship;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.settings.Setting;
import com.paragon.client.systems.module.settings.impl.*;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.awt.*;
import java.io.*;

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

                for (Setting setting : moduleIn.getSettings()) {
                    if (setting instanceof BooleanSetting) {
                        jsonObject.put(setting.getName(), ((BooleanSetting) setting).isEnabled());
                    } else if (setting instanceof NumberSetting) {
                        jsonObject.put(setting.getName(), ((NumberSetting) setting).getValue());
                    } else if (setting instanceof ModeSetting) {
                        jsonObject.put(setting.getName(), ((ModeSetting) setting).getCurrentMode());
                    } else if (setting instanceof ColourSetting) {
                        jsonObject.put(setting.getName(), ((ColourSetting) setting).getColour().getRGB());
                    } else if (setting instanceof KeybindSetting) {
                        jsonObject.put(setting.getName(), ((KeybindSetting) setting).getKeyCode());
                    }

                    if (!setting.getSubsettings().isEmpty()) {
                        setting.getSubsettings().forEach(setting1 -> {
                            String settingName = setting1.getParentSetting().getName() + "-" + setting1.getName();

                            try {
                                if (setting1 instanceof BooleanSetting) {
                                    jsonObject.put(settingName, ((BooleanSetting) setting1).isEnabled());
                                } else if (setting1 instanceof NumberSetting) {
                                    jsonObject.put(settingName, ((NumberSetting) setting1).getValue());
                                } else if (setting1 instanceof ModeSetting<?>) {
                                    jsonObject.put(settingName, ((ModeSetting<?>) setting1).getCurrentMode().toString());
                                } else if (setting1 instanceof ColourSetting) {
                                    jsonObject.put(settingName, ((ColourSetting) setting1).getColour().getRGB());
                                } else if (setting1 instanceof KeybindSetting) {
                                    jsonObject.put(settingName, ((KeybindSetting) setting1).getKeyCode());
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
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
            if (jsonObject.getBoolean("enabled")) {
                moduleIn.toggle();
            }

            moduleIn.setVisible(jsonObject.getBoolean("visible"));

            for (Setting setting : moduleIn.getSettings()) {
                if (setting instanceof BooleanSetting) {
                    ((BooleanSetting) setting).setEnabled(jsonObject.getBoolean(setting.getName()));
                } else if (setting instanceof NumberSetting) {
                    ((NumberSetting) setting).setValue(jsonObject.getFloat(setting.getName()));
                } else if (setting instanceof ModeSetting<?>) {
                    Enum newValue = Enum.valueOf(((Enum<?>) ((ModeSetting<?>) setting).getCurrentMode()).getClass(), jsonObject.getString(setting.getName()));
                    ((ModeSetting<Enum<?>>) setting).setCurrentMode(newValue);
                } else if (setting instanceof ColourSetting) {
                    ((ColourSetting) setting).setColour(new Color(jsonObject.getInt(setting.getName())));
                } else if (setting instanceof KeybindSetting) {
                    ((KeybindSetting) setting).setKeyCode(jsonObject.getInt(setting.getName()));
                }

                for (Setting subSetting : setting.getSubsettings()) {
                    String settingName = subSetting.getParentSetting().getName() + "-" + subSetting.getName();

                    if (subSetting instanceof BooleanSetting) {
                        ((BooleanSetting) subSetting).setEnabled(jsonObject.getBoolean(settingName));
                    } else if (subSetting instanceof NumberSetting) {
                        ((NumberSetting) subSetting).setValue(jsonObject.getFloat(settingName));
                    } else if (subSetting instanceof ModeSetting<?>) {
                        Enum newValue = Enum.valueOf(((Enum<?>) ((ModeSetting<?>) subSetting).getCurrentMode()).getClass(), jsonObject.getString(settingName));
                        ((ModeSetting<Enum<?>>) subSetting).setCurrentMode(newValue);
                    } else if (subSetting instanceof ColourSetting) {
                        ((ColourSetting) subSetting).setColour(new Color(jsonObject.getInt(settingName)));
                    } else if (subSetting instanceof KeybindSetting) {
                        ((KeybindSetting) subSetting).setKeyCode(jsonObject.getInt(settingName));
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
