package com.paragon.client.systems.module.impl.combat

import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.anyNull
import com.paragon.api.util.combat.CrystalUtil.getCrystalDamage
import com.paragon.api.util.combat.CrystalUtil.getDamageToEntity
import com.paragon.api.util.entity.EntityUtil
import com.paragon.api.util.entity.EntityUtil.isEntityAllowed
import com.paragon.api.util.entity.EntityUtil.isTooFarAwayFromSelf
import com.paragon.api.util.player.InventoryUtil
import com.paragon.api.util.player.RotationUtil
import com.paragon.api.util.render.RenderUtil
import com.paragon.api.util.system.backgroundThread
import com.paragon.api.util.world.BlockUtil
import com.paragon.api.util.world.BlockUtil.getBlockAtPos
import com.paragon.asm.mixins.accessor.IPlayerControllerMP
import com.paragon.client.managers.rotation.Rotate
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import java.awt.Color
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.abs


/**
 * @author Surge
 * @since 10/08/2022
 */
object AutoCrystal : Module("AutoCrystal", Category.COMBAT, "Automatically places and explodes crystals") {

    /*************************** MISC ***************************/
    private val timing = Setting("Timing", Timing.SEQUENTIAL) describedBy "The timing of when to perform actions"
    private val order = Setting("Order", Order.NORMAL) describedBy "The order in which to perform actions"

    /*************************** TARGETING ***************************/

    private val targeting = Setting("Targeting", false) describedBy "How to find targets"
    private val targetRange = Setting("Range", 10.0, 1.0, 15.0, 0.1) describedBy "The maximum range to target" subOf targeting
    private val targetPlayers = Setting("Players", true) describedBy "Whether to target players" subOf targeting
    private val targetMobs = Setting("Mobs", true) describedBy "Whether to target mobs" subOf targeting
    private val targetPassives = Setting("Passives", true) describedBy "Whether to target passives" subOf targeting

    /*************************** PLACING ***************************/

    private val place = Setting("Place", true) describedBy "Whether to place crystals"
    private val placeCheck = Setting("Check", PlaceCheck.HOLDING) describedBy "How to check if you want to place crystals" subOf place
    private val placeRange = Setting("Range", 6.0, 1.0, 15.0, 0.1) describedBy "The maximum range to place" subOf place
    private val placeWallRange = Setting("WallRange", 10.0, 1.0, 15.0, 0.1) describedBy "The maximum range to place through walls" subOf place
    private val placeAntiSuicide = Setting("AntiSuicide", true) describedBy "Prevent killing yourself with crystals" subOf place
    private val placeMin = Setting("Minimum", 4.0, 0.0, 36.0, 1.0) describedBy "The minimum amount of crystal damage to do" subOf place
    private val placeMax = Setting("Local", 10.0, 0.0, 36.0, 1.0) describedBy "The maximum amount of crystal damage to do to yourself" subOf place
    private val placeDelay = Setting("Delay", 1.0, 0.0, 10.0, 1.0) describedBy "The delay between placing crystals (in ticks)" subOf place
    private val placeRaytrace = Setting("Raytrace", true) describedBy "Whether to raytrace check each possible place position" subOf place
    private val placeWait = Setting("Wait", true) describedBy "Wait until you have rotated to the place position" subOf place
    private val placePacket = Setting("Packet", true) describedBy "Whether to send packets to place crystals" subOf place

    /*************************** EXPLODING *************************/

    private val explode = Setting("Explode", true) describedBy "Whether to explode ender crystals"
    private val explodeRange = Setting("Range", 6.0, 1.0, 7.0, 0.1) describedBy "The maximum range to explode crystals" subOf explode

    /*************************** ROTATE ***************************/

    private val rotate = Setting("Rotations", Rotate.PACKET) describedBy "How to rotate to the crystal or block"
    private val maxYaw = Setting("MaxYaw", 45.0, 1.0, 180.0, 1.0) describedBy "The maximum yaw to change" subOf rotate

    /*************************** RENDERING **************************/

    private val render = Setting("Render", true) describedBy "Whether to render highlights"

    /*************************** MISC *******************************/

    private val threaded = Setting("Threaded", true) describedBy "Whether to search for positions and crystals in a seperate thread"

    private var targetCrystal: Crystal? = null
    private var placePosition: CrystalPosition? = null
    private var lastJob: Job? = null
    private var placeTimer = 0

    var lastTarget: EntityLivingBase? = null

    /**
     * 0 = Explode
     * 1 = Place
     */
    private var state = 0

    override fun onTick() {
        if (minecraft.anyNull) {
            return
        }

        if (threaded.value) {
            backgroundThread {
                if (lastJob == null || lastJob!!.isCompleted) {
                    lastJob = launch {
                        targetCrystal = findCrystal()
                        placePosition = findPlacement()
                    }
                }
            }
        } else {
            targetCrystal = findCrystal()
            placePosition = findPlacement()
        }

        placeFoundPosition()
        explodeFoundCrystal()
    }

    override fun onRender3D() {
        if (render.value) {
            if (targetCrystal != null) {
                RenderUtil.drawBoundingBox(EntityUtil.getEntityBox(targetCrystal!!.crystal), 1f, Color.RED)
            }
            
            if (placePosition != null) {
                RenderUtil.drawBoundingBox(BlockUtil.getBlockBox(placePosition!!.position), 1f, Color.GREEN)
            }
        }
    }

    private fun findCrystal(): Crystal? {
        if (!explode.value || timing.value == Timing.SEQUENTIAL && state != 0) {
            return null
        }

        val crystalMap = TreeMap<Crystal, Float>()

        getTargetList().forEach { possibleTarget ->
            minecraft.world.loadedEntityList.filter { it != null && !it.isTooFarAwayFromSelf(explodeRange.value) }.forEach { entityCrystal ->
                // just for smart casting
                if (entityCrystal is EntityEnderCrystal) {
                    val currentCrystal = Crystal(entityCrystal, entityCrystal.getDamageToEntity(possibleTarget!!), possibleTarget)

                    if (crystalMap.any { it.key.crystal == entityCrystal }) {
                        val crystal = crystalMap.entries.stream().filter { it.key.crystal == entityCrystal }.findFirst().get()

                        if (currentCrystal.damage > crystal.key.damage) {
                            crystalMap[currentCrystal] = crystal.key.damage
                        }
                    }
                }
            }
        }

        if (crystalMap.isEmpty()) {
            return null
        }

        lastTarget = crystalMap.lastEntry().key.target

        return crystalMap.lastKey()
    }

    private fun findPlacement(): CrystalPosition? {
        if (!place.value || getTargetList().isEmpty() || timing.value == Timing.SEQUENTIAL && state != 1) {
            return null
        }

        val valid = TreeMap<Float, CrystalPosition>()

        BlockUtil.getSphere(7f, true).forEach { blockPos ->
            if (canPlaceCrystal(blockPos)) {
                if (minecraft.player.positionVector.distanceTo(Vec3d(blockPos)) > placeRange.value && canSeePos(blockPos)) {
                    return@forEach
                }

                else if (minecraft.player.positionVector.distanceTo(Vec3d(blockPos)) > placeWallRange.value && !canSeePos(blockPos)) {
                    return@forEach
                }

                val localDamage = blockPos.getCrystalDamage(minecraft.player)

                if (localDamage > placeMax.value || localDamage > EntityUtil.getEntityHealth(minecraft.player) && placeAntiSuicide.value) {
                    return@forEach
                }

                if (placeRaytrace.value && !canSeePos(blockPos)) {
                    return@forEach
                }

                getTargetList().forEach targetList@ { entity ->
                    val targetDamage = blockPos.getCrystalDamage(entity!!)

                    if (targetDamage < placeMin.value) {
                        return@targetList
                    }

                    valid[targetDamage] = CrystalPosition(blockPos, entity, targetDamage, localDamage)
                }
            }
        }

        if (valid.isEmpty()) {
            return null
        }

        lastTarget = valid.lastEntry().value.target

        return valid.lastEntry().value
    }

    private fun placeFoundPosition() {
        if (placePosition == null || placeTimer < placeDelay.value) {
            placeTimer++
            return
        }

        val crystalSlot = InventoryUtil.getItemInHotbar(Items.END_CRYSTAL)
        var returnSlot = -1

        if (crystalSlot == -1) {
            return
        }

        if (placeCheck.value == PlaceCheck.HOLDING && !InventoryUtil.isHolding(Items.END_CRYSTAL)) {
            return
        } else if (placeCheck.value == PlaceCheck.SILENT || placeCheck.value == PlaceCheck.SWITCH) {
            minecraft.player.inventory.currentItem = crystalSlot
            (minecraft.playerController as IPlayerControllerMP).hookSyncCurrentPlayItem()

            if (placeCheck.value == PlaceCheck.SILENT) {
                returnSlot = crystalSlot
            }
        }

        if (rotate(RotationUtil.getRotationToBlockPos(placePosition!!.position, 0.5)) || !placeWait.value) {
            var hand = EnumHand.MAIN_HAND

            if (minecraft.player.heldItemOffhand.item == Items.END_CRYSTAL) {
                hand = EnumHand.OFF_HAND
            }

            if (placePacket.value) {
                minecraft.player.connection.sendPacket(CPacketPlayerTryUseItemOnBlock(placePosition!!.position, EnumFacing.getDirectionFromEntityLiving(placePosition!!.position, minecraft.player), hand, 0f, 0f, 0f))
            } else {
                minecraft.playerController.processRightClickBlock(minecraft.player, minecraft.world, placePosition!!.position, EnumFacing.getDirectionFromEntityLiving(placePosition!!.position, minecraft.player), Vec3d(0.0, 0.0, 0.0), hand)
            }

            state = 0
        }

        if (returnSlot != -1) {
            minecraft.player.inventory.currentItem = returnSlot
            (minecraft.playerController as IPlayerControllerMP).hookSyncCurrentPlayItem()
        }

        placeTimer = 0
    }

    private fun explodeFoundCrystal() {
        state = 1
    }

    private fun getTargetList(): CopyOnWriteArrayList<EntityLivingBase?> {
        val loaded = CopyOnWriteArrayList(minecraft.world.loadedEntityList)

        loaded.removeIf { it !is EntityLivingBase || it == minecraft.player || it.isDead || it.isTooFarAwayFromSelf(targetRange.value) || !it.isEntityAllowed(targetPlayers.value, targetMobs.value, targetPassives.value) }

        return loaded as CopyOnWriteArrayList<EntityLivingBase?>
    }

    private fun canPlaceCrystal(position: BlockPos): Boolean {
        if (position.getBlockAtPos() != Blocks.BEDROCK && position.getBlockAtPos() != Blocks.OBSIDIAN) {
            return false
        }

        val increase = position.up()

        if (increase.getBlockAtPos() != Blocks.AIR) {
            return false
        }

        minecraft.world.getEntitiesWithinAABB(Entity::class.java, AxisAlignedBB(increase.x.toDouble(), increase.y.toDouble(), increase.z.toDouble(), increase.x.toDouble() + 1, increase.y.toDouble() + 1, increase.z.toDouble() + 1)).forEach { entity ->
            if (entity.isDead) {
                return@forEach
            }

            if (entity is EntityEnderCrystal && entity.position.equals(increase)) {
                return@forEach
            }

            return false
        }

        return true
    }

    private fun canSeePos(pos: BlockPos): Boolean {
        return minecraft.world.rayTraceBlocks(Vec3d(minecraft.player.posX,minecraft.player.posY + minecraft.player.getEyeHeight().toDouble(), minecraft.player.posZ), Vec3d(pos.x + 0.5, (pos.y + 1).toDouble(), pos.z + 0.5), false, true, false) == null
    }

    private fun rotate(vec: Vec2f): Boolean {
        /* var calculatedAngle = vec.x - Paragon.INSTANCE.rotationManager.serverRotation.x

        if (calculatedAngle > 180) {
            calculatedAngle = RotationUtil.normalizeAngle(calculatedAngle)
        }

        return if (abs(calculatedAngle) > maxYaw.value) {
            calculatedAngle = RotationUtil.normalizeAngle(Paragon.INSTANCE.rotationManager.serverRotation.x + 55 * if (vec.x > 0) 1 else -1)

            when (rotate.value) {
                Rotate.PACKET -> {
                    minecraft.player.connection.sendPacket(CPacketPlayer.Rotation(calculatedAngle, vec.y, minecraft.player.onGround))
                }

                Rotate.LEGIT -> {
                    minecraft.player.connection.sendPacket(CPacketPlayer.Rotation(calculatedAngle, vec.y, minecraft.player.onGround))

                    minecraft.player.rotationYaw = calculatedAngle
                    minecraft.player.rotationYawHead = calculatedAngle
                    minecraft.player.rotationPitch = calculatedAngle
                }

                else -> {}
            }

            false
        } else {
            true
        } */

        val yaw = calculateAngle(minecraft.player.rotationYaw, vec.x)

        when (rotate.value) {
            Rotate.PACKET -> {
                minecraft.player.connection.sendPacket(CPacketPlayer.Rotation(yaw.first, vec.y, minecraft.player.onGround))
            }

            Rotate.LEGIT -> {
                minecraft.player.connection.sendPacket(CPacketPlayer.Rotation(yaw.first, vec.y, minecraft.player.onGround))

                minecraft.player.rotationYaw = yaw.first
                minecraft.player.rotationYawHead = yaw.first
                minecraft.player.rotationPitch = vec.y
            }

            else -> {}
        }

        if (yaw.second) {
            return true
        }

        return false
    }

    private fun calculateAngle(playerAngle: Float, wantedAngle: Float): Pair<Float, Boolean> {
        var calculatedAngle = wantedAngle - playerAngle

        if (abs(calculatedAngle) > 180) {
            calculatedAngle = RotationUtil.normalizeAngle(calculatedAngle)
        }

        var isFinished = false

        calculatedAngle = if (abs(calculatedAngle) > maxYaw.value) {
            RotationUtil.normalizeAngle((playerAngle + maxYaw.value * if (wantedAngle > 0) 1 else -1).toFloat())
        } else {
            isFinished = true

            wantedAngle
        }

        return Pair(calculatedAngle, isFinished)
    }

    enum class PlaceCheck {
        /**
         * Check we are holding
         */
        HOLDING,

        /**
         * Switch to crystal
         */
        SWITCH,

        /**
         * Silent switch to crystal
         */
        SILENT
    }

    enum class Order {
        /**
         * Place crystals then explode
         */
        NORMAL,

        /**
         * Explode then place crystals
         */
        ALTERNATE
    }

    enum class Timing {
        /**
         * Place and explode on the same tick
         */
        VANILLA,

        /**
         * Place on one tick and explode on the next
         */
        SEQUENTIAL
    }

    internal class Crystal(val crystal: EntityEnderCrystal, val damage: Float, val target: EntityLivingBase)
    internal class CrystalPosition(val position: BlockPos, val target: EntityLivingBase, val targetDamage: Float, val localDamage: Float)

}