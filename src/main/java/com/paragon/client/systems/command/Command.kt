package com.paragon.client.systems.command

/**
 * @author Surge
 */
abstract class Command(val name: String, val syntax: String) {

    abstract fun whenCalled(args: Array<String>, fromConsole: Boolean)

}