package com.paragon.client.systems.command

import com.paragon.api.util.Wrapper

/**
 * @author Surge
 */
abstract class Command(val name: String, val syntax: String) : Wrapper {

    abstract fun whenCalled(args: Array<String>, fromConsole: Boolean)

}