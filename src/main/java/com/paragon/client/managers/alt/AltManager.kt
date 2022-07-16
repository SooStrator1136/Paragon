package com.paragon.client.managers.alt

/**
 * @author SooStrator1136
 */
class AltManager {

    val alts = ArrayList<Alt>()

    fun addAlt(alt: Alt) {
        alts.add(alt)
    }

    fun removeAlt(alt: Alt?) {
        alts.remove(alt)
    }

}