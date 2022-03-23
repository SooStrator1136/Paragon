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
import com.paragon.client.systems.module.impl.client.Colours;
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
import net.minecraft.network.play.server.SPacketDestroyEntities;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.Explosion;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * bad autocrystal. I have looked at some other client's ACs whilst writing this, but it isn't really skidded (apart from damage calcs - postman (which in turn I believe is skidded from gs)).
 * @author Wolfsurge
 */
@SuppressWarnings("unchecked")
public class AutoCrystalRewrite extends Module {

    public final ModeSetting<Order> order = new ModeSetting<>("Order", "The order of operations", Order.PLACE_EXPLODE);
    public final BooleanSetting grouped = (BooleanSetting) new BooleanSetting("Grouped", "Immediately attack or place the crystal after calculating", true).setParentSetting(order);

    public final ModeSetting<Heuristic> heuristic = new ModeSetting<>("Heuristic", "The way to calculate damage", Heuristic.MINIMAX);

    public final BooleanSetting targeting = new BooleanSetting("Targeting" , "Settings for targeting players", true);
    public final BooleanSetting targetFriends = (BooleanSetting) new BooleanSetting("Friends", "Target friends", false).setParentSetting(targeting);
    public final NumberSetting targetRange = (NumberSetting) new NumberSetting("Range", "The range to target players", 10, 1, 15, 1).setParentSetting(targeting);

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

    public final BooleanSetting override = new BooleanSetting("Override", "Override minimum damage when certain things happen", true);
    public final BooleanSetting overrideHealth = (BooleanSetting) new BooleanSetting("Health", "Override if the target's health is below a value", true).setParentSetting(override);
    public final NumberSetting overrideHealthValue = (NumberSetting) new NumberSetting("Override Health", "If the targets health is this value or below, ignore minimum damage", 10, 0, 36, 1).setParentSetting(override).setVisiblity(() -> override.isEnabled());

    public final BooleanSetting pause = new BooleanSetting("Pause", "Pause if certain things are happening", true);
    public final BooleanSetting pauseEating = (BooleanSetting) new BooleanSetting("Eating", "Pause when eating", true).setParentSetting(pause);
    public final BooleanSetting pauseDrinking = (BooleanSetting) new BooleanSetting("Drinking", "Pause when drinking", true).setParentSetting(pause);
    public final BooleanSetting pauseHealth = (BooleanSetting) new BooleanSetting("Health", "Pause when your health is below a specified value", true).setParentSetting(pause);
    public final NumberSetting pauseHealthValue = (NumberSetting) new NumberSetting("Health Value", "The health to pause at", 10, 1, 20, 1).setParentSetting(pause).setVisiblity(pauseHealth::isEnabled);

    public final BooleanSetting render = new BooleanSetting("Render", "Render the placement", true);
    public final ModeSetting<Render> renderMode = (ModeSetting<Render>) new ModeSetting<>("Mode", "How to render placement", Render.BOTH).setParentSetting(render);
    public final NumberSetting renderOutlineWidth = (NumberSetting) new NumberSetting("Outline Width", "The width of the lines", 0.5f, 0.1f, 2, 0.1f).setParentSetting(render);
    public final ColourSetting renderColour = (ColourSetting) new ColourSetting("Colour", "The colour of the render", new Color(185, 19, 255)).setParentSetting(render);

    private EntityPlayer currentTarget;
    private Crystal currentCrystal;
    private CrystalPosition currentPlacement;
    private CrystalPosition backlogPlacement;

    private final Timer explodeTimer = new Timer();
    private final Timer placeTimer = new Timer();

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
            if (pauseHealth.isEnabled() && mc.player.getHealth() <= pauseHealthValue.getValue()) {
                return;
            }

            if (pauseEating.isEnabled() && PlayerUtil.isPlayerEating()) {
                return;
            }

            if (pauseDrinking.isEnabled() && PlayerUtil.isPlayerDrinking()) {
                return;
            }
        }

        currentTarget = getCurrentTarget();

        if (currentTarget == null) {
            return;
        }

        switch (order.getCurrentMode()) {
            case PLACE_EXPLODE:
                currentPlacement = findBestPosition();

                if (grouped.isEnabled()) {
                    placeSearchedPosition();
                }

                currentCrystal = findBestCrystal();

                if (grouped.isEnabled()) {
                    explodeSearchedCrystal();
                }

                break;

            case EXPLODE_PLACE:
                currentCrystal = findBestCrystal();

                if (grouped.isEnabled()) {
                    explodeSearchedCrystal();
                }

                currentPlacement = findBestPosition();

                if (grouped.isEnabled()) {
                    placeSearchedPosition();
                }

                break;

        }

        if (!grouped.isEnabled()) {
            switch (order.getCurrentMode()) {
                case PLACE_EXPLODE:
                    placeSearchedPosition();
                    explodeSearchedCrystal();
                    break;
                case EXPLODE_PLACE:
                    explodeSearchedCrystal();
                    placeSearchedPosition();
                    break;
            }
        }
    }

    @Override
    public void onRender3D() {
        if (render.isEnabled()) {
            if (currentPlacement != null && place.isEnabled()) {
                if (renderMode.getCurrentMode().equals(Render.FILL) || renderMode.getCurrentMode().equals(Render.BOTH)) {
                    RenderUtil.drawFilledBox(BlockUtil.getBlockBox(currentPlacement.getPosition()), renderColour.getColour());
                }

                if (renderMode.getCurrentMode().equals(Render.OUTLINE) || renderMode.getCurrentMode().equals(Render.BOTH)) {
                    RenderUtil.drawBoundingBox(BlockUtil.getBlockBox(currentPlacement.getPosition()), renderOutlineWidth.getValue(), ColourUtil.integrateAlpha(renderColour.getColour().darker(), 255));
                }
            }
        }
    }

    @Listener
    public void onPacketReceive(PacketEvent.PreReceive event) {
        if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {
            CPacketPlayerTryUseItemOnBlock packet = (CPacketPlayerTryUseItemOnBlock) event.getPacket();

            if (mc.player.getHeldItem(packet.getHand()).getItem() == Items.END_CRYSTAL) {
                if (canPlaceCrystal(packet.getPos()) && currentTarget != null) {
                    selfPlacedCrystals.add(packet.getPos());
                }
            }
        }

        if (event.getPacket() instanceof SPacketSoundEffect && explodeSetDead.getCurrentMode().equals(SetDead.SOUND)) {
            SPacketSoundEffect packet = (SPacketSoundEffect) event.getPacket();

            if (packet.getSound().equals(SoundEvents.ENTITY_GENERIC_EXPLODE) && packet.getCategory().equals(SoundCategory.BLOCKS)) {
                for (Entity entity : mc.world.loadedEntityList) {
                    if (!(entity instanceof EntityEnderCrystal)) {
                        continue;
                    }

                    if (entity.isDead) {
                        continue;
                    }

                    if (entity.getDistance(packet.getX(), packet.getY(), packet.getZ()) <= 6) {
                        entity.setDead();
                    }
                }
            }
        }
    }

    public EntityPlayer getCurrentTarget() {
        EntityPlayer target = null;

        for (Entity entity : mc.world.loadedEntityList) {
            if (entity instanceof EntityOtherPlayerMP) {
                if (entity.isDead) {
                    continue;
                }

                EntityPlayer entityPlayer = (EntityPlayer) entity;

                if (EntityUtil.isTooFarAwayFromSelf(entityPlayer, targetRange.getValue())) {
                    continue;
                }

                if (!targetFriends.isEnabled()) {
                    if (Paragon.INSTANCE.getSocialManager().isFriend(entityPlayer.getName())) {
                        continue;
                    }
                }

                if (target == null || mc.player.getDistance(entityPlayer) < mc.player.getDistance(target)) {
                    target = entityPlayer;
                }
            }
        }

        return target;
    }

    public void explodeSearchedCrystal() {
        if (explode.isEnabled()) {
            if (currentCrystal != null && explodeTimer.hasTimePassed((long) explodeDelay.getValue())) {
                int antiWeaknessSlot = mc.player.inventory.currentItem;

                if (!antiWeakness.getCurrentMode().equals(AntiWeakness.OFF)) {
                    if (mc.player.isPotionActive(MobEffects.WEAKNESS)) {
                        if (strictInventory.isEnabled()) {
                            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.OPEN_INVENTORY));
                        }

                        int hotbarSwordSlot = InventoryUtil.getItemInHotbar(Items.DIAMOND_SWORD);

                        if (hotbarSwordSlot != -1) {
                            InventoryUtil.switchToSlot(hotbarSwordSlot, antiWeakness.getCurrentMode().equals(AntiWeakness.SILENT));
                        }
                    }
                }

                Vec2f originalPlayerRotation = new Vec2f(mc.player.rotationYaw, mc.player.rotationPitch);

                if (!explodeRotate.getCurrentMode().equals(Rotate.NONE)) {
                    Vec2f rotation = RotationUtil.getRotationToVec3d(new Vec3d(currentCrystal.getCrystal().posX, currentCrystal.getCrystal().posY + 1, currentCrystal.getCrystal().posZ));
                    RotationUtil.rotate(rotation, explodeRotate.getCurrentMode().equals(Rotate.PACKET));
                }

                if (packetExplode.isEnabled()) {
                    mc.player.connection.sendPacket(new CPacketUseEntity(currentCrystal.getCrystal()));
                } else {
                    mc.playerController.attackEntity(mc.player, currentCrystal.getCrystal());
                }

                if (explodeSetDead.getCurrentMode().equals(SetDead.ATTACK)) {
                    currentCrystal.getCrystal().setDead();
                }

                selfPlacedCrystals.remove(currentCrystal.getCrystal().getPosition());

                swing(explodeSwing.getCurrentMode());

                if (!explodeRotate.getCurrentMode().equals(Rotate.NONE) && explodeRotateBack.isEnabled()) {
                    RotationUtil.rotate(originalPlayerRotation, explodeRotate.getCurrentMode().equals(Rotate.PACKET));
                }

                if (!antiWeakness.getCurrentMode().equals(AntiWeakness.OFF)) {
                    if (mc.player.isPotionActive(MobEffects.WEAKNESS)) {
                        if (strictInventory.isEnabled()) {
                            mc.player.connection.sendPacket(new CPacketCloseWindow(mc.player.inventoryContainer.windowId));
                        }

                        if (antiWeaknessSlot != -1) {
                            InventoryUtil.switchToSlot(antiWeaknessSlot, antiWeakness.getCurrentMode().equals(AntiWeakness.SILENT));
                        }
                    }
                }
            }
        }
    }

    public void placeSearchedPosition() {
        if (currentPlacement != null && InventoryUtil.getHandHolding(Items.END_CRYSTAL) != null && placeTimer.hasTimePassed((long) placeDelay.getValue())) {
            Vec2f originalRotation = new Vec2f(mc.player.rotationYaw, mc.player.rotationPitch);

            if (!placeRotate.getCurrentMode().equals(Rotate.NONE)) {
                Vec2f placeRotation = RotationUtil.getRotationToBlockPos(currentPlacement.getPosition());
                RotationUtil.rotate(placeRotation, placeRotate.getCurrentMode().equals(Rotate.PACKET));
            }

            if (placePacket.isEnabled()) {
                mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(currentPlacement.getPosition(), currentPlacement.getFacing(), InventoryUtil.getHandHolding(Items.END_CRYSTAL), 0, 0, 0));
            } else {
                mc.playerController.processRightClickBlock(mc.player, mc.world, currentPlacement.getPosition(), currentPlacement.getFacing(), new Vec3d(currentPlacement.getFacing().getDirectionVec()), InventoryUtil.getHandHolding(Items.END_CRYSTAL));
            }

            swing(placeSwing.getCurrentMode());
            selfPlacedCrystals.add(currentPlacement.getPosition().up());

            if (!placeRotate.getCurrentMode().equals(Rotate.NONE) && placeRotateBack.isEnabled()) {
                RotationUtil.rotate(originalRotation, placeRotate.getCurrentMode().equals(Rotate.PACKET));
            }
        }
    }

    public Crystal findBestCrystal() {
        Crystal crystal = null;

        if (explode.isEnabled()) {
            for (Entity entity : mc.world.loadedEntityList) {
                if (entity instanceof EntityEnderCrystal) {
                    if (EntityUtil.isTooFarAwayFromSelf(entity, explodeRange.getValue())) {
                        continue;
                    }

                    Vec3d vec = new Vec3d(entity.posX, entity.posY, entity.posZ);

                    Crystal calculatedCrystal = new Crystal((EntityEnderCrystal) entity, calculateDamage(vec, currentTarget), calculateDamage(vec, mc.player));
                    CrystalPosition crystalPos = new CrystalPosition(calculatedCrystal.getCrystal().getPosition(), null, calculatedCrystal.getTargetDamage(), calculatedCrystal.getSelfDamage());

                    switch (explodeFilter.getCurrentMode()) {
                        case SELF:
                            if (!selfPlacedCrystals.contains(crystalPos.getPosition())) {
                                continue;
                            }

                            break;

                        case SELF_SMART:
                            if (!selfPlacedCrystals.contains(crystalPos.getPosition())) {
                                continue;
                            }

                            if (calculatedCrystal.getSelfDamage() > explodeMaxLocal.getValue()) {
                                continue;
                            }

                            if (calculatedCrystal.getTargetDamage() < explodeMinDamage.getValue() || isOverriding(currentTarget)) {
                                continue;
                            }

                            break;

                        case SMART:
                            if (calculatedCrystal.getSelfDamage() > explodeMaxLocal.getValue()) {
                                continue;
                            }

                            if (calculatedCrystal.getTargetDamage() < explodeMinDamage.getValue() || isOverriding(currentTarget)) {
                                continue;
                            }

                            break;
                    }

                    if (calculatedCrystal.getSelfDamage() > explodeMaxLocal.getValue()) {
                        continue;
                    }

                    if (calculatedCrystal.getTargetDamage() < explodeMinDamage.getValue() || isOverriding(currentTarget)) {
                        continue;
                    }

                    if (crystal == null || calculateHeuristic(calculatedCrystal.getSelfDamage(), calculatedCrystal.getTargetDamage(), mc.player.getDistance(entity), heuristic.getCurrentMode()) > calculateHeuristic(crystal.getSelfDamage(), crystal.getTargetDamage(), mc.player.getDistance(entity), heuristic.getCurrentMode())) {
                        crystal = calculatedCrystal;
                    }
                }
            }
        }

        return crystal;
    }

    public CrystalPosition findBestPosition() {
        CrystalPosition bestPlacement = null;

        if (place.isEnabled()) {
            int oldSlot = mc.player.inventory.currentItem;

            switch (placeWhen.getCurrentMode()) {
                case HOLDING:
                    if (!InventoryUtil.isHolding(Items.END_CRYSTAL)) {
                        return null;
                    }

                case SILENT_SWITCH:
                case SWITCH:
                    if (!InventoryUtil.isHolding(Items.END_CRYSTAL)) {
                        int crystalSlot = InventoryUtil.getItemInHotbar(Items.END_CRYSTAL);

                        if (crystalSlot == -1) {
                            return null;
                        } else {
                            InventoryUtil.switchToSlot(crystalSlot, placeWhen.getCurrentMode().equals(When.SILENT_SWITCH));
                        }
                    }
            }

            for (BlockPos pos : BlockUtil.getSphere(placeRange.getValue(), true)) {
                if (!canPlaceCrystal(pos)) {
                    continue;
                }

                Vec3d placeVec = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                Vec3d damageVec = new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);

                EnumFacing facing = EnumFacing.getDirectionFromEntityLiving(pos, mc.player);

                if (raytracePosition.isEnabled()) {
                    RayTraceResult result = mc.world.rayTraceBlocks(mc.player.getPositionEyes(1), placeVec);

                    if (result != null && result.typeOfHit.equals(RayTraceResult.Type.BLOCK)) {
                        facing = result.sideHit;
                    }
                }

                CrystalPosition crystalPosition = new CrystalPosition(pos, facing, calculateDamage(damageVec, currentTarget), calculateDamage(damageVec, mc.player));

                if (crystalPosition.getTargetDamage() <= placeMinDamage.getValue() || isOverriding(currentTarget)) {
                    continue;
                }

                if (crystalPosition.getSelfDamage() >= placeMaxLocal.getValue()) {
                    continue;
                }

                if (bestPlacement == null || calculateHeuristic(crystalPosition, heuristic.getCurrentMode()) > calculateHeuristic(bestPlacement, heuristic.getCurrentMode())) {
                    bestPlacement = crystalPosition;
                }
            }

            if (!placeWhen.getCurrentMode().equals(When.HOLDING)) {
                if (placeWhenSwitchBack.isEnabled()) {
                    InventoryUtil.switchToSlot(oldSlot, placeWhen.getCurrentMode().equals(When.SILENT_SWITCH));
                }
            }
        }

        backlogPlacement = bestPlacement;
        return bestPlacement;
    }

    public boolean isOverriding(EntityLivingBase entity) {
        return entity.getHealth() <= overrideHealthValue.getValue() && override.isEnabled() && overrideHealth.isEnabled();
    }

    public void swing(Swing swing) {
        switch (swing) {
            case MAIN_HAND:
                mc.player.swingArm(EnumHand.MAIN_HAND);
                break;
            case OFFHAND:
                mc.player.swingArm(EnumHand.OFF_HAND);
                break;
            case BOTH:
                mc.player.swingArm(EnumHand.MAIN_HAND);
                mc.player.swingArm(EnumHand.OFF_HAND);
                break;
            case PACKET:
                mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
                break;
        }
    }

    public float calculateHeuristic(float self, float target, float distance, Heuristic heuristic) {
        switch (heuristic) {
            case DAMAGE:
                return target;
            case MINIMAX:
                return target - self;
            case UNIFORM:
                return target - self - distance;
        }

        return target;
    }

    public float calculateHeuristic(Crystal crystal, Heuristic heuristic) {
        if (crystal == null) {
            return 0;
        }

        return calculateHeuristic(crystal.getSelfDamage(), crystal.getTargetDamage(), mc.player.getDistance(crystal.getCrystal()), heuristic);
    }

    public float calculateHeuristic(CrystalPosition crystal, Heuristic heuristic) {
        if (crystal == null) {
            return 0;
        }

        return calculateHeuristic(crystal.getSelfDamage(), crystal.getTargetDamage(), (float) mc.player.getDistanceSq(crystal.getPosition()), heuristic);
    }

    public boolean canPlaceCrystal(BlockPos pos) {
        Block block = BlockUtil.getBlockAtPos(pos);

        if (!(block.equals(Blocks.OBSIDIAN) || block.equals(Blocks.BEDROCK))) {
            return false;
        }

        if (!mc.world.isAirBlock(pos.up()) || !mc.world.getBlockState(pos.up(2)).getMaterial().isReplaceable()) {
            return false;
        }

        if (mc.player.getPosition().equals(pos)) {
            return false;
        }

        for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, BlockUtil.getBlockBox(pos.up()))) {
            if (entity instanceof EntityEnderCrystal && entity.getPosition().equals(pos.up()) && entity.ticksExisted < 20) {
                continue;
            }

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
        PLACE_EXPLODE,
        EXPLODE_PLACE
    }

    public enum Heuristic {
        DAMAGE,
        MINIMAX,
        UNIFORM
    }

    public enum When {
        HOLDING,
        SWITCH,
        SILENT_SWITCH
    }

    public enum ExplodeFilter {
        ALL,
        SMART,
        SELF,
        SELF_SMART
    }

    public enum AntiWeakness {
        SWITCH,
        SILENT,
        OFF
    }

    public enum SetDead {
        ATTACK,
        SOUND,
        OFF
    }

    public enum Rotate {
        NONE,
        LEGIT,
        PACKET
    }

    public enum Swing {
        MAIN_HAND,
        OFFHAND,
        BOTH,
        PACKET,
        OFF
    }

    public enum Render {
        OUTLINE,
        FILL,
        BOTH
    }

    static class Crystal {
        private final EntityEnderCrystal crystal;

        private final float targetDamage;

        private final float selfDamage;

        public Crystal(EntityEnderCrystal crystal, float targetDamage, float selfDamage) {
            this.crystal = crystal;
            this.targetDamage = targetDamage;
            this.selfDamage = selfDamage;
        }

        public EntityEnderCrystal getCrystal() {
            return crystal;
        }

        public float getTargetDamage() {
            return targetDamage;
        }

        public float getSelfDamage() {
            return selfDamage;
        }
    }

    static class CrystalPosition {
        private final BlockPos position;

        private final EnumFacing facing;

        private final float targetDamage;

        private final float selfDamage;

        public CrystalPosition(BlockPos position, EnumFacing facing, float targetDamage, float selfDamage) {
            this.position = position;
            this.facing = facing;
            this.targetDamage = targetDamage;
            this.selfDamage = selfDamage;
        }

        public BlockPos getPosition() {
            return position;
        }

        public EnumFacing getFacing() {
            return facing;
        }

        public float getTargetDamage() {
            return targetDamage;
        }

        public float getSelfDamage() {
            return selfDamage;
        }
    }
}
