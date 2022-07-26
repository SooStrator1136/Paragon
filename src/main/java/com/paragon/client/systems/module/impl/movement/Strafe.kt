package com.paragon.client.systems.module.impl.movement

import com.paragon.api.event.player.PlayerMoveEvent
import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.Wrapper.mc
import com.paragon.api.util.calculations.Timer
import com.paragon.api.util.player.PlayerUtil
import com.paragon.asm.mixins.accessor.IMinecraft
import com.paragon.asm.mixins.accessor.ITimer
import me.wolfsurge.cerauno.listener.Listener
import net.minecraft.util.math.Vec3d
import kotlin.math.max

/**
 * @author Surge
 * @since 15/07/22
 */
object Strafe : Module("Strafe", Category.MOVEMENT, "Increases your movement speed") {

    private val timerSpeed = Setting("TimerSpeed", 1.09f, 1.01f, 2f, 0.01f)
        .setDescription("How fast the game's timer will tick at")

    private val airSpeed = Setting("AirSpeed", 1.05f, 1f, 2f, 0.01f)
        .setDescription("How fast the game's timer will tick at when you're in the air")

    private val speedFactor = Setting("SpeedFactor", 1.25f, 1f, 2f, 0.01f)
        .setDescription("How much to multiply the speed by")

    private val airFriction = Setting("AirFriction", 0.5f, 0.1f, 1f, 0.1f)
        .setDescription("How much friction to apply whilst in the air")

    private val delay = Setting("Delay", 250.0, 0.0, 1000.0, 1.0)
        .setDescription("How long to wait before increasing the speed")

    private var state: State = State.INCREASE
    private var speed: Double = 0.0
    private var timer: Timer = Timer()

    override fun onEnable() {
        state = State.SLOW
        speed = PlayerUtil.getBaseMoveSpeed()
    }

    override fun onDisable() {
        setTimerSpeed(1f)
    }

    @Listener
    fun onMove(event: PlayerMoveEvent) {
        // Don't apply speed if we're in a liquid or on a ladder
        if (!applySpeed()) {
            return
        }

        // Make sure we are moving
        if (PlayerUtil.isMoving() && !PlayerUtil.isInLiquid() && !mc.player.isOverWater) {
            // Check on ground state and whether the delay has passed
            if (mc.player.onGround && timer.hasMSPassed(delay.value)) {
                // Increase timer speed
                setTimerSpeed(timerSpeed.value)

                // Simulate a jump, but with 0.41 instead of 0.42 as it seems to bypass better?
                event.y = ((0.41f).toFloat().also { mc.player.motionY = it.toDouble() }).toDouble()

                // Set speed
                speed = PlayerUtil.getBaseMoveSpeed() * speedFactor.value.toDouble()

                // Set state
                state = State.SLOW

                // Reset timer
                timer.reset()
            }

            else {
                // Set timer speed to the air speed
                setTimerSpeed(airSpeed.value)

                // Check state or horizontal collision state
                if (state == State.SLOW || mc.player.collidedHorizontally) {
                    // Decrease speed
                    speed -= airFriction.value * PlayerUtil.getBaseMoveSpeed()

                    // Set state
                    state = State.INCREASE
                }
            }

            // Get the greatest possible speed
            speed = max(speed, PlayerUtil.getBaseMoveSpeed())

            // Get vector of forward position
            val forward: Vec3d = PlayerUtil.forward(speed)

            // Override X and Y
            event.x = forward.x
            event.z = forward.z
        }
    }

    private fun applySpeed(): Boolean = !minecraft.player.isInLava && !minecraft.player.isInWater && !minecraft.player.isOnLadder

    private fun setTimerSpeed(input: Float) {
        ((minecraft as IMinecraft).timer as ITimer).tickLength = 50f / input
    }

    enum class State {
        /**
         * Boost
         */
        INCREASE,

        /**
         * Slow down
         */
        SLOW
    }

}