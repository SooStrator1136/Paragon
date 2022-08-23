package com.paragon.api.util.player

import com.paragon.api.util.Wrapper
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.world.World

class EntityFakePlayer(worldIn: World?) : EntityOtherPlayerMP(worldIn, Minecraft.getMinecraft().player.gameProfile), Wrapper {

    init {
        copyLocationAndAnglesFrom(minecraft.player)
        inventory.copyInventory(minecraft.player.inventory)
        minecraft.world.addEntityToWorld(-Int.MAX_VALUE, this)
    }

    fun despawn() {
        minecraft.world.removeEntity(minecraft.world.getEntityByID(-Int.MAX_VALUE))
    }
}