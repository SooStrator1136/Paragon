package com.paragon.impl.module.combat

import com.paragon.Paragon
import com.paragon.impl.event.network.PacketEvent.PreReceive
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.util.player.PlayerUtil
import com.paragon.bus.listener.Listener
import com.paragon.impl.managers.rotation.Rotate
import com.paragon.impl.module.Category
import com.paragon.impl.module.misc.AutoEZ
import com.paragon.impl.setting.Bind
import com.paragon.mixins.accessor.IPlayerControllerMP
import com.paragon.util.anyNull
import com.paragon.util.calculations.Timer
import com.paragon.util.entity.EntityUtil.getEntityHealth
import com.paragon.util.entity.EntityUtil.isTooFarAwayFromSelf
import com.paragon.util.mc
import com.paragon.util.player.InventoryUtil.getHandHolding
import com.paragon.util.player.InventoryUtil.getItemInHotbar
import com.paragon.util.player.InventoryUtil.isHolding
import com.paragon.util.player.InventoryUtil.switchToSlot
import com.paragon.util.player.RotationUtil
import com.paragon.util.player.RotationUtil.getRotationToBlockPos
import com.paragon.util.player.RotationUtil.getRotationToVec3d
import com.paragon.util.render.RenderUtil.drawNametagText
import com.paragon.util.render.builder.BoxRenderMode
import com.paragon.util.render.builder.RenderBuilder
import com.paragon.util.world.BlockUtil
import com.paragon.util.world.BlockUtil.canSeePos
import com.paragon.util.world.BlockUtil.getBlockAtPos
import com.paragon.util.world.BlockUtil.getSphere
import net.minecraft.block.Block
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.init.MobEffects
import net.minecraft.init.SoundEvents
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.SPacketSoundEffect
import net.minecraft.util.*
import net.minecraft.util.math.*
import net.minecraft.world.Explosion
import java.awt.Color
import kotlin.math.abs
import kotlin.math.roundToInt


/**
 * @author Surge
 * @since 28/08/2022
 */
object AutoCrystal : Module("AutoCrystal", Category.COMBAT, "Automatically places and explodes crystals") {

    /******************************* MISC *******************************/

    private val order: Setting<Order> = Setting("Order", Order.PLACE_EXPLODE) describedBy "The order of operations to perform"
    private val timing: Setting<Timing> = Setting("Timing", Timing.LINEAR) describedBy "When to perform actions"
    private val heuristic: Setting<Heuristic> = Setting("Heuristic", Heuristic.MINIMAX) describedBy "The algorithm for calculating damage"

    /******************************* TARGETING *******************************/

    private val targeting = Setting("Targeting", true) describedBy "Settings for targeting players"
    private val targetPriority = Setting("Priority", TargetPriority.DISTANCE) describedBy "The way to sort possible targets" subOf (targeting)
    private val targetFriends = Setting("Friends", false) describedBy "Target friends" subOf (targeting)
    private val targetRange = Setting("Range", 10f, 1f, 15f, 0.1f) describedBy "The range to target players" subOf (targeting)

    /******************************* PLACING *******************************/
    private val place: Setting<Boolean> = Setting("Place", true) describedBy "Automatically place crystals"
    private val placeWhen = Setting("When", When.SILENT_SWITCH) describedBy "When to place crystals" subOf place
    private val placeRange = Setting("Range", 5f, 1f, 7f, 0.1f) describedBy "The range to place" subOf place
    private val placeMax = Setting("Limit", true) describedBy "Limit the amount of times we can attempt to place on a position" subOf place
    private val placeMaxAmount = Setting("Amount", 3.0, 1.0, 10.0, 1.0) describedBy "The amount of times we can attempt to place on a position" subOf place visibleWhen { placeMax.value }
    private val placeDelay = Setting("Delay", 10.0, 0.0, 500.0, 1.0) describedBy "The delay between placing crystals" subOf place
    private val placeRaytrace = Setting("Raytrace", true) describedBy "Checks if you can raytrace to the position" subOf place
    private val multiplace = Setting("Multiplace", false) describedBy "Place multiple crystals" subOf place
    private val placeMinDamage = Setting("MinDamage", 4f, 0f, 36f, 1f) describedBy "The minimum amount of damage to do to the target" subOf place
    private val placeMaxLocal = Setting("MaxLocal", 8f, 0f, 36f, 1f) describedBy "The minimum amount of damage to inflict upon yourself" subOf place
    private val placePacket = Setting("Packet", false) describedBy "Place with only a packet" subOf place
    private val placeSwing = Setting("Swing", Swing.MAIN_HAND) describedBy "Swing when placing a crystal" subOf place

    /******************************* EXPLODING *******************************/
    private val explode: Setting<Boolean> = Setting("Explode", true) describedBy "Automatically explode crystals"
    private val explodeRange = Setting("Range", 5f, 1f, 7f, 0.1f) describedBy "The range to explode crystals" subOf explode
    private val explodeDelay = Setting("Delay", 10.0, 0.0, 500.0, 1.0) describedBy "The delay between exploding crystals" subOf explode
    private val explodeFilter = Setting("Filter", ExplodeFilter.SMART) describedBy "What crystals to explode" subOf explode
    private val explodeMax = Setting("Limit", true) describedBy "Limit the amount of attacks on a crystal" subOf explode
    private val explodeLimitMax = Setting("LimitValue", 5f, 1f, 10f, 1f) describedBy "When to start ignoring the crystals" subOf explode visibleWhen { explodeMax.value }
    private val explodeTicksExisted = Setting("TicksExisted", 0.0, 0.0, 5.0, 1.0) describedBy "Check the amount of ticks the crystal has existed before exploding" subOf explode
    private val explodeRaytrace = Setting("Raytrace", false) describedBy "Checks that you can raytrace to the crystal" subOf explode
    private val antiWeakness = Setting("AntiWeakness", AntiWeakness.SWITCH) describedBy "If you have the weakness effect, you will still be able to explode crystals" subOf explode
    private val strictInventory = Setting("StrictInventory", true) describedBy "Fake opening your inventory when you switch" subOf explode visibleWhen { antiWeakness.value != AntiWeakness.OFF }
    private val packetExplode = Setting("Packet", false) describedBy "Explode crystals with a packet only" subOf explode
    private val explodeSwing = Setting("Swing", Swing.BOTH) describedBy "How to swing your hand" subOf explode
    private val explodeMinDamage = Setting("MinDamage", 4f, 0f, 36f, 1f) describedBy "The minimum amount of damage to do to the target" subOf explode visibleWhen { explodeFilter.value == ExplodeFilter.SMART || explodeFilter.value == ExplodeFilter.SELF_SMART }
    private val explodeMaxLocal = Setting("MaxLocal", 8f, 0f, 36f, 1f) subOf explode visibleWhen { explodeFilter.value == ExplodeFilter.SMART || explodeFilter.value == ExplodeFilter.SELF_SMART }
    private val explodeSync = Setting("Sync", SetDead.SOUND) describedBy "Sync crystal explosions" subOf explode

    /******************************* MISC *******************************/
    private val rotate = Setting("Rotate", Rotate.PACKET) describedBy "How to rotate"
    private val yawStep = Setting("YawStep", 45f, 1f, 180f, 1f) describedBy "The max yaw to step per tick" subOf rotate

    /******************************* OVERRIDING *******************************/
    private val overrideSetting = Setting("Override", true) describedBy "Override minimum damage when certain things happen"
    private val overrideHealth = Setting("Health", true) describedBy "Override if the target's health is below a value" subOf overrideSetting
    private val overrideHealthValue = Setting("OverrideHealth", 10f, 0f, 36f, 1f) describedBy "If the targets health is this value or below, ignore minimum damage" subOf overrideSetting visibleWhen (overrideSetting::value)
    private val overrideTotalArmour = Setting("Armour", true) describedBy "Override if the target's total armour durability is below a certain value" subOf overrideSetting
    private val overrideTotalArmourValue = Setting("ArmourValue", 10f, 0f, 100f, 1f) describedBy "The value which we will start to override at (in %)" subOf overrideSetting
    private val forceOverride: Setting<Bind> = Setting("ForceOverride", Bind(0, Bind.Device.KEYBOARD)) describedBy "Force override when you press a key" subOf overrideSetting
    private val ignoreMax = Setting("IgnoreMax", true) describedBy "Do not ignore the limits if we are overriding" subOf overrideSetting visibleWhen (explodeMax::value)
    private val ignoreMaxLocal = Setting("IgnoreMaxLocal", true) describedBy "Place or explode even if the damage done to us is larger than the max local damage" subOf overrideSetting

    /******************************* PAUSING *******************************/
    private val pause = Setting("Pause", true) describedBy "Pause if certain things are happening"
    private val pauseEating = Setting("Eating", true) describedBy "Pause when eating" subOf pause
    private val pauseDrinking = Setting("Drinking", true) describedBy "Pause when drinking" subOf pause
    private val pauseHealth = Setting("Health", true) describedBy "Pause when your health is below a specified value" subOf pause
    private val pauseHealthValue = Setting("HealthValue", 10f, 1f, 20f, 1f) subOf pause visibleWhen { pauseHealth.value }
    private val antiSuicide = Setting("AntiSuicide", true) describedBy "Does not explode / place the crystal if it will pop or kill you" subOf pause

    /******************************* RENDERING *******************************/
    private val render = Setting("Render", BoxRenderMode.BOTH) describedBy "Render the placement"
    private val renderOutlineWidth = Setting("OutlineWidth", 0.5f, 0.1f, 2f, 0.1f) describedBy "The width of the lines" subOf render
    private val renderColour = Setting("FillColour", Color(185, 19, 255, 130)) describedBy "The colour of the fill" subOf render
    private val renderOutlineColour = Setting("OutlineColour", Color(185, 19, 255)) subOf render
    private val renderDamageNametag = Setting("DamageNametag", true) describedBy "Render the damage nametag" subOf render

    // The current player we are targeting
    private var currentTarget: EntityPlayer? = null

    // The current crystal we are targeting
    private var currentCrystal: Crystal? = null

    // The current position we are placing at
    private var currentPlacement: CrystalPosition? = null

    // Exists purely to stop the flickering in the HUD info
    private var backlogPlacement: CrystalPosition? = null

    // Timers
    private val explodeTimer: Timer = Timer()
    private val placeTimer: Timer = Timer()

    // List of crystals we have placed
    private val selfPlacedCrystals: ArrayList<BlockPos> = ArrayList()

    // The current action we are performing
    private var currentActionState = ActionState.PLACING

    // Map of crystals we have attacked. Key is ID, Value is the amount of times we have attacked it
    private val explodeLimitMap: HashMap<Int, Int> = HashMap()

    // Map of block positions we have attempted to place on
    private val placeLimitMap: HashMap<BlockPos, Int> = HashMap()

    // Whether we are overriding or not
    private var overriding: Boolean = false

    override fun onTick() {
        if (minecraft.anyNull) {
            return
        }

        // Pause if we are supposed to
        if (pause.value && (pauseHealth.value && getEntityHealth(minecraft.player) <= pauseHealthValue.value || pauseEating.value && PlayerUtil.isPlayerEating || pauseDrinking.value && PlayerUtil.isPlayerDrinking)) {
            return
        }

        // Set our target
        currentTarget = getCurrentTarget()

        // Don't do anything if we don't have a target
        if (currentTarget == null) {
            reset()
            return
        }

        // Get overriding state
        // Called once because otherwise we do the same logic several times
        overriding = isOverriding(currentTarget!!)

        // Add target to AutoEZ list
        AutoEZ.addTarget(currentTarget!!.name)

        if (timing.value == Timing.LINEAR) {
            currentCrystal = findBestCrystal(overriding)
            currentPlacement = findBestPosition(overriding)
        }

        when (order.value) {
            Order.EXPLODE_PLACE -> {
                explodeSearchedCrystal()
                placeSearchedPosition()
            }

            Order.PLACE_EXPLODE -> {
                placeSearchedPosition()
                explodeSearchedCrystal()
            }
        }
    }

    override fun onRender3D() {
        if (currentPlacement != null && place.value) {
            RenderBuilder()
                .boundingBox(BlockUtil.getBlockBox(currentPlacement!!.position))
                .inner(renderColour.value)
                .outer(renderOutlineColour.value)
                .type(render.value)
                .start()
                .lineWidth(renderOutlineWidth.value)
                .blend(true)
                .depth(true)
                .texture(true)
                .build(false)

            // Render damage nametag
            if (renderDamageNametag.value) {
                drawNametagText("[" + currentPlacement!!.targetDamage.toInt() + ", " + currentPlacement!!.selfDamage.toInt() + "]", Vec3d(currentPlacement!!.position.x + 0.5, currentPlacement!!.position.y + 0.5, currentPlacement!!.position.z + 0.5), -1)
            }
        }
    }

    @Listener
    fun onPacketReceive(event: PreReceive) {
        // If we are trying to use and item on a block
        if (event.packet is CPacketPlayerTryUseItemOnBlock) {
            // Get packet
            val packet = event.packet

            // Check we are holding end crystals
            if (mc.player.getHeldItem(packet.hand).item.equals(Items.END_CRYSTAL)) {
                // If we can place a crystal on that block, add it to our self placed crystals list
                selfPlacedCrystals.add(packet.pos)
            }
        }

        // Check it's a sound packet
        if (event.packet is SPacketSoundEffect && explodeSync.value == SetDead.SOUND) {
            // Get packet
            val packet = event.packet

            // Check it's an explosion sound
            if (packet.sound == SoundEvents.ENTITY_GENERIC_EXPLODE && packet.category == SoundCategory.BLOCKS) {
                // Iterate through loaded entities
                for (entity in mc.world.loadedEntityList) {
                    // If the entity isn't an ender crystal, or it is dead, ignore
                    if (entity !is EntityEnderCrystal || entity.isDead) {
                        continue
                    }

                    // If the crystal is close to the explosion sound origin, set the crystals state to dead
                    if (entity.getDistance(packet.x, packet.y, packet.z) <= 6) {
                        entity.setDead()
                    }
                }
            }
        }
    }

    /**
     * Gets the best player to target
     * @return The best player to target
     */
    private fun getCurrentTarget(): EntityPlayer? {
        // All valid targets
        val validTargets: MutableList<EntityPlayer> = ArrayList()

        // Iterate through loaded entities
        for (entity in mc.world.loadedEntityList) {
            // Check it's a player that isn't us
            if (entity is EntityOtherPlayerMP) {
                // Get player
                val entityPlayer = entity as EntityPlayer

                // Make sure the player is a valid target
                if (entityPlayer.isDead || entityPlayer.health <= 0 || entityPlayer.isTooFarAwayFromSelf(targetRange.value.toDouble())) {
                    continue
                }

                // If it's a friend, and we don't want to target friends, ignore
                if (!targetFriends.value) {
                    if (Paragon.INSTANCE.friendManager.isFriend(entityPlayer.name)) {
                        continue
                    }
                }

                // Add to valid targets list
                validTargets.add(entityPlayer)
            }
        }

        // Return null if there are no valid targets
        if (validTargets.isEmpty()) {
            return null
        }

        when (targetPriority.value) {
            TargetPriority.DISTANCE -> validTargets.sortWith(Comparator.comparingDouble { target -> minecraft.player.getDistance(target).toDouble() })
            TargetPriority.HEALTH -> validTargets.sortWith(Comparator.comparingDouble { obj: EntityLivingBase -> obj.health.toDouble() })
            TargetPriority.ARMOUR -> validTargets.sortWith(Comparator.comparingDouble { target ->
                var totalArmour = 0f

                // Iterate through target's armour slots
                for (armour in target.armorInventoryList) {
                    // Don't do anything if they don't have an item in the slot
                    if (armour.isEmpty) {
                        continue
                    }

                    // Add item damage to total
                    totalArmour += armour.itemDamage.toFloat()
                }

                totalArmour.toDouble()
            })
        }

        return validTargets[0]
    }

    /**
     * Explodes the searched crystal
     */
    private fun explodeSearchedCrystal() {
        if (timing.value == Timing.SEQUENTIAL) {
            currentCrystal = findBestCrystal(overriding)
        }

        // Check we want to explode
        if (explode.value && currentCrystal != null && (currentActionState == ActionState.EXPLODING || timing.value != Timing.SEQUENTIAL)) {
            // Check we want to explode a crystal
            if (!explodeTimer.hasMSPassed(explodeDelay.value) || currentCrystal!!.selfDamage > getEntityHealth(mc.player) && antiSuicide.value) {
                return
            }

            // Get our current slot so we can switch back
            val antiWeaknessSlot = mc.player.inventory.currentItem

            // Check we want to apply anti weakness
            if (antiWeakness.value != AntiWeakness.OFF && mc.player.isPotionActive(MobEffects.WEAKNESS)) {
                // If we want to fake opening our inventory, send the opening inventory packet
                if (strictInventory.value) {
                    mc.player.connection.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.OPEN_INVENTORY))
                }

                // Get the best sword
                val hotbarSwordSlot = getItemInHotbar(Items.DIAMOND_SWORD)

                // If we have found a sword, switch to it
                if (hotbarSwordSlot != -1) {
                    switchToSlot(hotbarSwordSlot, antiWeakness.value == AntiWeakness.SILENT)
                }
            }

            // Get rotation
            val rotationVec = getRotationToVec3d(Vec3d(currentCrystal!!.crystal.posX, currentCrystal!!.crystal.posY, currentCrystal!!.crystal.posZ))

            if (rotate(rotationVec)) {
                if (packetExplode.value) {
                    // Explode with a packet
                    mc.player.connection.sendPacket(CPacketUseEntity(currentCrystal!!.crystal))
                }
                else {
                    // Attack crystal
                    mc.playerController.attackEntity(mc.player, currentCrystal!!.crystal)
                }

                // If we want to set the crystal to dead as soon as we attack, do that
                if (explodeSync.value == SetDead.ATTACK) {
                    currentCrystal!!.crystal.setDead()
                }

                // Remove it from our self placed crystals
                selfPlacedCrystals.remove(currentCrystal!!.crystal.position.down())

                // Swing our arm
                swing(explodeSwing.value)

                // Check we want to switch
                if (antiWeakness.value != AntiWeakness.OFF && mc.player.isPotionActive(MobEffects.WEAKNESS)) {
                    // Fake opening inventory
                    if (strictInventory.value) {
                        mc.player.connection.sendPacket(CPacketCloseWindow(mc.player.inventoryContainer.windowId))
                    }

                    // Switch to slot
                    if (antiWeaknessSlot != -1) {
                        switchToSlot(antiWeaknessSlot, antiWeakness.value == AntiWeakness.SILENT)
                    }
                }

                explodeTimer.reset()

                currentActionState = ActionState.PLACING
            }
        }
    }

    /**
     * Places a crystal at the searched position
     */
    private fun placeSearchedPosition() {
        if (timing.value == Timing.SEQUENTIAL) {
            currentPlacement = findBestPosition(overriding)
        }

        // Check we want to place a crystal
        if (currentPlacement != null && !placeTimer.hasMSPassed(placeDelay.value) && currentPlacement!!.selfDamage > getEntityHealth(mc.player) && antiSuicide.value && (currentActionState == ActionState.EXPLODING || timing.value != Timing.SEQUENTIAL)) {
            return
        }

        val oldSlot = mc.player.inventory.currentItem

        val hasSwitched = when (placeWhen.value) {
            When.HOLDING -> isHolding(Items.END_CRYSTAL)
            When.SWITCH, When.SILENT_SWITCH -> {
                val silentCrystalSlot = getItemInHotbar(Items.END_CRYSTAL)

                if (silentCrystalSlot == -1) {
                    false
                }
                else {
                    switchToSlot(silentCrystalSlot, false)
                    true
                }
            }
        }

        // We haven't switched, don't place
        // Also null checking currentPlacement because funny crash
        if (!hasSwitched || currentPlacement == null) {
            return
        }

        // Get rotation
        val placeRotation = getRotationToBlockPos(currentPlacement!!.position, 0.5)

        // Check we want to rotate
        if (rotate(placeRotation)) {
            val placeHand = getHandHolding(Items.END_CRYSTAL)

            // Let's call this, it fixes the packet place bug, and it shouldn't do anything bad afaik.
            (mc.playerController as IPlayerControllerMP).hookSyncCurrentPlayItem()

            if (placePacket.value && placeHand != null) {
                // Send packet
                mc.player.connection.sendPacket(CPacketPlayerTryUseItemOnBlock(currentPlacement!!.position, currentPlacement!!.facing, if (mc.player.heldItemOffhand.item.equals(Items.END_CRYSTAL)) EnumHand.OFF_HAND else placeHand, currentPlacement!!.facingVec.x.toFloat(), currentPlacement!!.facingVec.y.toFloat(), currentPlacement!!.facingVec.z.toFloat()))

                // Swing arm
                swing(placeSwing.value)
            }
            else if (placeHand != null) {
                // Place crystal
                if (mc.playerController.processRightClickBlock(mc.player, mc.world, currentPlacement!!.position, currentPlacement!!.facing, Vec3d(currentPlacement!!.facing.directionVec), if (mc.player.heldItemOffhand.item.equals(Items.END_CRYSTAL)) EnumHand.OFF_HAND else placeHand).equals(EnumActionResult.SUCCESS)) {
                    // Swing arm
                    swing(placeSwing.value)
                }
            }

            // Add position to our self placed crystals
            selfPlacedCrystals.add(currentPlacement!!.position)

            if (placeWhen.value == When.SILENT_SWITCH) {
                switchToSlot(oldSlot, false)
            }

            placeTimer.reset()

            currentActionState = ActionState.EXPLODING;
        }
    }

    /**
     * Finds the best crystal to attack
     * @return The best crystal to attack
     */
    private fun findBestCrystal(overriding: Boolean): Crystal? {
        // The best crystal (we will return this)
        var crystal: Crystal? = null

        // Check we want to explode
        if (explode.value) {
            // Iterate through loaded entities
            for (entity in mc.world.loadedEntityList) {
                // Check the entity is a crystal
                if (entity is EntityEnderCrystal && !entity.isDead) {
                    // Check the crystal is valid
                    if (entity.ticksExisted < explodeTicksExisted.value.toInt() || entity.isTooFarAwayFromSelf(explodeRange.value.toDouble()) || explodeRaytrace.value && !mc.player.canEntityBeSeen(entity)) {
                        continue
                    }

                    // We have already tried to explode this crystal
                    if (explodeMax.value && explodeLimitMap.containsKey(entity.getEntityId()) && explodeLimitMap[entity.getEntityId()]!!.toFloat() > explodeLimitMax.value.toInt()) {
                        if (!overriding && !ignoreMax.value) {
                            continue
                        }
                    }
                    else {
                        explodeLimitMap[entity.getEntityId()] = explodeLimitMap.getOrDefault(entity.getEntityId(), 0) + 1
                    }

                    // Get the crystals position as a vector
                    val vec = Vec3d(entity.posX, entity.posY, entity.posZ)

                    // Crystal
                    val calculatedCrystal = Crystal(entity, calculateDamage(vec, currentTarget!!), calculateDamage(vec, mc.player))

                    // Position of crystal
                    val (position) = CrystalPosition(calculatedCrystal.crystal.position, EnumFacing.UP, Vec3d(0.0, 0.0, 0.0), calculatedCrystal.targetDamage, calculatedCrystal.selfDamage)

                    when (explodeFilter.value) {
                        ExplodeFilter.SELF -> if (!selfPlacedCrystals.contains(position)) {
                            continue
                        }

                        ExplodeFilter.SELF_SMART -> {
                            // Check it's in our self placed crystals
                            if (!selfPlacedCrystals.contains(position)) {
                                continue
                            }

                            // Check it meets our max local requirement
                            if (calculatedCrystal.selfDamage > explodeMaxLocal.value) {
                                if (!(overriding && ignoreMaxLocal.value)) {
                                    continue
                                }
                            }

                            // Check it meets our minimum damage requirement
                            if (calculatedCrystal.targetDamage < explodeMinDamage.value && !overriding) {
                                continue
                            }
                        }

                        ExplodeFilter.SMART -> {
                            // Check it meets our max local requirement
                            if (calculatedCrystal.selfDamage > explodeMaxLocal.value) {
                                if (!(overriding && ignoreMaxLocal.value)) {
                                    continue
                                }
                            }

                            // Check it meets our minimum damage requirement
                            if (calculatedCrystal.targetDamage < explodeMinDamage.value && !overriding) {
                                continue
                            }
                        }

                        else -> {}
                    }

                    // Set the crystal to this if: the current best crystal is null, or this crystal's target damage is higher than the last crystal checked
                    if (crystal == null || calculateHeuristic(calculatedCrystal.selfDamage, calculatedCrystal.targetDamage, mc.player.getDistance(entity), heuristic.value) > calculateHeuristic(crystal.selfDamage, crystal.targetDamage, mc.player.getDistance(entity), heuristic.value)) {
                        crystal = calculatedCrystal
                    }
                }
            }
        }
        return crystal
    }

    /**
     * Finds the best position to place at
     * @return The best position to place at
     */
    private fun findBestPosition(overriding: Boolean): CrystalPosition? {
        val crystalPositions: MutableList<CrystalPosition> = ArrayList()

        // Check we want to place
        if (place.value) {
            // Iterate through blocks around us
            for (pos in getSphere(placeRange.value, true)) {
                // Check we can place crystals on this block
                if (!canPlaceCrystal(pos)) {
                    continue
                }

                if (placeMax.value && placeLimitMap.containsKey(pos) && placeLimitMap[pos]!! >= placeMaxAmount.value) {
                    if (!overriding && !ignoreMax.value) {
                        continue
                    }
                }
                else {
                    placeLimitMap.put(pos, (placeLimitMap[pos] ?: 0) + 1)
                }

                // Position we are placing at
                val placeVec = Vec3d(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5)

                // Position we will calculate damage at
                val damageVec = Vec3d(pos.x + 0.5, (pos.y + 1).toDouble(), pos.z + 0.5)

                // Get the direction we want to face
                var facing = EnumFacing.getDirectionFromEntityLiving(pos, mc.player)
                var facingVec: Vec3d? = null
                val rayTraceResult = mc.world.rayTraceBlocks(mc.player.getPositionEyes(1f), mc.player.getPositionEyes(1f).add(Vec3d(placeVec.x * placeRange.value, placeVec.y * placeRange.value, placeVec.z * placeRange.value)), false, false, true)
                val middleResult = mc.world.rayTraceBlocks(mc.player.getPositionEyes(1f), Vec3d(pos).add(Vec3d(0.5, 0.5, 0.5)))

                // Check we hit a block
                if (middleResult != null && middleResult.typeOfHit == RayTraceResult.Type.BLOCK) {
                    facing = middleResult.sideHit

                    // We can place if we are at max height by doing this
                    if (pos.y >= mc.world.actualHeight - 1) {
                        facing = EnumFacing.DOWN
                    }
                }

                // Get angles
                if (rayTraceResult?.hitVec != null) {
                    facingVec = Vec3d(rayTraceResult.hitVec.x - pos.x, rayTraceResult.hitVec.y - pos.y, rayTraceResult.hitVec.z - pos.z)
                }

                // Create new crystal position
                val crystalPosition = CrystalPosition(pos, facing, facingVec!!, calculateDamage(damageVec, currentTarget!!), calculateDamage(damageVec, mc.player))

                // Check it's below or equal to our maximum local damage requirement
                if (crystalPosition.selfDamage > placeMaxLocal.value && !(overriding && ignoreMaxLocal.value)) {
                    continue
                }

                // Check it's above or equal to our minimum damage requirement
                if (!overriding && crystalPosition.targetDamage < placeMinDamage.value) {
                    continue
                }
                crystalPositions.add(crystalPosition)
            }
        }

        crystalPositions.sortWith(Comparator.comparingDouble { position -> calculateHeuristic(position, heuristic.value).toDouble() })

        crystalPositions.reverse()

        if (crystalPositions.isNotEmpty()) {
            backlogPlacement = crystalPositions[0]
            return crystalPositions[0]
        }

        return null
    }

    private fun isOverriding(target: EntityPlayer): Boolean {
        if (overrideSetting.value) {
            if (overrideHealth.value && getEntityHealth(target) <= overrideHealthValue.value || forceOverride.value.isPressed()) {
                return true
            }

            if (overrideTotalArmour.value) {
                var lowest = 100f

                // Iterate through target's armour
                for (armourPiece in target.armorInventoryList) {
                    // If it is an actual piece of armour
                    if (armourPiece != null && armourPiece.item !== Items.AIR) {
                        // Get durability
                        val durability = (armourPiece.maxDamage - armourPiece.itemDamage) / armourPiece.maxDamage.toFloat() * 100

                        // If it is less than the last lowest, set the lowest to this durability
                        if (durability < lowest) {
                            lowest = durability
                        }
                    }
                }

                // We are overriding if the lowest durability is less or equal to the total armour value setting
                return lowest <= overrideTotalArmourValue.value
            }
        }

        return false
    }

    private fun rotate(vec: Vec2f): Boolean {
        // We use the server rotation as it lets us place when using packet rotate
        val yaw = calculateAngle(Paragon.INSTANCE.rotationManager.serverRotation.x, vec.x)

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

        calculatedAngle = if (abs(calculatedAngle) > yawStep.value) {
            RotationUtil.normalizeAngle((playerAngle + yawStep.value * if (wantedAngle > 0) 1 else -1).toFloat())
        }
        else {
            isFinished = true
            wantedAngle
        }

        return Pair(calculatedAngle, isFinished)
    }

    /**
     * Swings our hands
     * @param swing The hand to swing
     */
    private fun swing(swing: Swing) {
        when (swing) {
            Swing.MAIN_HAND -> mc.player.swingArm(EnumHand.MAIN_HAND)
            Swing.OFFHAND -> mc.player.swingArm(EnumHand.OFF_HAND)

            Swing.BOTH -> {
                mc.player.swingArm(EnumHand.MAIN_HAND)
                mc.player.swingArm(EnumHand.OFF_HAND)
            }

            Swing.PACKET -> mc.player.connection.sendPacket(CPacketAnimation(EnumHand.MAIN_HAND))

            else -> {}
        }
    }

    /**
     * Checks if we can place a crystal on a block
     * @param pos The pos to check
     * @return Whether we can place a crystal on that block or not
     */
    private fun canPlaceCrystal(pos: BlockPos): Boolean {
        // Get block
        val block: Block = pos.getBlockAtPos()

        // Check position is valid
        if (!(block == Blocks.OBSIDIAN || block == Blocks.BEDROCK) || !mc.world.isAirBlock(pos.up()) || !mc.world.getBlockState(pos.up(2)).material.isReplaceable || mc.player.position.equals(pos) || placeRaytrace.value && !canSeePos(pos)) {
            return false
        }

        // Iterate through entities in the block above
        for (entity in mc.world.getEntitiesWithinAABB(Entity::class.java, AxisAlignedBB(pos.up()))) {
            // If the entity is dead, or we aren't multiplacing, continue
            if (entity.isDead || !multiplace.value && entity is EntityEnderCrystal) {
                continue
            }

            return false
        }
        return true
    }

    /**
     * Calculates the heuristic
     * @param self The damage done to us
     * @param target The damage done to the target
     * @param distance The distance from the crystal
     * @param heuristic The heuristic type
     * @return The damage heuristic
     */
    private fun calculateHeuristic(self: Float, target: Float, distance: Float, heuristic: Heuristic?): Float {
        when (heuristic) {
            Heuristic.DAMAGE -> return target
            Heuristic.MINIMAX -> return target - self
            Heuristic.UNIFORM -> return target - self - distance

            else -> {}
        }

        return target
    }

    /**
     * Calculates the heuristic based on a crystal position
     * @param crystal The crystal to calculate heuristic for
     * @param heuristic The heuristic type
     * @return The damage heuristic
     */
    private fun calculateHeuristic(crystal: CrystalPosition?, heuristic: Heuristic?): Float {
        return if (crystal == null) {
            0f
        }
        else {
            calculateHeuristic(crystal.selfDamage, crystal.targetDamage, mc.player.getDistanceSq(crystal.position).toFloat(), heuristic)
        }
    }

    /**
     * Calculates the explosion damage based on a Vec3D
     * @param vec The vector to calculate damage from
     * @param entity The target
     * @return The damage done to the target
     */
    private fun calculateDamage(vec: Vec3d, entity: EntityLivingBase): Float {
        var finalDamage = 0.0f
        try {
            val doubleExplosionSize = 12.0f
            val distancedSize = entity.getDistance(vec.x, vec.y, vec.z) / doubleExplosionSize.toDouble()
            val blockDensity = entity.world.getBlockDensity(Vec3d(vec.x, vec.y, vec.z), entity.entityBoundingBox).toDouble()
            val v = (1.0 - distancedSize) * blockDensity
            val damage = ((v * v + v) / 2.0 * 7.0 * doubleExplosionSize.toDouble() + 1.0).toInt().toFloat()
            val diff = mc.world.difficulty.difficultyId

            finalDamage = getBlastReduction(entity, damage * if (diff == 0) 0f else if (diff == 2) 1f else if (diff == 1) 0.5f else 1.5f, Explosion(mc.world, null, vec.x, vec.y, vec.z, 6f, false, true))
        } catch (ignored: NullPointerException) {

        }

        return finalDamage
    }

    /**
     * Gets the blast reduction
     *
     * @param entity The entity to calculate damage for
     * @param damage The original damage
     * @param explosion The explosion
     * @return The blast reduction
     */
    private fun getBlastReduction(entity: EntityLivingBase, damage: Float, explosion: Explosion?): Float {
        var damage = damage

        if (entity is EntityPlayer) {
            val ds = DamageSource.causeExplosionDamage(explosion)
            damage = CombatRules.getDamageAfterAbsorb(damage, entity.totalArmorValue.toFloat(), entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).attributeValue.toFloat())

            val k = EnchantmentHelper.getEnchantmentModifierDamage(entity.armorInventoryList, ds)
            val f = MathHelper.clamp(k.toFloat(), 0.0f, 20.0f)
            damage *= 1.0f - f / 25.0f

            if (entity.isPotionActive(MobEffects.WEAKNESS)) {
                damage -= damage / 4
            }

            damage = damage.coerceAtLeast(0.0f)
            return damage
        }

        damage = CombatRules.getDamageAfterAbsorb(
            damage, entity.totalArmorValue.toFloat(), entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).attributeValue.toFloat()
        )
        return damage
    }

    /**
     * Resets the process
     */
    fun reset() {
        currentTarget = null
        currentCrystal = null
        currentPlacement = null
        backlogPlacement = null
        selfPlacedCrystals.clear()
        explodeLimitMap.clear()
    }

    override fun getData(): String {
        return if (currentTarget == null) "No Target" else currentTarget!!.name + " DMG " + (if (!isOverriding(currentTarget!!)) "" else "[OVERRIDING] ") + if (backlogPlacement == null) "No Placement" else calculateHeuristic(backlogPlacement, heuristic.value).roundToInt()
    }

    enum class Order {
        /**
         * Place then explode
         */
        PLACE_EXPLODE,

        /**
         * Explode then place
         */
        EXPLODE_PLACE
    }

    enum class Timing {
        /**
         * Run actions one after another
         */
        LINEAR,

        /**
         * Run actions on different ticks
         */
        SEQUENTIAL
    }

    enum class ActionState {
        /**
         * About to explode crystals
         */
        EXPLODING,

        /**
         * About to place crystals
         */
        PLACING
    }

    enum class Heuristic {
        /**
         * Just target damage
         */
        DAMAGE,

        /**
         * Target damage minus self damage
         */
        MINIMAX,

        /**
         * Target damage minus self damage minus distance
         */
        UNIFORM
    }

    enum class TargetPriority {
        /**
         * Target closest to us
         */
        DISTANCE,

        /**
         * Target with the lowest health
         */
        HEALTH,

        /**
         * Target with the lowest total armour value
         */
        ARMOUR
    }

    enum class When {
        /**
         * Only place when holding crystals
         */
        HOLDING,

        /**
         * Switch to crystals
         */
        SWITCH,

        /**
         * Silent switch to crystals (with a packet)
         */
        SILENT_SWITCH
    }

    enum class ExplodeFilter {
        /**
         * Explode all crystals regardless of parameters
         */
        ALL,

        /**
         * Explode crystals if they fit our minimum damage and maximum local damage requirements
         */
        SMART,

        /**
         * Explode crystals only if we have placed them
         */
        SELF,

        /**
         * Explode crystals if they fit our minimum damage and maximum local damage requirements, and only we have placed them
         */
        SELF_SMART
    }

    enum class AntiWeakness {
        /**
         * Switch to sword
         */
        SWITCH,

        /**
         * Silent switch to sword
         */
        SILENT,

        /**
         * Don't switch at all
         */
        OFF
    }

    enum class SetDead {
        /**
         * Set crystal's alive status to dead when we attack it
         */
        ATTACK,

        /**
         * Set crystal's alive status to dead when the explosion sound plays
         */
        SOUND,

        /**
         * Don't modify when we set it's alive status
         */
        OFF
    }

    enum class Swing {
        /**
         * Swing main hand
         */
        MAIN_HAND,

        /**
         * Swing offhand
         */
        OFFHAND,

        /**
         * Swing both hands
         */
        BOTH,

        /**
         * Send swing animation packet
         */
        PACKET,

        /**
         * Don't swing
         */
        OFF
    }

    data class Crystal(val crystal: EntityEnderCrystal, val targetDamage: Float, val selfDamage: Float)
    data class CrystalPosition(val position: BlockPos, val facing: EnumFacing, val facingVec: Vec3d, val targetDamage: Float, val selfDamage: Float)

}