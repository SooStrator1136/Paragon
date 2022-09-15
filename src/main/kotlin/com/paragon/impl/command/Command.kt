package com.paragon.impl.command

import com.paragon.util.Wrapper

/**
 * @author Surge
 */
abstract class Command(val name: String, val syntax: String) : Wrapper {

    abstract fun whenCalled(args: Array<String>, fromConsole: Boolean)

}