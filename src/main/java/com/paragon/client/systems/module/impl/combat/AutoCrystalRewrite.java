package com.paragon.client.systems.module.impl.combat;

import com.paragon.Paragon;
import com.paragon.api.event.network.PacketEvent;
import com.paragon.api.util.calculations.Timer;
import com.paragon.api.util.entity.EntityUtil;
import com.paragon.api.util.player.InventoryUtil;
import com.paragon.api.util.player.PlayerUtil;
import com.paragon.api.util.player.RotationUtil;
import com.paragon.api.util.render.ColourUtil;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.world.BlockUtil;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.settings.impl.BooleanSetting;
import com.paragon.client.systems.module.settings.impl.ColourSetting;
import com.paragon.client.systems.module.settings.impl.ModeSetting;
import com.paragon.client.systems.module.settings.impl.NumberSetting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.Explosion;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * bad autocrystal. I have looked at some other client's ACs whilst writing this, but it isn't really skidded (apart from damage calcs - postman (which in turn I believe is skidded from gs)).
 * @author Wolfsurge
 */
@SuppressWarnings("unchecked")
public class AutoCrystalRewrite extends Module {

    // Order of operations
    public final ModeSetting<Order> order = new ModeSetting<>("Order", "The order of operations", Order.PLACE_EXPLODE);
    public final BooleanSetting grouped = (BooleanSetting) new BooleanSetting("Grouped", "Immediately attack or place the crystal after calculating", true).setParentSetting(order);

    public final ModeSetting<Heuristic> heuristic = new ModeSetting<>("Heuristic", "The way to calculate damage", Heuristic.MINIMAX);

    // Targeting settings
    public final BooleanSetting targeting = new BooleanSetting("Targeting" , "Settings for targeting players", true);
    public final BooleanSetting targetFriends = (BooleanSetting) new BooleanSetting("Friends", "Target friends", false).setParentSetting(targeting);
    public final NumberSetting targetRange = (NumberSetting) new NumberSetting("Range", "The range to target players", 10, 1, 15, 1).setParentSetting(targeting);

    // Place settings
    public final BooleanSetting place = new BooleanSetting("Place", "Automatically place crystals", true);
    public final ModeSetting<When> placeWhen = (ModeSetting<When>) new ModeSetting<>("When", "When to place", When.HOLDING).setParentSetting(place);
    public final BooleanSetting placeWhenSwitchBack = (BooleanSetting) new BooleanSetting("Switch Back", "Switch back to your original item", true).setParentSetting(place).setVisiblity(() -> placeWhen.getCurrentMode().equals(When.SWITCH) || placeWhen.getCurrentMode().equals(When.SILENT_SWITCH));
    public final NumberSetting placeRange = (NumberSetting) new NumberSetting("Range", "The range to place", 5, 1, 7, 1).setParentSetting(place);
    public final NumberSetting placeDelay = (NumberSetting) new NumberSetting("Delay", "The delay between placing crystals", 10, 0, 100, 1).setParentSetting(place);
    public final ModeSetting<Rotate> placeRotate = (ModeSetting<Rotate>) new ModeSetting<>("Rotate", "Rotate to the position you are placing at", Rotate.PACKET).setParentSetting(place);
    public final BooleanSetting placeRotateBack = (BooleanSetting) new BooleanSetting("Rotate Back", "Rotate back to your original rotation", true).setParentSetting(place).setVisiblity(() -> !placeRotate.getCurrentMode().equals(Rotate.NONE));
    public final BooleanSetting raytracePosition = (BooleanSetting) new BooleanSetting("Raytrace Position", "Checks if you can see the position", true).setParentSetting(place);
    public final NumberSetting placeMinDamage = (NumberSetting) new NumberSetting("Min Damage", "The minimum amount of damage to do to the target", 4, 0, 36, 1).setParentSetting(place);
    public final NumberSetting placeMaxLocal = (NumberSetting) new NumberSetting("Max Local Damage", "The minimum amount of damage to inflict upon yourself", 8, 0, 36, 1).setParentSetting(place);
    public final BooleanSetting placePacket = (BooleanSetting) new BooleanSetting("Packet", "Place with only a packet", false).setParentSetting(place);
    public final ModeSetting<Swing> placeSwing = (ModeSetting<Swing>) new ModeSetting<>("Swing", "Swing when placing a crystal", Swing.MAIN_HAND).setParentSetting(place);

    // Explode settings
    public final BooleanSetting explode = new BooleanSetting("Explode", "Automatically explode crystals", true);
    public final NumberSetting explodeRange = (NumberSetting) new NumberSetting("Range", "The range to explode crystals", 5, 1, 7, 1).setParentSetting(explode);
    public final NumberSetting explodeDelay = (NumberSetting) new NumberSetting("Delay", "The delay between exploding crystals", 10, 0, 100, 1).setParentSetting(explode);
    public final ModeSetting<ExplodeFilter> explodeFilter = (ModeSetting<ExplodeFilter>) new ModeSetting<>("Filter", "What crystals to explode", ExplodeFilter.SMART).setParentSetting(explode);
    public final ModeSetting<Rotate> explodeRotate = (ModeSetting<Rotate>) new ModeSetting<>("Rotate", "How to rotate to the crystal", Rotate.PACKET).setParentSetting(explode);
    public final BooleanSetting explodeRotateBack = (BooleanSetting) new BooleanSetting("Rotate Back", "Rotate back to your original rotation", true).setParentSetting(explode).setVisiblity(() -> !explodeRotate.getCurrentMode().equals(Rotate.NONE));
    public final ModeSetting<AntiWeakness> antiWeakness = (ModeSetting<AntiWeakness>) new ModeSetting<>("Anti Weakness", "If you have the weakness effect, you will still be able to explode crystals", AntiWeakness.SWITCH).setParentSetting(explode);
    public final BooleanSetting strictInventory = (BooleanSetting) new BooleanSetting("Strict Inventory", "Fake opening your inventory when you switch", true).setParentSetting(explode).setVisiblity(() -> !antiWeakness.getCurrentMode().equals(AntiWeakness.OFF));
    public final BooleanSetting packetExplode = (BooleanSetting) new BooleanSetting("Packet", "Explode crystals with a packet only", false).setParentSetting(explode);
    public final ModeSetting<Swing> explodeSwing = (ModeSetting<Swing>) new ModeSetting<>("Swing", "How to swing your hand", Swing.BOTH).setParentSetting(explode);
    public final NumberSetting explodeMinDamage = (NumberSetting) new NumberSetting("Min Damage", "The minimum amount of damage to do to the target", 4, 0, 36, 1).setParentSetting(explode).setVisiblity(() -> explodeFilter.getCurrentMode().equals(ExplodeFilter.SMART) || explodeFilter.getCurrentMode().equals(ExplodeFilter.SELF_SMART));
    public final NumberSetting explodeMaxLocal = (NumberSetting) new NumberSetting("Max Local Damage", "The minimum amount of damage to inflict upon yourself", 8, 0, 36, 1).setParentSetting(explode).setVisiblity(() -> explodeFilter.getCurrentMode().equals(ExplodeFilter.SMART) || explodeFilter.getCurrentMode().equals(ExplodeFilter.SELF_SMART));
    public final ModeSetting<SetDead> explodeSetDead = (ModeSetting<SetDead>) new ModeSetting<>("Set Dead", "Set the crystals alive status to dead", SetDead.ATTACK).setParentSetting(explode);

    // Override settings
    public final BooleanSetting override = new BooleanSetting("Override", "Override minimum damage when certain things happen", true);
    public final BooleanSetting overrideHealth = (BooleanSetting) new BooleanSetting("Health", "Override if the target's health is below a value", true).setParentSetting(override);
    public final NumberSetting overrideHealthValue = (NumberSetting) new NumberSetting("Override Health", "If the targets health is this value or below, ignore minimum damage", 10, 0, 36, 1).setParentSetting(override).setVisiblity(() -> override.isEnabled());

    // Pause settings
    public final BooleanSetting pause = new BooleanSetting("Pause", "Pause if certain things are happening", true);
    public final BooleanSetting pauseEating = (BooleanSetting) new BooleanSetting("Eating", "Pause when eating", true).setParentSetting(pause);
    public final BooleanSetting pauseDrinking = (BooleanSetting) new BooleanSetting("Drinking", "Pause when drinking", true).setParentSetting(pause);
    public final BooleanSetting pauseHealth = (BooleanSetting) new BooleanSetting("Health", "Pause when your health is below a specified value", true).setParentSetting(pause);
    public final NumberSetting pauseHealthValue = (NumberSetting) new NumberSetting("Health Value", "The health to pause at", 10, 1, 20, 1).setParentSetting(pause).setVisiblity(pauseHealth::isEnabled);

    // Render settings
    public final BooleanSetting render = new BooleanSetting("Render", "Render the placement", true);
    public final ModeSetting<Render> renderMode = (ModeSetting<Render>) new ModeSetting<>("Mode", "How to render placement", Render.BOTH).setParentSetting(render);
    public final NumberSetting renderOutlineWidth = (NumberSetting) new NumberSetting("Outline Width", "The width of the lines", 0.5f, 0.1f, 2, 0.1f).setParentSetting(render);
    public final ColourSetting renderColour = (ColourSetting) new ColourSetting("Colour", "The colour of the render", new Color(185, 19, 255)).setParentSetting(render);

    // The current player we are targeting
    private EntityPlayer currentTarget;

    // The current crystal we are targeting
    private Crystal currentCrystal;

    // The current position we are placing at
    private CrystalPosition currentPlacement;

    // Exists purely to stop the flickering in the HUD info
    private CrystalPosition backlogPlacement;

    // Timers
    private final Timer explodeTimer = new Timer();
    private final Timer placeTimer = new Timer();

    // List of crystals we have placed
    private final List<BlockPos> selfPlacedCrystals = new ArrayList<>();

    public AutoCrystalRewrite() {
        super("AutoCrystalRewrite", ModuleCategory.COMBAT, "Automatically places and explodes crystals");
        this.addSettings(order, heuristic, place, explode, targeting, override, pause, render);
    }

    @Override
    public void onEnable() {
        reset();
    }

    @Override
    public void onDisable() {
        reset();
    }

    @Override
    public void onTick() {
        if (nullCheck()) {
            return;
        }

        if (pause.isEnabled()) {
            // Pause if we are at a low health
            if (pauseHealth.isEnabled() && mc.player.getHealth() <= pauseHealthValue.getValue()) {
                return;
            }

            // Pause if we are eating
            if (pauseEating.isEnabled() && PlayerUtil.isPlayerEating()) {
                return;
            }

            // Pause if we are drinking
            if (pauseDrinking.isEnabled() && PlayerUtil.isPlayerDrinking()) {
                return;
            }
        }

        // Set our target
        currentTarget = getCurrentTarget();

        // Don't do anything if we don't have a target
        if (currentTarget == null) {
            return;
        }

        switch (order.getCurrentMode()) {
            case PLACE_EXPLODE:
                // Find placement
                currentPlacement = findBestPosition();

                // Immediately place if we have found a position and grouped is enabled
                if (grouped.isEnabled()) {
                    placeSearchedPosition();
                }

                // Find crystal
                currentCrystal = findBestCrystal();

                // Immediately explode if we have found a crystal and grouped is enabled
                if (grouped.isEnabled()) {
                    explodeSearchedCrystal();
                }

                break;

            case EXPLODE_PLACE:
                // Find crystal
                currentCrystal = findBestCrystal();

                // Immediately explode if we have found a crystal and grouped is enabled
                if (grouped.isEnabled()) {
                    explodeSearchedCrystal();
                }

                // Find placement
                currentPlacement = findBestPosition();

                // Immediately place if we have found a position and grouped is enabled
                if (grouped.isEnabled()) {
                    placeSearchedPosition();
                }

                break;

        }

        // If we haven't grouped them
        if (!grouped.isEnabled()) {
            switch (order.getCurrentMode()) {
                case PLACE_EXPLODE:
                    // Place crystal at position
                    placeSearchedPosition();

                    // Explode best crystal
                    explodeSearchedCrystal();
                    break;
                case EXPLODE_PLACE:
                    // Explode best crystal
                    explodeSearchedCrystal();

                    // Place crystal at position
                    placeSearchedPosition();
                    break;
            }
        }
    }

    @Override
    public void onRender3D() {
        // Check we want to render
        if (render.isEnabled()) {
            // Check we have a placement and placing is enabled
            if (currentPlacement != null && place.isEnabled()) {
                // Render fill
                if (renderMode.getCurrentMode().equals(Render.FILL) || renderMode.getCurrentMode().equals(Render.BOTH)) {
                    RenderUtil.drawFilledBox(BlockUtil.getBlockBox(currentPlacement.getPosition()), renderColour.getColour());
                }

                // Render outline
                if (renderMode.getCurrentMode().equals(Render.OUTLINE) || renderMode.getCurrentMode().equals(Render.BOTH)) {
                    RenderUtil.drawBoundingBox(BlockUtil.getBlockBox(currentPlacement.getPosition()), renderOutlineWidth.getValue(), ColourUtil.integrateAlpha(renderColour.getColour().darker(), 255));
                }
            }
        }
    }

    @Listener
    public void onPacketReceive(PacketEvent.PreReceive event) {
        // If we are trying to use and item on a block
        if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {
            // Get packet
            CPacketPlayerTryUseItemOnBlock packet = (CPacketPlayerTryUseItemOnBlock) event.getPacket();

            // Check we are holding end crystals
            if (mc.player.getHeldItem(packet.getHand()).getItem() == Items.END_CRYSTAL) {
                // If we can place a crystal on that block, add it to our self placed crystals list
                if (canPlaceCrystal(packet.getPos()) && currentTarget != null) {
                    selfPlacedCrystals.add(packet.getPos());
                }
            }
        }

        // Check it's a sound packet
        if (event.getPacket() instanceof SPacketSoundEffect && explodeSetDead.getCurrentMode().equals(SetDead.SOUND)) {
            // Get packet
            SPacketSoundEffect packet = (SPacketSoundEffect) event.getPacket();

            // Check it's an explosion sound
            if (packet.getSound().equals(SoundEvents.ENTITY_GENERIC_EXPLODE) && packet.getCategory().equals(SoundCategory.BLOCKS)) {
                // Iterate through loaded entities
                for (Entity entity : mc.world.loadedEntityList) {
                    // If the entity isn't an ender crystal, ignore
                    if (!(entity instanceof EntityEnderCrystal)) {
                        continue;
                    }

                    // If the entity is dead, ignore
                    if (entity.isDead) {
                        continue;
                    }

                    // If the crystal is close to the explosion sound origin, set the crystals state to dead
                    if (entity.getDistance(packet.getX(), packet.getY(), packet.getZ()) <= 6) {
                        entity.setDead();
                    }
                }
            }
        }
    }

    public EntityPlayer getCurrentTarget() {
        // The best target (we will return this)
        EntityPlayer target = null;

        // Iterate through loaded entities
        for (Entity entity : mc.world.loadedEntityList) {
            // Check it's a player that isn't us
            if (entity instanceof EntityOtherPlayerMP) {
                // If the player is dead, ignore
                if (entity.isDead) {
                    continue;
                }

                // Get player
                EntityPlayer entityPlayer = (EntityPlayer) entity;

                // If it's too far away, ignore
                if (EntityUtil.isTooFarAwayFromSelf(entityPlayer, targetRange.getValue())) {
                    continue;
                }

                // If it's a friend, and we don't want to target friends, ignore
                if (!targetFriends.isEnabled()) {
                    if (Paragon.INSTANCE.getSocialManager().isFriend(entityPlayer.getName())) {
                        continue;
                    }
                }

                // If the current best target is null, or this target is closer to the player, set the best target to this player
                if (target == null || mc.player.getDistance(entityPlayer) < mc.player.getDistance(target)) {
                    target = entityPlayer;
                }
            }
        }

        return target;
    }

    public void explodeSearchedCrystal() {
        // Check we want to explode
        if (explode.isEnabled()) {
            // Check we have a crystal to explode, and the timer has passed the required value
            if (currentCrystal != null && explodeTimer.hasTimePassed((long) explodeDelay.getValue())) {
                // Get our current slot so we can switch back
                int antiWeaknessSlot = mc.player.inventory.currentItem;

                // Check we want to apply anti weakness
                if (!antiWeakness.getCurrentMode().equals(AntiWeakness.OFF)) {
                    // Check we have the weakness effect
                    if (mc.player.isPotionActive(MobEffects.WEAKNESS)) {
                        // If we want to fake opening our inventory, send the opening inventory packet
                        if (strictInventory.isEnabled()) {
                            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.OPEN_INVENTORY));
                        }

                        // Get the best sword
                        int hotbarSwordSlot = InventoryUtil.getItemInHotbar(Items.DIAMOND_SWORD);

                        // If we have found a sword, switch to it
                        if (hotbarSwordSlot != -1) {
                            InventoryUtil.switchToSlot(hotbarSwordSlot, antiWeakness.getCurrentMode().equals(AntiWeakness.SILENT));
                        }
                    }
                }

                // Get our original rotation before rotating to the crystal
                Vec2f originalPlayerRotation = new Vec2f(mc.player.rotationYaw, mc.player.rotationPitch);

                // Check we want to rotate
                if (!explodeRotate.getCurrentMode().equals(Rotate.NONE)) {
                    // Get rotation
                    Vec2f rotation = RotationUtil.getRotationToVec3d(new Vec3d(currentCrystal.getCrystal().posX, currentCrystal.getCrystal().posY + 1, currentCrystal.getCrystal().posZ));

                    // Rotate to crystal
                    RotationUtil.rotate(rotation, explodeRotate.getCurrentMode().equals(Rotate.PACKET));
                }

                if (packetExplode.isEnabled()) {
                    // Explode with a packet
                    mc.player.connection.sendPacket(new CPacketUseEntity(currentCrystal.getCrystal()));
                } else {
                    // Attack crystal
                    mc.playerController.attackEntity(mc.player, currentCrystal.getCrystal());
                }

                // If we want to set the crystal to dead as soon as we attack, do that
                if (explodeSetDead.getCurrentMode().equals(SetDead.ATTACK)) {
                    currentCrystal.getCrystal().setDead();
                }

                // Remove it from our self placed crystals
                selfPlacedCrystals.remove(currentCrystal.getCrystal().getPosition());

                // Swing our arm
                swing(explodeSwing.getCurrentMode());

                // Rotate back to our original rotation
                if (!explodeRotate.getCurrentMode().equals(Rotate.NONE) && explodeRotateBack.isEnabled()) {
                    RotationUtil.rotate(originalPlayerRotation, explodeRotate.getCurrentMode().equals(Rotate.PACKET));
                }

                // Check we want to switch
                if (!antiWeakness.getCurrentMode().equals(AntiWeakness.OFF)) {
                    // Check we have weakness
                    if (mc.player.isPotionActive(MobEffects.WEAKNESS)) {
                        // Fake opening inventory
                        if (strictInventory.isEnabled()) {
                            mc.player.connection.sendPacket(new CPacketCloseWindow(mc.player.inventoryContainer.windowId));
                        }

                        // Switch to slot
                        if (antiWeaknessSlot != -1) {
                            InventoryUtil.switchToSlot(antiWeaknessSlot, antiWeakness.getCurrentMode().equals(AntiWeakness.SILENT));
                        }
                    }
                }
            }
        }
    }

    public void placeSearchedPosition() {
        // Check we have a position to place at, we are holding crystals, and the place timer has passed the required time
        if (currentPlacement != null && InventoryUtil.getHandHolding(Items.END_CRYSTAL) != null && placeTimer.hasTimePassed((long) placeDelay.getValue())) {
            // Get our current rotation
            Vec2f originalRotation = new Vec2f(mc.player.rotationYaw, mc.player.rotationPitch);

            // Check we want to rotate
            if (!placeRotate.getCurrentMode().equals(Rotate.NONE)) {
                // Get rotation
                Vec2f placeRotation = RotationUtil.getRotationToBlockPos(currentPlacement.getPosition());

                // Rotate to position
                RotationUtil.rotate(placeRotation, placeRotate.getCurrentMode().equals(Rotate.PACKET));
            }

            if (placePacket.isEnabled()) {
                // Send place packet
                mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(currentPlacement.getPosition(), currentPlacement.getFacing(), InventoryUtil.getHandHolding(Items.END_CRYSTAL), 0, 0, 0));
            } else {
                // Place crystal
                mc.playerController.processRightClickBlock(mc.player, mc.world, currentPlacement.getPosition(), currentPlacement.getFacing(), new Vec3d(currentPlacement.getFacing().getDirectionVec()), InventoryUtil.getHandHolding(Items.END_CRYSTAL));
            }

            // Swing arm
            swing(placeSwing.getCurrentMode());

            // Add position to our self placed crystals
            selfPlacedCrystals.add(currentPlacement.getPosition().up());

            // Check we want to rotate back
            if (!placeRotate.getCurrentMode().equals(Rotate.NONE) && placeRotateBack.isEnabled()) {
                // Rotate back
                RotationUtil.rotate(originalRotation, placeRotate.getCurrentMode().equals(Rotate.PACKET));
            }
        }
    }

    public Crystal findBestCrystal() {
        // The best crystal (we will return this)
        Crystal crystal = null;

        // Check we want to explode
        if (explode.isEnabled()) {
            // Iterate through loaded entities
            for (Entity entity : mc.world.loadedEntityList) {
                // Check the entity is a crystal
                if (entity instanceof EntityEnderCrystal) {
                    // If it's too far away, ignore
                    if (EntityUtil.isTooFarAwayFromSelf(entity, explodeRange.getValue())) {
                        continue;
                    }

                    // Get the crystals position as a vector
                    Vec3d vec = new Vec3d(entity.posX, entity.posY, entity.posZ);

                    // Crystal
                    Crystal calculatedCrystal = new Crystal((EntityEnderCrystal) entity, calculateDamage(vec, currentTarget), calculateDamage(vec, mc.player));

                    // Position of crystal
                    CrystalPosition crystalPos = new CrystalPosition(calculatedCrystal.getCrystal().getPosition(), null, calculatedCrystal.getTargetDamage(), calculatedCrystal.getSelfDamage());

                    if (isOverriding(currentTarget)) {
                        // Check it meets our filter
                        switch (explodeFilter.getCurrentMode()) {
                            case SELF:
                                // Check it's in our self placed crystals
                                if (!selfPlacedCrystals.contains(crystalPos.getPosition())) {
                                    continue;
                                }

                                break;

                            case SELF_SMART:
                                // Check it's in our self placed crystals
                                if (!selfPlacedCrystals.contains(crystalPos.getPosition())) {
                                    continue;
                                }

                                // Check it meets our max local requirement
                                if (calculatedCrystal.getSelfDamage() > explodeMaxLocal.getValue()) {
                                    continue;
                                }

                                // Check it meets our minimum damage requirement
                                if (calculatedCrystal.getTargetDamage() < explodeMinDamage.getValue()) {
                                    continue;
                                }

                                break;

                            case SMART:
                                // Check it meets our max local requirement
                                if (calculatedCrystal.getSelfDamage() > explodeMaxLocal.getValue()) {
                                    continue;
                                }

                                // Check it meets our minimum damage requirement
                                if (calculatedCrystal.getTargetDamage() < explodeMinDamage.getValue()) {
                                    continue;
                                }

                                break;
                        }
                    }

                    // Set the crystal to this if: the current best crystal is null, or this crystal's target damage is higher than the last crystal checked
                    if (crystal == null || calculateHeuristic(calculatedCrystal.getSelfDamage(), calculatedCrystal.getTargetDamage(), mc.player.getDistance(entity), heuristic.getCurrentMode()) > calculateHeuristic(crystal.getSelfDamage(), crystal.getTargetDamage(), mc.player.getDistance(entity), heuristic.getCurrentMode())) {
                        crystal = calculatedCrystal;
                    }
                }
            }
        }

        return crystal;
    }

    public CrystalPosition findBestPosition() {
        // The best placement (we will return this)
        CrystalPosition bestPlacement = null;

        // Check we want to place
        if (place.isEnabled()) {
            // Get our current slot
            int oldSlot = mc.player.inventory.currentItem;

            // Check we want to place
            switch (placeWhen.getCurrentMode()) {
                case HOLDING:
                    // Check we are holding crystals
                    if (!InventoryUtil.isHolding(Items.END_CRYSTAL)) {
                        return null;
                    }

                case SILENT_SWITCH:
                case SWITCH:
                    // If we aren't holding crystals
                    if (!InventoryUtil.isHolding(Items.END_CRYSTAL)) {
                        // Get crystals in our hotbar
                        int crystalSlot = InventoryUtil.getItemInHotbar(Items.END_CRYSTAL);

                        // Return null if we couldn't find crystals
                        if (crystalSlot == -1) {
                            return null;
                        } else {
                            // Switch to crystal slot
                            InventoryUtil.switchToSlot(crystalSlot, placeWhen.getCurrentMode().equals(When.SILENT_SWITCH));
                        }
                    }
            }

            // Iterate through blocks around us
            for (BlockPos pos : BlockUtil.getSphere(placeRange.getValue(), true)) {
                // Check we can place crystals
                if (!canPlaceCrystal(pos)) {
                    continue;
                }

                // Position we are placing at
                Vec3d placeVec = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

                // Position we will calculate damage at
                Vec3d damageVec = new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);

                // Get the direction we want to face
                EnumFacing facing = EnumFacing.getDirectionFromEntityLiving(pos, mc.player);

                // Check we can raytrace to it
                if (raytracePosition.isEnabled()) {
                    RayTraceResult result = mc.world.rayTraceBlocks(mc.player.getPositionEyes(1), placeVec);

                    // Check we hit a block
                    if (result != null && result.typeOfHit.equals(RayTraceResult.Type.BLOCK)) {
                        facing = result.sideHit;
                    }
                }

                // Create new crystal position
                CrystalPosition crystalPosition = new CrystalPosition(pos, facing, calculateDamage(damageVec, currentTarget), calculateDamage(damageVec, mc.player));

                // Check we aren't overriding
                if (isOverriding(currentTarget)) {
                    // Check it's above or equal to our minimum damage requirement
                    if (crystalPosition.getTargetDamage() <= placeMinDamage.getValue()) {
                        continue;
                    }

                    // Check it's below or equal to our maximum local damage requirement
                    if (crystalPosition.getSelfDamage() >= placeMaxLocal.getValue()) {
                        continue;
                    }
                }

                // Set it to our best placement if it does more damage to the target than our last position
                if (bestPlacement == null || calculateHeuristic(crystalPosition, heuristic.getCurrentMode()) > calculateHeuristic(bestPlacement, heuristic.getCurrentMode())) {
                    bestPlacement = crystalPosition;
                }
            }

            // If the 'When' setting wasn't 'Holding'
            if (!placeWhen.getCurrentMode().equals(When.HOLDING)) {
                // If we want to switch back
                if (placeWhenSwitchBack.isEnabled()) {
                    // Switch back
                    InventoryUtil.switchToSlot(oldSlot, placeWhen.getCurrentMode().equals(When.SILENT_SWITCH));
                }
            }
        }

        // Set our backlog placement
        backlogPlacement = bestPlacement;

        // Return our best placement
        return bestPlacement;
    }

    public boolean isOverriding(EntityLivingBase entity) {
        return !(entity.getHealth() <= overrideHealthValue.getValue()) || !override.isEnabled() || !overrideHealth.isEnabled();
    }

    public void swing(Swing swing) {
        switch (swing) {
            case MAIN_HAND:
                // Swing main hand
                mc.player.swingArm(EnumHand.MAIN_HAND);
                break;
            case OFFHAND:
                // Swing offhand
                mc.player.swingArm(EnumHand.OFF_HAND);
                break;
            case BOTH:
                // Swing both hands
                mc.player.swingArm(EnumHand.MAIN_HAND);
                mc.player.swingArm(EnumHand.OFF_HAND);
                break;
            case PACKET:
                // Send a swing hand packet
                mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
                break;
        }
    }

    public float calculateHeuristic(float self, float target, float distance, Heuristic heuristic) {
        switch (heuristic) {
            case DAMAGE:
                // Just target damage
                return target;
            case MINIMAX:
                // Target damage minus self damage
                return target - self;
            case UNIFORM:
                // Target damage minus self damage minus distance
                return target - self - distance;
        }

        return target;
    }

    public float calculateHeuristic(CrystalPosition crystal, Heuristic heuristic) {
        // Prevent NPE
        if (crystal == null) {
            return 0;
        }

        // Calculate and return heuristic
        return calculateHeuristic(crystal.getSelfDamage(), crystal.getTargetDamage(), (float) mc.player.getDistanceSq(crystal.getPosition()), heuristic);
    }

    public boolean canPlaceCrystal(BlockPos pos) {
        // Get block
        Block block = BlockUtil.getBlockAtPos(pos);

        // Check the block is obsidian or bedrock
        if (!(block.equals(Blocks.OBSIDIAN) || block.equals(Blocks.BEDROCK))) {
            return false;
        }

        // Check the position above is an air block
        if (!mc.world.isAirBlock(pos.up()) || !mc.world.getBlockState(pos.up(2)).getMaterial().isReplaceable()) {
            return false;
        }

        // Check we aren't standing in the position
        if (mc.player.getPosition().equals(pos)) {
            return false;
        }

        // Check no entities are colliding with the position
        for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, BlockUtil.getBlockBox(pos.up()))) {
            // Check the entity isn't an end crystal
            if (entity instanceof EntityEnderCrystal && entity.getPosition().equals(pos.up()) && entity.ticksExisted < 20) {
                continue;
            }

            // If the entity is dead, we can still place here
            if (entity.isDead) {
                continue;
            }

            return false;
        }

        return true;
    }

    public float calculateDamage(Vec3d vec, EntityLivingBase entity) {
        float doubleExplosionSize = 12.0F;
        double distancedSize = entity.getDistance(vec.x, vec.y, vec.z) / (double) doubleExplosionSize;
        Vec3d vec3d = new Vec3d(vec.x, vec.y, vec.z);
        double blockDensity = entity.world.getBlockDensity(vec3d, entity.getEntityBoundingBox());
        double v = (1.0D - distancedSize) * blockDensity;
        float damage = (float) ((int) ((v * v + v) / 2.0D * 7.0D * (double) doubleExplosionSize + 1.0D));
        double finald;

        float damageMultiplied = damage * (mc.world.getDifficulty().getDifficultyId() == 0 ? 0 : (mc.world.getDifficulty().getDifficultyId() == 2 ? 1 : (mc.world.getDifficulty().getDifficultyId() == 1 ? 0.5f : 1.5f)));
        finald = getBlastReduction(entity, damageMultiplied, new Explosion(mc.world, entity, vec.x, vec.y, vec.z, 6F, false, true));
        return (float) finald;
    }

    public float getBlastReduction(EntityLivingBase entity, float damage, Explosion explosion) {
        DamageSource source = DamageSource.causeExplosionDamage(explosion);
        damage = CombatRules.getDamageAfterAbsorb(damage, entity.getTotalArmorValue(), (float) entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
        float enchantModifier = MathHelper.clamp(EnchantmentHelper.getEnchantmentModifierDamage(entity.getArmorInventoryList(), source), 0, 20);

        damage *= 1 - enchantModifier / 25;

        if (entity.isPotionActive(MobEffects.RESISTANCE)) {
            damage -= damage / 4;
        }

        return Math.max(damage, 0);
    }

    public void reset() {
        this.currentTarget = null;
        this.currentCrystal = null;
        this.currentPlacement = null;
        this.selfPlacedCrystals.clear();
    }

    @Override
    public String getModuleInfo() {
        return (currentTarget == null ? " No Target" : " " + currentTarget.getName() + EntityUtil.getTextColourFromEntityHealth(currentTarget) + " " + currentTarget.getHealth() + TextFormatting.GRAY + " DMG " + (backlogPlacement == null ? "No Placement" : Math.round(calculateHeuristic(backlogPlacement, heuristic.getCurrentMode()))));
    }

    public enum Order {
        /**
         * Place then explode
         */
        PLACE_EXPLODE,

        /**
         * Explode then place
         */
        EXPLODE_PLACE
    }

    public enum Heuristic {
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

    public enum When {
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

    public enum ExplodeFilter {
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

    public enum AntiWeakness {
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

    public enum SetDead {
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

    public enum Rotate {
        /**
         * Don't rotate
         */
        NONE,

        /**
         * Rotate player's rotation angles
         */
        LEGIT,

        /**
         * Send rotate packet
         */
        PACKET
    }

    public enum Swing {
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

    public enum Render {
        /**
         * Render outline
         */
        OUTLINE,

        /**
         * Render fill
         */
        FILL,

        /**
         * Render both
         */
        BOTH
    }

    static class Crystal {
        // The entity
        private final EntityEnderCrystal crystal;

        // The damage we do to the target
        private final float targetDamage;

        // The damage we do to us
        private final float selfDamage;

        public Crystal(EntityEnderCrystal crystal, float targetDamage, float selfDamage) {
            this.crystal = crystal;
            this.targetDamage = targetDamage;
            this.selfDamage = selfDamage;
        }

        /**
         * Gets the crystal
         * @return The crystal
         */
        public EntityEnderCrystal getCrystal() {
            return crystal;
        }

        /**
         * Gets the target damage
         * @return The target damage
         */
        public float getTargetDamage() {
            return targetDamage;
        }

        /**
         * Gets the self damage
         * @return The self damage
         */
        public float getSelfDamage() {
            return selfDamage;
        }
    }

    static class CrystalPosition {
        // The position we will place at
        private final BlockPos position;

        // The direction we want to face
        private final EnumFacing facing;

        // The damage we do to the target
        private final float targetDamage;

        // The damage we do to us
        private final float selfDamage;

        public CrystalPosition(BlockPos position, EnumFacing facing, float targetDamage, float selfDamage) {
            this.position = position;
            this.facing = facing;
            this.targetDamage = targetDamage;
            this.selfDamage = selfDamage;
        }

        /**
         * Gets the position
         * @return The position
         */
        public BlockPos getPosition() {
            return position;
        }

        /**
         * Gets the direction we want to face in
         * @return The direction we want to face in
         */
        public EnumFacing getFacing() {
            return facing;
        }

        /**
         * Gets the target damage
         * @return The target damage
         */
        public float getTargetDamage() {
            return targetDamage;
        }

        /**
         * Gets the self damage
         * @return The self damage
         */
        public float getSelfDamage() {
            return selfDamage;
        }
    }
}