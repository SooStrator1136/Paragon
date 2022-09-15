package com.paragon.client.systems.module.impl.misc

import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.anyNull
import com.paragon.api.util.calculations.Timer
import com.paragon.api.util.player.RotationUtil
import com.paragon.client.managers.rotation.Rotate
import net.minecraft.entity.projectile.EntityFireball
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.network.play.client.CPacketUseEntity
import net.minecraft.util.EnumHand

/**
 * @author SooStrator1136
 */
object AntiGhast : Module("AntiGhast", Category.MISC, "Keep yourself save (kys) from fireballs") {

    private val rotation = Setting("Rotation", Rotate.NONE)
    private val range = Setting(
        "Range",
        3.0,
        2.0,
        7.0,
        0.1
    ) describedBy "The range to attack Fireballs in"
    private val delay = Setting(
        "Delay",
        50F,
        50F,
        250F,
        10F
    ) describedBy "The minimum delay between hitting fireballs"
    private val hitType = Setting("HitType", AttackType.NORMAL)

    private val timer = Timer()

    override fun onTick() {
        if (!timer.hasMSPassed(delay.value.toDouble()) || minecraft.anyNull) {
            return
        }

        val fireBalls = minecraft.world.loadedEntityList.filterIsInstance<EntityFireball>().filter {
            it.getDistance(
                minecraft.player.posX,
                minecraft.player.posY + minecraft.player.getEyeHeight(),
                minecraft.player.posZ
            ) <= range.value
        }
        if (fireBalls.isEmpty()) {
            return
        }

        val target = fireBalls.sortedBy {
            it.getDistanceSq(
                minecraft.player.posX,
                minecraft.player.posY + minecraft.player.getEyeHeight(),
                minecraft.player.posZ
            )
        }[0]

        RotationUtil.rotate(RotationUtil.getRotationToVec3d(target.positionVector), rotation.value)
        if (hitType.value == AttackType.NORMAL) {
            minecraft.playerController.attackEntity(minecraft.player, target)
        } else {
            minecraft.connection?.sendPacket(CPacketUseEntity(target))
            minecraft.connection?.sendPacket(CPacketAnimation(EnumHand.MAIN_HAND))
        }
    }

    internal enum class AttackType {
        NORMAL, PACKET
    }

}