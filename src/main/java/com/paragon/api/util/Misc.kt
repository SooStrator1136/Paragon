package com.paragon.api.util

import net.minecraft.client.Minecraft
import org.lwjgl.opengl.GL11

inline val mc: Minecraft get() = Minecraft.getMinecraft()

val Minecraft.anyNull get() = player == null || world == null

inline fun <E> Collection<E>.anyIndexed(predicate: (E) -> Boolean): Int {
    if (isEmpty()) return -1
    forEachIndexed { index, element ->
        if (predicate(element)) {
            return index
        }
    }
    return -1
}

/**
 * In java usage will look like this:
 * ```
 * public void exampleScaleTo(float x, float y, float z, double scaleFacX, double scaleFacY, double scaleFacZ) {
 *    scaleTo(x, y, z, scaleFacX, scaleFacY, scaleFacZ, unit -> {
 *       methodThatWillBeScaled();
 *       return Unit.INSTANCE;
 *    });
 * }
 * ```
 */
inline fun scaleTo(
    x: Float,
    y: Float,
    z: Float,
    scaleFacX: Double,
    scaleFacY: Double,
    scaleFacZ: Double,
    block: (Unit) -> Unit
) {
    GL11.glPushMatrix()
    GL11.glTranslatef(x, y, z)
    GL11.glScaled(scaleFacX, scaleFacY, scaleFacZ)
    GL11.glTranslatef(-x, -y, -z)
    block.invoke(Unit)
    GL11.glPopMatrix()
}

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
        else -> 0 //Shouldn't be reached since setting are supposed to me float or double
    }
}