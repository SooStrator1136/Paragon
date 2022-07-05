package com.paragon.api.util

import net.minecraft.client.Minecraft

inline val mc: Minecraft get() = Minecraft.getMinecraft()

val Minecraft.anyNull get() = player == null || world == null
