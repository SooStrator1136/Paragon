package com.paragon.client.systems.module.impl.combat

import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.Wrapper.mc
import com.paragon.api.util.anyNull
import com.paragon.api.util.combat.CrystalUtil.getCrystalDamage
import com.paragon.api.util.combat.CrystalUtil.getDamageToEntity
import com.paragon.api.util.entity.EntityUtil
import com.paragon.api.util.entity.EntityUtil.isEntityAllowed
import com.paragon.api.util.entity.EntityUtil.isTooFarAwayFromSelf
import com.paragon.api.util.render.RenderUtil
import com.paragon.api.util.system.backgroundThread
import com.paragon.api.util.world.BlockUtil
import com.paragon.api.util.world.BlockUtil.getBlockAtPos
import com.paragon.client.managers.rotation.Rotate
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.init.Blocks
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import java.awt.Color
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.min


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
    private val placeRange = Setting("Range", 10.0, 1.0, 15.0, 0.1) describedBy "The maximum range to place" subOf place
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
    private val explodeRange = Setting("Range", 5.0, 1.0, 7.0, 0.1) describedBy "The maximum range to explode crystals" subOf explode

    /*************************** ROTATE ***************************/

    private val rotate = Setting("Rotations", Rotate.PACKET) describedBy "How to rotate to the crystal or block"
    private val rotateTo = Setting("To", RotateTo.CRYSTAL) describedBy "The target to rotate to" subOf rotate

    /*************************** RENDERING **************************/

    private val render = Setting("Render", true) describedBy "Whether to render highlights"

    /*************************** MISC *******************************/

    private val threaded = Setting("Threaded", true) describedBy "Whether to search for positions and crystals in a seperate thread"

    private var targetCrystal: Crystal? = null
    private var placePosition: CrystalPosition? = null
    private var lastJob: Job? = null
    private var placeTimer = 0
    var lastTarget: EntityLivingBase? = null

    private var rotateTarget: Vec2f? = null

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
        if (!explode.value) {
            return null;
        }

        val crystalSet = TreeMap<Crystal, Float>()

        getTargetList().forEach { possibleTarget ->
            minecraft.world.loadedEntityList.stream().filter { !it.isTooFarAwayFromSelf(explodeRange.value) }.forEach { entityCrystal ->
                // just for smart casting
                if (entityCrystal is EntityEnderCrystal) {
                    val currentCrystal = Crystal(entityCrystal, entityCrystal.getDamageToEntity(possibleTarget!!))

                    if (crystalSet.any { it.key.crystal == entityCrystal }) {
                        val crystal = crystalSet.entries.stream().filter { it.key.crystal == entityCrystal }.findFirst().get()

                        if (currentCrystal.damage > crystal.key.damage) {
                            crystalSet[currentCrystal] = crystal.key.damage
                        }
                    }
                }
            }
        }

        if (crystalSet.isEmpty()) {
            return null
        }

        return crystalSet.lastKey()
    }

    private fun findPlacement(): CrystalPosition? {
        if (!place.value || getTargetList().isEmpty()) {
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

        return valid.lastEntry().value
    }

    private fun placeFoundPosition() {
        if (placePosition == null || placeTimer < placeDelay.value) {
            placeTimer++
            return
        }

        placeTimer = 0
    }

    private fun explodeFoundCrystal() {}

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

    fun canSeePos(pos: BlockPos): Boolean {
        return minecraft.world.rayTraceBlocks(Vec3d(minecraft.player.posX,minecraft.player.posY + minecraft.player.getEyeHeight().toDouble(), minecraft.player.posZ), Vec3d(pos.x + 0.5, (pos.y + 1).toDouble(), pos.z + 0.5), false, true, false) == null
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

    enum class RotateTo {
        /**
         * Rotate to the current crystal
         */
        CRYSTAL,

        /**
         * Rotate to the current block
         */
        BLOCK,

        /**
         * Rotate to the current block and crystal
         */
        BOTH
    }

    internal class Crystal(val crystal: EntityEnderCrystal, val damage: Float)
    internal class CrystalPosition(val position: BlockPos, val target: EntityLivingBase, val targetDamage: Float, val localDamage: Float)

}