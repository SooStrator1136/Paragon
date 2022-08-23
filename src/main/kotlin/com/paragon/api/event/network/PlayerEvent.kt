package com.paragon.api.event.network

/**
 * @author Surge
 */
open class PlayerEvent(val name: String) {

    class PlayerJoinEvent(name: String) : PlayerEvent(name)
    class PlayerLeaveEvent(name: String) : PlayerEvent(name)

}