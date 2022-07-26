package com.paragon.client.managers.rotation

import com.paragon.api.util.Wrapper
import com.paragon.client.managers.rotation.Rotate
import com.paragon.client.managers.rotation.RotationPriority
import net.minecraft.network.play.client.CPacketPlayer

/**
 * @author Surge
 * @since 23/03/22
 */
class Rotation(val yaw: Float, val pitch: Float, val rotate: Rotate, val priority: RotationPriority) : Wrapper