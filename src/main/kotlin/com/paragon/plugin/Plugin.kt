package com.paragon.plugin

import com.paragon.impl.module.Module

/**
 * @author Surge
 * @since 04/09/2022
 */
abstract class Plugin {

    lateinit var name: String
    lateinit var authors: Array<String>

    val modules = arrayListOf<Module>()
    val commands = arrayListOf<com.paragon.impl.command.Command>()

    /**
     * Because funky kotlin!
     */
    abstract fun initialise()

    /**
     * Called when the plugin is loaded upon client initialisation
     *
     * Register modules and commands
     */
    abstract fun onLoad()

    /**
     * Called after the client is fully loaded
     */
    abstract fun onPostLoad()

}