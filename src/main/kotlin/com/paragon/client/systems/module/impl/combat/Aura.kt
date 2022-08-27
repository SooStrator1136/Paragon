package com.paragon.client.systems.module.impl.combat

import com.paragon.Paragon
import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.anyNull
import com.paragon.api.util.calculations.Timer
import com.paragon.api.util.entity.EntityUtil.isEntityAllowed
import com.paragon.api.util.player.EntityFakePlayer
import com.paragon.api.util.player.InventoryUtil.getItemSlot
import com.paragon.api.util.player.InventoryUtil.isHoldingSword
import com.paragon.api.util.player.InventoryUtil.switchToSlot
import com.paragon.api.util.player.RotationUtil.getRotationToVec3d
import com.paragon.client.managers.rotation.Rotate
import com.paragon.client.managers.rotation.Rotation
import com.paragon.client.managers.rotation.RotationPriority
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.network.play.client.CPacketUseEntity
import net.minecraft.util.EnumHand
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.function.Function
import java.util.stream.Collectors

/**
 * Basic Aura module.
 *
 * @author Surge
 */
@SideOnly(Side.CLIENT)
object Aura : Module("Aura", Category.COMBAT, "Automatically attacks entities") {

    // How to sort the targets
    private val sort = Setting("Sort", Sort.DISTANCE, null, null, null) describedBy "How to sort the targets"

    // Filters
    private val players = Setting("Players", true, null, null, null) describedBy "Attack players"
    private val mobs = Setting("Mobs", true, null, null, null) describedBy "Attack mobs"
    private val passives = Setting("Passives", true, null, null, null) describedBy "Attack passives"

    // Main settings
    private val range = Setting("Range", 5f, 0f, 6f, 0.1f) describedBy "The range to attack"
    private val delay = Setting("Delay", 700.0, 0.0, 2000.0, 1.0) describedBy "The delay between attacking in milliseconds"
    private val performWhen = Setting("When", When.HOLDING, null, null, null) describedBy "When to attack"
    private val rotate = Setting("Rotate", Rotate.PACKET, null, null, null) describedBy "How to rotate to the target"
    private val rotateBack = Setting("RotateBack", true, null, null, null) describedBy "Rotate back to your original rotation" subOf rotate
    private val where = Setting("Where", Where.BODY, Where.BODY, Where.BODY, Where.BODY) describedBy "Where to attack"
    private val packetAttack = Setting("Packet", false, null, null, null) describedBy "Attack with a packet"

    var lastTarget: EntityLivingBase? = null
        private set

    private val attackTimer = Timer()
    private var target: EntityLivingBase? = null

    override fun onTick() {
        if (minecraft.anyNull) {
            return
        }

        target = null

        // Check the delay has passed
        if (attackTimer.hasMSPassed(delay.value)) {
            // Filter entities
            var entities: List<Entity> = minecraft.world.loadedEntityList.stream().filter { obj: Any? -> EntityLivingBase::class.java.isInstance(obj) }.collect(Collectors.toList())

            // Filter entities based on settings
            entities = entities.stream().filter { entity: Entity -> entity.getDistance(minecraft.player) <= range.value && entity !== minecraft.player && !entity.isDead && (entity.isEntityAllowed(players.value!!, mobs.value!!, passives.value!!) || entity is EntityFakePlayer) && (entity !is EntityPlayer || !Paragon.INSTANCE.socialManager.isFriend(entity.getName())) }.collect(Collectors.toList())

            // Sort entities
            entities = entities.sortedWith(Comparator.comparingDouble { entityLivingBase: Entity -> sort.value!!.getSort(entityLivingBase as EntityLivingBase).toDouble() })

            // Check we have targets
            if (entities.isNotEmpty()) {
                // Get the target
                val entityLivingBase = entities[0] as EntityLivingBase
                target = entityLivingBase
                lastTarget = target

                // Get our old slot
                val oldSlot: Int = minecraft.player.inventory.currentItem
                when (performWhen.value) {
                    When.SILENT_SWITCH, When.SWITCH -> {
                        if (!isHoldingSword) {
                            val swordSlot = getItemSlot(Items.DIAMOND_SWORD)

                            if (swordSlot > -1) {
                                switchToSlot(swordSlot, false)
                            }

                            else {
                                lastTarget = null
                                return
                            }
                        }

                        if (!isHoldingSword) {
                            lastTarget = null
                            return
                        }
                    }

                    When.HOLDING -> if (!isHoldingSword) {
                        lastTarget = null
                        return
                    }

                    else -> {}
                }

                // Get our original rotation
                val originalRotation = Vec2f(minecraft.player.rotationYaw, minecraft.player.rotationPitch)

                // Get our target rotation
                val rotationVec = getRotationToVec3d(Vec3d(entityLivingBase.posX, entityLivingBase.posY + where.value.getWhere(entityLivingBase), entityLivingBase.posZ))
                val rotation = Rotation(rotationVec.x, rotationVec.y, rotate.value!!, RotationPriority.HIGH)

                // Rotate to the target
                Paragon.INSTANCE.rotationManager.addRotation(rotation)

                // Attack the target
                if (packetAttack.value!!) {
                    minecraft.player.connection.sendPacket(CPacketUseEntity(entityLivingBase, EnumHand.MAIN_HAND))
                }
                else {
                    minecraft.playerController.attackEntity(minecraft.player, entityLivingBase)
                }

                // Swing hand
                minecraft.player.swingArm(EnumHand.MAIN_HAND)

                // Reset our cooldown
                minecraft.player.resetCooldown()

                // Rotate back to the original rotation
                if (rotateBack.value!! && rotate.value != Rotate.NONE) {
                    val rotationBack = Rotation(originalRotation.x, originalRotation.y, rotate.value!!, RotationPriority.NORMAL)
                    Paragon.INSTANCE.rotationManager.addRotation(rotationBack)
                }

                // Switch back to the old slot
                if (oldSlot != minecraft.player.inventory.currentItem && performWhen.value == When.SILENT_SWITCH) {
                    switchToSlot(oldSlot, false)
                }
            }
            else {
                lastTarget = null
            }
            attackTimer.reset()
        }
    }

    val isReady: Boolean
        get() {
            if (minecraft.anyNull) {
                return false
            }

            when (performWhen.value) {
                When.SILENT_SWITCH, When.SWITCH -> {
                    if (!isHoldingSword) {
                        val swordSlot = getItemSlot(Items.DIAMOND_SWORD)
                        return swordSlot > -1
                    }
                    if (isHoldingSword) {
                        return true
                    }
                }

                When.HOLDING -> if (isHoldingSword) {
                    return true
                }

                else -> {}
            }

            return false
        }

    override fun getData(): String {
        return if (target == null) "No target" else target!!.name
    }

    override fun isActive(): Boolean {
        return super.isActive() && target != null && isReady
    }

    enum class Sort(var function: Function<EntityLivingBase, Float>) {
        /**
         * Sort by distance
         */
        DISTANCE(Function<EntityLivingBase, Float> { e: EntityLivingBase -> Minecraft.getMinecraft().player.getDistance(e) }),

        /**
         * Sort by health
         */
        HEALTH(Function { obj: EntityLivingBase -> obj.health }),

        /**
         * Sort by armour
         */
        ARMOUR(Function { entityLivingBase: EntityLivingBase ->
            var totalArmourDamage = 0f
            for (itemStack in entityLivingBase.armorInventoryList) {
                totalArmourDamage += itemStack.itemDamage.toFloat()
            }
            totalArmourDamage
        });

        /**
         * Gets the function to sort by
         *
         * @return The function to sort by
         */
        fun getSort(entityLivingBase: EntityLivingBase): Float {
            return function.apply(entityLivingBase)
        }
    }

    enum class When {
        /**
         * Only attack when we are holding a sword
         */
        HOLDING,

        /**
         * Switch to a sword
         */
        SWITCH,

        /**
         * Silent switch to a sword
         */
        SILENT_SWITCH
    }

    enum class Where( // The function to get the added height
        var function: Function<EntityLivingBase, Float>
    ) {
        /**
         * Rotate to feet of target
         */
        FEET(Function { entityLivingBase: EntityLivingBase? -> 0f }),

        /**
         * Rotate to body of target
         */
        BODY(Function { entityLivingBase: EntityLivingBase -> entityLivingBase.width / 2f }),

        /**
         * Rotate to head of target
         */
        HEAD(Function { entityLivingBase: EntityLivingBase -> entityLivingBase.height });

        /**
         * Gets the height to add to the rotation
         *
         * @param entityLivingBase The entity to get the height for
         * @return The height to add to the rotation
         */
        fun getWhere(entityLivingBase: EntityLivingBase): Float {
            return function.apply(entityLivingBase)
        }
    }

}