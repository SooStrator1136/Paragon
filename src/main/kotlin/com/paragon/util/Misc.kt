package com.paragon.util

import net.minecraft.client.Minecraft

inline val mc: Minecraft get() = Minecraft.getMinecraft()

val Minecraft.anyNull get() = player == null || world == null

/**
 * Assumes that the both given numbers are the same primitive type, if that isn't the case inaccuracies will appear
 */
operator fun Number.minus(toSubtract: Number): Number {
    return when (this) {
        is Double -> this.toDouble().minus(toSubtract.toDouble())
        is Float -> this.toFloat().minus(toSubtract.toFloat())
        is Long -> this.toLong().minus(toSubtract.toLong())
        is Int -> this.toInt().minus(toSubtract.toInt())
        is Short -> this.toShort().minus(toSubtract.toShort())
        is Byte -> this.toByte().minus(toSubtract.toByte())
        else -> 0 //Shouldn't be reached
    }
}

/**
 * Assumes that the both given numbers are the same primitive type, if that isn't the case inaccuracies will appear
 */
operator fun Number.plus(toAdd: Number): Number {
    return when (this) {
        is Double -> this.toDouble().plus(toAdd.toDouble())
        is Float -> this.toFloat().plus(toAdd.toFloat())
        is Long -> this.toLong().plus(toAdd.toLong())
        is Int -> this.toInt().plus(toAdd.toInt())
        is Short -> this.toShort().plus(toAdd.toShort())
        is Byte -> this.toByte().plus(toAdd.toByte())
        else -> 0 //Shouldn't be reached
    }
}

/**
 * Converts a boolean to binary
 */
fun Boolean.toBinary(): Int {
    return if (this) 1 else 0
}

/**
 * Assumes that the both given numbers are the same primitive type, if that isn't the case inaccuracies will appear
 */
fun Number.isBetween(a: Number, b: Number): Boolean {
    return when (this) {
        is Double -> this > a.toDouble() && this < b.toDouble()
        is Float -> this > a.toFloat() && this < b.toFloat()
        is Long -> this > a.toLong() && this < b.toLong()
        is Int -> this > a.toInt() && this < b.toInt()
        is Short -> this > a.toShort() && this < b.toShort()
        is Byte -> this > a.toByte() && this < b.toByte()
        else -> false // Shouldn't be reached
    }
}

/**
 * Assumes that the both given numbers are the same primitive type, if that isn't the case inaccuracies will appear
 */
fun Number.difference(a: Number): Number {
    return when (this) {
        is Double -> if (this > a.toDouble()) this - a else a - this
        is Float -> if (this > a.toFloat()) this - a else a - this
        is Long -> if (this > a.toLong()) this - a else a - this
        is Int -> if (this > a.toInt()) this - a else a - this
        is Short -> if (this > a.toShort()) this - a else a - this
        is Byte -> if (this > a.toByte()) this - a else a - this
        else -> 0.0 // Shouldn't be reached
    }
}