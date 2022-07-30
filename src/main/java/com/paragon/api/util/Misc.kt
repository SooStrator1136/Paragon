package com.paragon.api.util

import net.minecraft.client.Minecraft

inline val mc: Minecraft get() = Minecraft.getMinecraft()

val Minecraft.anyNull get() = player == null || world == null

inline fun <E> Collection<E>.anyIndexed(predicate: (E) -> Boolean): Int {
    if (isEmpty()) return 0
    forEachIndexed { index, element ->
        if (predicate(element)) {
            return index
        }
    }
    return 0
}