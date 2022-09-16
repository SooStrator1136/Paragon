package com.paragon.impl.managers

import com.paragon.Paragon
import com.paragon.plugin.Plugin
import org.apache.commons.io.IOUtils
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.lang.reflect.Constructor
import java.net.URLClassLoader
import java.nio.charset.Charset
import java.util.*
import java.util.jar.JarEntry
import java.util.jar.JarFile


/**
 * @author Surge
 * @since 04/09/2022
 */
class PluginManager {

    private val plugins: ArrayList<Plugin> = arrayListOf()

    init {
        val pluginFolder = File("paragon/plugins")

        if (pluginFolder.isDirectory) {
            for (file in pluginFolder.listFiles()!!) {
                if (file.name.endsWith(".jar.disabled")) {
                    Paragon.INSTANCE.logger.info("Ignoring ${file.name}, plugin is disabled")
                    continue
                }

                if (!file.name.endsWith(".jar")) {
                    Paragon.INSTANCE.logger.error("${file.name} is not a valid plugin!")
                    continue
                }

                val jar = JarFile(file)

                val e: Enumeration<JarEntry> = jar.entries()

                while (e.hasMoreElements()) {
                    val je: JarEntry = e.nextElement()

                    if (je.name.equals("plugin.json")) {
                        val stream: InputStream = jar.getInputStream(je)

                        val json = JSONObject(IOUtils.toString(stream, Charset.defaultCharset()))

                        val loader: ClassLoader = URLClassLoader.newInstance(arrayOf(file.toURI().toURL()), javaClass.classLoader)

                        val clazz = Class.forName(json.getString("main-class"), true, loader)

                        val instanceClass = clazz.asSubclass(Plugin::class.java)
                        val instanceClassConstructor = instanceClass.getConstructor() as Constructor<out Plugin>
                        val plugin = instanceClassConstructor.newInstance()

                        plugin.initialise()
                        plugins.add(plugin)
                    }
                }
            }
        }
    }

    fun onLoad() {
        plugins.forEach {
            it.onLoad()

            it.modules.forEach { module -> module.reflectSettings() }
            Paragon.INSTANCE.moduleManager.modules.addAll(it.modules)

            Paragon.INSTANCE.commandManager.commands.addAll(it.commands)
        }
    }

    fun onPostLoad() {
        plugins.forEach {
            it.onPostLoad()
        }
    }

}