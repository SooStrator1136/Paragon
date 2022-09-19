package com.paragon.impl.managers

import com.paragon.Paragon
import com.paragon.impl.setting.Setting
import com.paragon.impl.module.hud.HUDModule
import com.paragon.impl.managers.alt.Alt
import com.paragon.impl.setting.Bind
import com.paragon.impl.setting.Bind.Device
import org.apache.commons.io.FileUtils
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.awt.Color
import java.io.File
import java.io.FileNotFoundException
import java.io.FileWriter
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files

/**
 * @author SooStrator1136
 */
class StorageManager {

    private val mainFolder = File("paragon")
    private val configFolder = File("paragon${File.separator}configs${File.separator}")
    private val socialFolder = File("paragon${File.separator}social")

    var mainMenuShadow: String = "#ifdef GL_ES\n" + "precision mediump float;\n" + "#endif\n" + "\n" + "#extension GL_OES_standard_derivatives : enable\n" + "\n" + "uniform vec2      resolution;\n" + "uniform float     time;\n" + "\n" + "\n" + "float rand(vec2 n) {\n" + "    return fract(cos(dot(n, vec2(16.9898, 10.1414))) * 93758.5453);\n" + "}\n" + "\n" + "float noise(vec2 n) {\n" + "    const vec2 d = vec2(0.0, 1.0);\n" + "    vec2 b = floor(n), f = smoothstep(vec2(0.0), vec2(1.0), fract(n));\n" + "    return mix(mix(rand(b), rand(b + d.yx), f.x), mix(rand(b + d.xy), rand(b + d.yy), f.x), f.y);\n" + "}\n" + "\n" + "float fbm(vec2 n) {\n" + "    float total = 0.0, amplitude = 1.0;\n" + "    for (int i = 0; i < 10; i++) {\n" + "        total += noise(n) * amplitude;\n" + "        n += n;\n" + "        amplitude *= 0.3;\n" + "    }\n" + "    return total;\n" + "}\n" + "\n" + "void main() {\n" + "    const vec3 c1 = vec3(300.0/255.0, 50.0/255.0, 197.0/255.0);\n" + "    const vec3 c2 = vec3(-11.0/255.0, 50.0/255.0, 111.4/255.0);\n" + "    const vec3 c3 = vec3(0.2 + .19, 0.19, 0.19);\n" + "    const vec3 c4 = vec3(6./255.0, 150.0/255.0, 260./255.0);\n" + "    const vec3 c5 = vec3(0.6);\n" + "    const vec3 c6 = vec3(.3);\n" + "\n" + "    vec2 p = gl_FragCoord.xy * 7.0 / resolution.xx;\n" + "    float q = fbm(p - time * 0.1);\n" + "    vec2 r = vec2(fbm(p + q + time * 0.1 - p.x - p.y), fbm(p + q - time * -0.1));\n" + "    vec3 c = mix(c1, c2, fbm(p + r)) + mix(c3, c4, r.x) - mix(c5, c6, r.y);\n" + "    gl_FragColor = vec4(c * cos(0.0 * gl_FragCoord.y / resolution.y), 1.0);\n" + "    gl_FragColor.w = 0.8;\n" + "}"

    @Throws(IOException::class, JSONException::class)
    private fun getJSON(file: File) = if (!Files.exists(file.toPath())) null else JSONObject(FileUtils.readFileToString(file, StandardCharsets.UTF_8))

    fun saveModules(configName: String) {
        // Create configs folder if it doesn't already exist
        if (!configFolder.exists()) {
            configFolder.mkdirs()
        }

        val configFile = File("paragon${File.separator}configs${File.separator}$configName.json")

        val json = JSONObject()

        Paragon.INSTANCE.moduleManager.modules.forEach {
            try {
                val settings = JSONObject()

                settings.put("enabled", it.isEnabled)

                if (it is HUDModule) {
                    settings.put("x", it.x)
                    settings.put("y", it.y)
                }

                it.settings.forEach { setting ->
                    try {
                        when (setting.value) {
                            is Color -> {
                                val color = setting.value as Color
                                settings.put(
                                    setting.name, color.red.toString() + ":" + color.green + ":" + color.blue + ":" + setting.alpha + ":" + setting.isRainbow + ":" + setting.rainbowSpeed + ":" + setting.rainbowSaturation + ":" + setting.isSync
                                )
                            }

                            is Bind -> {
                                val bind = setting.value as Bind
                                settings.put(
                                    setting.name, bind.buttonCode.toString() + ":" + bind.device
                                )
                            }

                            else -> settings.put(setting.name, setting.value)
                        }

                        if (setting.subsettings.isNotEmpty()) {
                            for (subSetting in setting.subsettings) {
                                val subSettingName = subSetting.parentSetting?.name + " " + subSetting.name
                                when (subSetting.value) {
                                    is Color -> {
                                        val color = subSetting.value as Color
                                        settings.put(
                                            subSettingName, color.red.toString() + ":" + color.green + ":" + color.blue + ":" + subSetting.alpha + ":" + subSetting.isRainbow + ":" + subSetting.rainbowSpeed + ":" + subSetting.rainbowSaturation + ":" + subSetting.isSync
                                        )
                                    }

                                    is Bind -> {
                                        val bind = subSetting.value as Bind
                                        settings.put(
                                            subSettingName, bind.buttonCode.toString() + ":" + bind.device
                                        )
                                    }

                                    else -> settings.put(subSettingName, subSetting.value)
                                }
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }

                json.put(it.name, settings)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

        try {
            // Create file writer instance
            val fileWriter = FileWriter(configFile)

            // Write with indentation factor of 4
            fileWriter.write(json.toString(4))

            // Flush and close
            fileWriter.flush()
            fileWriter.close()
        } catch (e: JSONException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        /* val saveConfigFolder = File("paragon${File.separator}configs${File.separator}$configName")

        // Create the folder if it doesn't exist
        if (!saveConfigFolder.exists()) {
            saveConfigFolder.mkdirs()
        }

        Paragon.INSTANCE.moduleManager.modules.forEach { module ->
            try {
                val jsonObject = JSONObject()

                jsonObject.put("enabled", module.isEnabled)

                if (module is HUDModule) {
                    jsonObject.put("x", module.x)
                    jsonObject.put("y", module.y)
                }

                for (setting in module.settings) {
                    when (setting.value) {
                        is Color -> {
                            val color = setting.value as Color
                            jsonObject.put(
                                setting.name,
                                color.red.toString() + ":" + color.green + ":" + color.blue + ":" + setting.alpha + ":" + setting.isRainbow + ":" + setting.rainbowSpeed + ":" + setting.rainbowSaturation + ":" + setting.isSync
                            )
                        }

                        is Bind -> {
                            val bind = setting.value as Bind
                            jsonObject.put(
                                setting.name,
                                bind.buttonCode.toString() + ":" + bind.device
                            )
                        }

                        else -> jsonObject.put(setting.name, setting.value)
                    }

                    if (setting.subsettings.isNotEmpty()) {
                        for (subSetting in setting.subsettings) {
                            val subSettingName = subSetting.parentSetting?.name + " " + subSetting.name
                            when (subSetting.value) {
                                is Color -> {
                                    val color = subSetting.value as Color
                                    jsonObject.put(
                                        subSettingName,
                                        color.red.toString() + ":" + color.green + ":" + color.blue + ":" + subSetting.alpha + ":" + subSetting.isRainbow + ":" + subSetting.rainbowSpeed + ":" + subSetting.rainbowSaturation + ":" + subSetting.isSync
                                    )
                                }

                                is Bind -> {
                                    val bind = subSetting.value as Bind
                                    jsonObject.put(
                                        subSettingName,
                                        bind.buttonCode.toString() + ":" + bind.device
                                    )
                                }

                                else -> jsonObject.put(subSettingName, subSetting.value)
                            }
                        }
                    }
                }

                // Write to file
                FileWriter(File(saveConfigFolder, module.name + ".json")).use {
                    it.write(jsonObject.toString(4))
                    it.flush()
                }
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
            }
        } */
    }

    @Suppress("UNCHECKED_CAST")
    fun loadModules(configName: String) {
        // Create configs folder if it doesn't already exist
        if (!configFolder.exists()) {
            configFolder.mkdirs()
        }

        val configFile = File("paragon${File.separator}configs${File.separator}$configName.json")

        val json = getJSON(configFile) ?: return

        Paragon.INSTANCE.moduleManager.modules.forEach {
            try {
                val settings = json.getJSONObject(it.name)

                if (settings.has("x") && settings.has("y")) {
                    it as HUDModule
                    it.x = settings.getInt("x").toFloat()
                    it.y = settings.getInt("y").toFloat()
                }

                fun loadSetting(setting: Setting<*>, isSub: Boolean) {
                    runCatching {
                        val settingName = if (isSub) setting.parentSetting?.name + " " + setting.name else setting.name

                        when (setting.value) {
                            is Boolean -> (setting as Setting<Boolean?>).setValue(settings.getBoolean(settingName))
                            is Bind -> {
                                val bind = setting.value as Bind
                                val parts = settings.getString(settingName).split(":".toRegex()).toTypedArray()

                                bind.buttonCode = parts[0].toInt()
                                bind.device = java.lang.Enum.valueOf(
                                    Device::class.java, parts[1]
                                )
                            }

                            is Float -> (setting as Setting<Float?>).setValue(settings.getFloat(settingName))
                            is Double -> (setting as Setting<Double?>).setValue(settings.getDouble(settingName))
                            is Enum<*> -> {
                                val enum = setting.value as Enum<*>
                                val value = java.lang.Enum.valueOf(enum::class.java, settings.getString(settingName))

                                run breakLoop@{
                                    enum::class.java.enumConstants.forEachIndexed { index, enumValue ->
                                        if (enumValue.name == value.name) {
                                            setting.index = index
                                            return@breakLoop
                                        }
                                    }
                                }

                                (setting as Setting<Enum<*>>).setValueRaw(value)
                            }

                            is Color -> {
                                val values = settings.getString(settingName).split(":".toRegex()).toTypedArray()

                                val color = Color(
                                    values[0].toInt() / 255f, values[1].toInt() / 255f, values[2].toInt() / 255f, values[3].toFloat() / 255f
                                )

                                setting.alpha = values[3].toFloat()
                                setting.isRainbow = java.lang.Boolean.parseBoolean(values[4])
                                setting.rainbowSpeed = values[5].toFloat()
                                setting.rainbowSaturation = values[6].toFloat()
                                setting.isSync = java.lang.Boolean.parseBoolean(values[7])
                                (setting as Setting<Color?>).setValue(color)
                            }
                        }
                    }
                }

                it.settings.forEach { setting ->
                    loadSetting(setting, false)

                    setting.subsettings.forEach { subSetting ->
                        loadSetting(subSetting, true)
                    }
                }

                if (settings.getBoolean("enabled") == !it.isEnabled) {
                    it.toggle()
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

        /* val loadFolder = File("paragon${File.separator}configs${File.separator}$configName")

        if (!loadFolder.exists()) {
            loadFolder.mkdirs()
            return
        }

        Paragon.INSTANCE.moduleManager.modules.forEach { module ->
            val moduleJSON = getJSON(File(loadFolder, module.name + ".json")) ?: return@forEach

            try {
                if (moduleJSON.has("x") && moduleJSON.has("y")) {
                    module as HUDModule
                    module.x = moduleJSON.getInt("x").toFloat()
                    module.y = moduleJSON.getInt("y").toFloat()
                }

                fun loadSetting(setting: Setting<*>, isSub: Boolean) {
                    runCatching {
                        val settingName = if (isSub) setting.parentSetting?.name + " " + setting.name else setting.name

                        when (setting.value) {
                            is Boolean -> (setting as Setting<Boolean?>).setValue(moduleJSON.getBoolean(settingName))
                            is Bind -> {
                                val bind = setting.value as Bind
                                val parts = moduleJSON.getString(settingName).split(":".toRegex()).toTypedArray()

                                bind.buttonCode = parts[0].toInt()
                                bind.device = java.lang.Enum.valueOf(
                                    Device::class.java,
                                    parts[1]
                                )
                            }

                            is Float -> (setting as Setting<Float?>).setValue(moduleJSON.getFloat(settingName))
                            is Double -> (setting as Setting<Double?>).setValue(moduleJSON.getDouble(settingName))
                            is Enum<*> -> {
                                val enum = setting.value as Enum<*>
                                val value = java.lang.Enum.valueOf(
                                    enum::class.java,
                                    moduleJSON.getString(settingName)
                                )

                                run breakLoop@{
                                    enum::class.java.enumConstants.forEachIndexed { index, enumValue ->
                                        if (enumValue.name == value.name) {
                                            setting.index = index
                                            return@breakLoop
                                        }
                                    }
                                }

                                (setting as Setting<Enum<*>>).setValue(value)
                            }

                            is Color -> {
                                val values = moduleJSON.getString(settingName).split(":".toRegex()).toTypedArray()

                                val color = Color(
                                    values[0].toInt() / 255f,
                                    values[1].toInt() / 255f,
                                    values[2].toInt() / 255f,
                                    values[3].toFloat() / 255f
                                )

                                setting.alpha = values[3].toFloat()
                                setting.isRainbow = java.lang.Boolean.parseBoolean(values[4])
                                setting.rainbowSpeed = values[5].toFloat()
                                setting.rainbowSaturation = values[6].toFloat()
                                setting.isSync = java.lang.Boolean.parseBoolean(values[7])
                                (setting as Setting<Color?>).setValue(color)
                            }
                        }
                    }
                }

                module.settings.forEach {
                    loadSetting(it, false)
                    it.subsettings.forEach { subSetting ->
                        loadSetting(subSetting, true)
                    }
                }

                if (moduleJSON.getBoolean("enabled") == !module.isEnabled) {
                    module.toggle()
                }
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
            }
        } */
    }

    /**
     * Saves the social interactions
     */
    fun saveSocial() {
        if (!socialFolder.exists()) {
            socialFolder.mkdir() //Create friends folder if it doesn't already exist
        }

        val file = File("paragon${File.separator}social${File.separator}social_interactions.json")
        file.createNewFile()

        FileWriter(file).use { writer ->
            val jsonObject = JSONObject()
            val array = JSONArray()

            // Put the UUIDs into the array
            Paragon.INSTANCE.friendManager.names.forEach {
                array.put(it)
            }

            // Add array to json object
            jsonObject.putOpt("uuids", array)

            writer.write(jsonObject.toString(4))
            writer.flush()
        }
    }

    /**
     * Loads the social interactions
     */
    fun loadSocial() {
        if (!socialFolder.exists()) {
            socialFolder.mkdirs() //Create friends folder if it doesn't already exist
            return //If there is no folder there won't be anything to load either
        }

        runCatching {
            val json = getJSON(File("paragon${File.separator}social${File.separator}social_interactions.json")) ?: return

            val array = json.getJSONArray("names")

            //For every value in array, create add a player to the SocialManager
            for (i in 0 until array.length()) {
                Paragon.INSTANCE.friendManager.addName(array.get(i).toString())
            }
        }
    }

    fun saveAlts() {
        // Create main folder if it doesn't already exist
        if (!mainFolder.exists()) {
            mainFolder.mkdirs()
        }

        val file = File("paragon${File.separator}alts.json")
        file.createNewFile()

        val jsonObject = JSONObject()

        FileWriter(file).use { writer ->
            val array = JSONArray()

            Paragon.INSTANCE.altManager.alts.forEach {
                array.put(it.email + ":" + it.password) //Put the player's info in the array - email:password
            }

            jsonObject.putOpt("alts", array)

            writer.write(jsonObject.toString(4))
            writer.flush()
        }
    }

    fun loadAlts() {
        if (!mainFolder.exists()) {
            mainFolder.mkdirs()
            return
        }

        runCatching {
            val json = getJSON(File("paragon${File.separator}alts.json")) ?: return
            val alts = json.getJSONArray("alts")

            //For every entry in the array, add a new alt
            for (i in 0 until alts.length()) {
                val info = alts[i].toString().split(":".toRegex()).toTypedArray()
                Paragon.INSTANCE.altManager.addAlt(Alt(info[0], info[1]))
            }
        }
    }

    fun saveOther() {
        if (!mainFolder.exists()) {
            mainFolder.mkdirs()
        }

        runCatching {
            val file = File("paragon${File.separator}client.json")
            file.createNewFile()

            FileWriter(file).use { writer ->
                val jsonObject = JSONObject()

                jsonObject.put("mainmenu", Paragon.INSTANCE.isParagonMainMenu)

                var prefixes = ""

                for (prefix in Paragon.INSTANCE.commandManager.commonPrefixes) {
                    prefixes += "$prefix "
                }

                jsonObject.put("ignored_prefixes", prefixes)

                writer.write(jsonObject.toString(4))
                writer.flush()
            }
        }
    }

    fun loadOther() {
        if (!mainFolder.exists()) {
            mainFolder.mkdirs()
            return
        }

        runCatching {
            val jsonObject = getJSON(File("paragon${File.separator}client.json")) ?: return

            Paragon.INSTANCE.isParagonMainMenu = jsonObject.getBoolean("mainmenu")

            if (jsonObject.has("ignored_prefixes")) {
                for (prefix in jsonObject.getString("ignored_prefixes").toString().split(" ".toRegex())) {
                    Paragon.INSTANCE.commandManager.commonPrefixes.add(prefix)
                }
            }
        }
    }

}