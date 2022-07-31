package com.paragon.client.systems.module.impl.combat;

import com.paragon.Paragon;
import com.paragon.api.event.network.PacketEvent;
import com.paragon.api.module.Category;
import com.paragon.api.module.Module;
import com.paragon.api.setting.Bind;
import com.paragon.api.setting.Setting;
import com.paragon.api.util.calculations.Timer;
import com.paragon.api.util.entity.EntityUtil;
import com.paragon.api.util.player.InventoryUtil;
import com.paragon.api.util.player.PlayerUtil;
import com.paragon.api.util.player.RotationUtil;
import com.paragon.api.util.render.ColourUtil;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.world.BlockUtil;
import com.paragon.asm.mixins.accessor.ICPacketUseEntity;
import com.paragon.asm.mixins.accessor.IPlayerControllerMP;
import com.paragon.client.managers.rotation.Rotate;
import com.paragon.client.managers.rotation.Rotation;
import com.paragon.client.managers.rotation.RotationPriority;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.Explosion;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Surge
 */
@SideOnly(Side.CLIENT)
public class AutoCrystalRewrite extends Module {

    public static AutoCrystalRewrite INSTANCE;

    public static Setting<Order> order = new Setting<>("Order", Order.PLACE_EXPLODE)
            .setDescription("The order in which to complete actions");

    public static Setting<Timing> timing = new Setting<>("Timing", Timing.SEQUENTIAL)
            .setDescription("When to perform actions");

    // TARGETING

    public static Setting<TargetSort> targeting = new Setting<>("Targeting", TargetSort.DISTANCE)
            .setDescription("The target priority to use");

    public static Setting<Double> targetRange = new Setting<>("Range", 10.0D, 1.0D, 15.0D, 0.1D)
            .setDescription("The furthest a target can be away from you")
            .setParentSetting(targeting);

    public static Setting<Boolean> players = new Setting<>("Players", true)
            .setDescription("Whether to target players")
            .setParentSetting(targeting);

    public static Setting<Boolean> mobs = new Setting<>("Mobs", true)
            .setDescription("Whether to target mobs")
            .setParentSetting(targeting);

    public static Setting<Boolean> animals = new Setting<>("Animals", true)
            .setDescription("Whether to target animals")
            .setParentSetting(targeting);

    // PLACEMENTS

    public static Setting<Boolean> place = new Setting<>("Place", true)
            .setDescription("Whether to place crystals");

    public static Setting<Float> placeDelay = new Setting<>("Delay", 50f, 0f, 1000f, 1f)
            .setDescription("The delay between placing crystals")
            .setParentSetting(place);

    public static Setting<Perform> placePerform = new Setting<>("Perform", Perform.SILENT_SWITCH)
            .setDescription("When to perform the action of placing crystals")
            .setParentSetting(place);

    public static Setting<Boolean> swapBack = new Setting<>("SwapBack", true)
            .setDescription("Whether to swap back to the original item after placing crystals")
            .setParentSetting(place)
            .setVisibility(() -> placePerform.getValue().equals(Perform.KEEP));

    public static Setting<Double> placeRange = new Setting<>("Range", 5.0D, 1.0D, 7.0D, 0.1D)
            .setDescription("The furthest distance a crystal can be placed")
            .setParentSetting(place);

    public static Setting<Boolean> multiplace = new Setting<>("Multiplace", false)
            .setDescription("Allow placing multiple crystals at different positions")
            .setParentSetting(place);

    public static Setting<Double> placeMinimum = new Setting<>("Minimum", 4.0D, 1.0D, 36.0D, 1D)
            .setDescription("The minimum damage the crystal must deal to place")
            .setParentSetting(place);

    public static Setting<Double> placeMaximum = new Setting<>("Maximum", 10.0D, 1.0D, 36.0D, 1D)
            .setDescription("The maximum damage the crystal can deal to you to place")
            .setParentSetting(place);

    public static Setting<Raytrace> placeRaytrace = new Setting<>("Raytrace", Raytrace.HALF)
            .setDescription("The raytrace method to use when placing crystals")
            .setParentSetting(place);

    public static Setting<PlaceMode> placeMode = new Setting<>("Place", PlaceMode.VANILLA)
            .setDescription("How to place the crystal")
            .setParentSetting(place);

    public static Setting<Boolean> placeSwing = new Setting<>("Swing", true)
            .setDescription("Whether to swing the item when placing")
            .setParentSetting(place);

    // EXPLODE

    public static Setting<Boolean> explode = new Setting<>("Explode", true)
            .setDescription("Whether to explode crystals");

    public static Setting<Float> explodeDelay = new Setting<>("Delay", 50f, 0f, 1000f, 1f)
            .setDescription("The delay between exploding crystals")
            .setParentSetting(explode);

    public static Setting<Double> explodeRange = new Setting<>("Range", 5.0D, 1.0D, 7.0D, 0.1D)
            .setDescription("The furthest distance a crystal can be exploded away from you")
            .setParentSetting(explode);

    public static Setting<Float> explodeTicks = new Setting<>("Ticks", 1f, 0f, 5f, 1f)
            .setDescription("The number of ticks the crystal has to have existed before exploding")
            .setParentSetting(explode);

    public static Setting<Double> explodeMinimum = new Setting<>("Minimum", 4.0D, 1.0D, 36.0D, 1D)
            .setDescription("The minimum damage the crystal must deal to explode")
            .setParentSetting(explode);

    public static Setting<Double> explodeMaximum = new Setting<>("Maximum", 10.0D, 1.0D, 36.0D, 1D)
            .setDescription("The maximum damage the crystal can deal to you to explode")
            .setParentSetting(explode);

    public static Setting<ExplodeMode> explodeMode = new Setting<>("Explode", ExplodeMode.VANILLA)
            .setDescription("How to explode the crystal")
            .setParentSetting(explode);

    public static Setting<Sync> sync = new Setting<>("Sync", Sync.ATTACK)
            .setDescription("When to sync the explosion")
            .setParentSetting(explode);

    // PAUSE

    public static Setting<Boolean> pause = new Setting<>("Pause", true)
            .setDescription("Pause when certain things are occurring");

    public static Setting<Boolean> eating = new Setting<>("Eating", true)
            .setDescription("Pause when eating")
            .setParentSetting(pause);

    public static Setting<Boolean> drinking = new Setting<>("Drinking", true)
            .setDescription("Pause when drinking")
            .setParentSetting(pause);

    public static Setting<Boolean> lowHealth = new Setting<>("LowHealth", true)
            .setDescription("Pause when health is low")
            .setParentSetting(pause);

    public static Setting<Float> healthAmount = new Setting<>("HealthAmount", 10f, 1f, 20f, 1f)
            .setDescription("The amount of health needed to pause")
            .setParentSetting(lowHealth)
            .setVisibility(lowHealth::getValue);

    public static Setting<Boolean> mending = new Setting<>("Mending", true)
            .setDescription("Pause when mending")
            .setParentSetting(pause);

    public static Setting<Double> pauseTicks = new Setting<>("Ticks", 0D, 0D, 5D, 1D)
            .setDescription("The number of ticks to pause between performing actions")
            .setParentSetting(pause);


    // OVERRIDE

    public static Setting<Boolean> override = new Setting<>("Override", true)
            .setDescription("Override the minimum damage settings when certain things are occurring");

    public static Setting<Boolean> overrideHealth = new Setting<>("Health", true)
            .setDescription("Override when the target's health is low")
            .setParentSetting(override);

    public static Setting<Float> overrideHealthAmount = new Setting<>("HealthAmount", 10f, 1f, 20f, 1f)
            .setDescription("The amount of health needed to override")
            .setParentSetting(overrideHealth)
            .setVisibility(overrideHealth::getValue);

    public static Setting<Boolean> overrideArmour = new Setting<>("Armour", true)
            .setDescription("Override when the target's armour is low")
            .setParentSetting(override);

    public static Setting<Float> armourDurability = new Setting<>("Durability", 100f, 0f, 200f, 1f)
            .setDescription("Override when one of the target's armour pieces has less than this durability")
            .setParentSetting(override)
            .setVisibility(overrideArmour::getValue);

    public static Setting<Bind> forceOverride = new Setting<>("ForceOverride", new Bind(0, Bind.Device.KEYBOARD))
            .setDescription("Force the override to be used")
            .setParentSetting(override);

    public static Setting<Boolean> maxIgnore = new Setting<>("MaxIgnore", true)
            .setDescription("Ignore the maximum damage settings when certain things are occurring")
            .setParentSetting(override);


    // ROTATIONS

    public static Setting<Rotate> rotate = new Setting<>("Rotate", Rotate.PACKET)
            .setDescription("Rotate the player");

    public static Setting<Boolean> rotateBack = new Setting<>("Back", true)
            .setDescription("Whether to rotate back to your original original after rotating")
            .setParentSetting(rotate);

    // RENDER

    public static Setting<Boolean> render = new Setting<>("Render", true)
            .setDescription("Whether to render the placement position");

    public static Setting<Render> renderMode = new Setting<>("Mode", Render.BOTH)
            .setDescription("The render mode to use")
            .setParentSetting(render);

    public static Setting<Float> renderOutlineWidth = new Setting<>("Width", 1.0F, 0.0F, 5.0F, 0.1F)
            .setDescription("The width of the outline")
            .setParentSetting(render)
            .setVisibility(() -> !renderMode.getValue().equals(Render.FILL));

    public static Setting<Color> renderColour = new Setting<>("Colour", new Color(185, 17, 255))
            .setDescription("The colour of the render")
            .setParentSetting(render);

    public static Setting<Color> renderOutlineColour = new Setting<>("OutlineColour", new Color(185, 17, 255))
            .setDescription("The colour of the render")
            .setParentSetting(render)
            .setVisibility(() -> !renderMode.getValue().equals(Render.FILL));

    public static Setting<Text> renderText = new Setting<>("Text", Text.BOTH)
            .setDescription("The text to render")
            .setParentSetting(render);

    private BlockPos placementPosition;
    private final Timer placeTimer = new Timer();
    private final Timer explodeTimer = new Timer();
    private float placementDamage;

    private int originalSlot = -1;

    private final Map<BlockPos, Float> renderPositions = new ConcurrentHashMap<>();

    private State state = null;

    private int passedTicks = 0;

    public AutoCrystalRewrite() {
        super("AutoCrystalRewrite", Category.COMBAT, "Automatically places and explodes ender crystals");

        INSTANCE = this;
    }

    @Override
    public void onDisable() {
        state = null;
        renderPositions.clear();

        if (originalSlot != -1 && swapBack.getValue()) {
            mc.player.inventory.currentItem = originalSlot;
            originalSlot = -1;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onTick(TickEvent.ClientTickEvent event) {
        if (nullCheck() || pause.getValue() && (eating.getValue() && PlayerUtil.isPlayerEating() || drinking.getValue() && PlayerUtil.isPlayerDrinking() || lowHealth.getValue() && EntityUtil.getEntityHealth(mc.player) <= healthAmount.getValue() || mending.getValue() && mc.player.isHandActive() && mc.player.getActiveItemStack().getItem().equals(Items.EXPERIENCE_BOTTLE))) {
            return;
        }

        if (pause.getValue()) {
            if (passedTicks < pauseTicks.getValue()) {
                passedTicks++;
                return;
            } else {
                passedTicks = 0;
            }
        }

        order.getValue().run();
    }

    @Override
    public void onRender3D() {
        if (render.getValue() && placementPosition != null) {
            renderPositions.forEach((pos, factor) -> {

                // Block bounding box
                AxisAlignedBB bb = BlockUtil.getBlockBox(pos);

                // Render values
                double x = bb.minX + (bb.maxX - bb.minX) / 2;
                double y = bb.minY + (bb.maxY - bb.minY) / 2;
                double z = bb.minZ + (bb.maxZ - bb.minZ) / 2;

                double sizeX = factor * (bb.maxX - x);
                double sizeY = factor * (bb.maxY - y);
                double sizeZ = factor * (bb.maxZ - z);

                // The bounding box we will highlight
                AxisAlignedBB highlightBB = new AxisAlignedBB(x - sizeX, y - sizeY, z - sizeZ, x + sizeX, y + sizeY, z + sizeZ);

                // Draw the highlight
                switch (renderMode.getValue()) {
                    case FILL:
                        RenderUtil.drawFilledBox(highlightBB, ColourUtil.integrateAlpha(renderColour.getValue(), renderColour.getValue().getAlpha() * factor));
                        break;

                    case OUTLINE:
                        RenderUtil.drawBoundingBox(highlightBB, renderOutlineWidth.getValue(), renderOutlineColour.getValue());
                        break;

                    case BOTH:
                        System.out.println(renderColour.getAlpha());
                        RenderUtil.drawFilledBox(highlightBB, ColourUtil.integrateAlpha(renderColour.getValue(), renderColour.getAlpha() * factor));
                        RenderUtil.drawBoundingBox(highlightBB, renderOutlineWidth.getValue(), renderOutlineColour.getValue());
                        break;
                }

                // Draw the text
                if (!renderText.getValue().equals(Text.NONE) && pos.equals(placementPosition)) {
                    switch (renderText.getValue()) {
                        case TARGET:
                            RenderUtil.drawNametagText((int) placementDamage + "", new Vec3d(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f), -1);
                            break;

                        case SELF:
                            RenderUtil.drawNametagText("" + (int) calculateDamage(new Vec3d(pos.getX(), pos.getY() + 1, pos.getZ()), mc.player), new Vec3d(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f), -1);
                            break;

                        case BOTH:
                            RenderUtil.drawNametagText((int) placementDamage + "", new Vec3d(pos.getX() + 0.5f, pos.getY() + 0.75f, pos.getZ() + 0.5f), -1);
                            RenderUtil.drawNametagText("" + (int) calculateDamage(new Vec3d(pos.getX(), pos.getY() + 1, pos.getZ()), mc.player), new Vec3d(pos.getX() + 0.5f, pos.getY() + 0.4f, pos.getZ() + 0.5f), -1);
                            break;
                    }
                }

                if (pos.getX() == placementPosition.getX() && pos.getY() == placementPosition.getY() && pos.getZ() == placementPosition.getZ()) {
                    renderPositions.put(pos, MathHelper.clamp(factor + 0.025f, 0f, 1f));
                } else {
                    renderPositions.put(pos, factor - 0.025f);
                }
            });
        }

        renderPositions.values().removeIf(factor -> factor <= 0);
    }

    @Listener
    public void onPacketReceive(PacketEvent.PreReceive event) {
        if (event.getPacket() instanceof SPacketSoundEffect && sync.getValue().equals(Sync.SOUND)) {
            // Get packet
            SPacketSoundEffect packet = (SPacketSoundEffect) event.getPacket();

            // Check it's an explosion sound
            if (packet.getSound().equals(SoundEvents.ENTITY_GENERIC_EXPLODE) && packet.getCategory().equals(SoundCategory.BLOCKS)) {
                // Iterate through loaded entities
                for (Entity entity : mc.world.loadedEntityList) {
                    // If the entity isn't an ender crystal, or it is dead, ignore
                    if (!(entity instanceof EntityEnderCrystal) || entity.isDead) {
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

    /**
     * Finds the best target
     *
     * @param positions A list of positions to sort for damage
     * @param positionOffset The Y offset for positions
     *
     * @return The best target
     */
    private EntityLivingBase findTarget(ArrayList<BlockPos> positions, float positionOffset) {
        List<EntityLivingBase> validEntities = mc.world.loadedEntityList.stream().filter(entity -> !entity.isDead && mc.player.getDistance(entity) <= targetRange.getValue() && EntityUtil.isEntityAllowed(entity, players.getValue(), mobs.getValue(), animals.getValue())).sorted(Comparator.comparingDouble(entity -> {
            switch (targeting.getValue()) {
                case DISTANCE:
                    return mc.player.getDistance(entity);

                case HEALTH:
                    return ((EntityLivingBase) entity).getHealth();

                case DAMAGE:
                    positions.sort(Comparator.comparingDouble(pos -> calculateDamage(new Vec3d(pos.getX() + 0.5, pos.getY() + positionOffset, pos.getZ() + 0.5), (EntityLivingBase) entity)));

                    return calculateDamage(new Vec3d(positions.get(0).getX() + 0.5, positions.get(0).getY() + positionOffset, positions.get(0).getZ() + 0.5), (EntityLivingBase) entity);
            }

            return 0;
        })).map(EntityLivingBase.class::cast).collect(Collectors.toList());

        if (validEntities.isEmpty()) {
            return null;
        }

        return validEntities.get(0);
    }

    private void explodeCrystals() {
        if (!explode.getValue() || !explodeTimer.hasMSPassed(explodeDelay.getValue()) || timing.getValue().equals(Timing.SEQUENTIAL) && state.equals(State.PLACING)) {
            return;
        }

        ArrayList<BlockPos> positions = getExplodeableCrystals();

        if (positions.isEmpty()) {
            return;
        }

        EntityLivingBase target = findTarget(positions, 0.5f);

        if (target == null) {
            return;
        }

        positions.removeIf(pos -> {
            if (calculateDamage(new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5), target) < placeMinimum.getValue()) {
                if (!shouldOverride(target)) {
                    return true;
                }
            }

            if (calculateDamage(new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5), mc.player) > placeMaximum.getValue()) {
                return !(shouldOverride(target) && maxIgnore.getValue());
            }

            return false;
        });

        if (positions.isEmpty()) {
            return;
        }

        positions.sort(Comparator.comparingDouble(pos -> calculateDamage(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), target)));

        EntityEnderCrystal crystal = mc.world.getEntitiesWithinAABB(EntityEnderCrystal.class, new AxisAlignedBB(positions.get(0))).get(0);

        if (crystal == null) {
            return;
        }

        Vec2f playerRotation = new Vec2f(mc.player.rotationYaw, mc.player.rotationPitch);
        Vec2f rotation = RotationUtil.getRotationToVec3d(new Vec3d(crystal.posX, crystal.posY + 0.5, crystal.posZ));

        rotate(rotation);

        switch (explodeMode.getValue()) {
            case VANILLA:
                mc.playerController.attackEntity(mc.player, crystal);
                break;

            case PACKET:
                mc.player.connection.sendPacket(generateInstantHit(crystal.getEntityId()));
                break;
        }

        mc.player.resetCooldown();

        if (rotateBack.getValue()) {
            rotate(playerRotation);
        }

        if (sync.getValue().equals(Sync.ATTACK)) {
            crystal.setDead();
        }

        explodeTimer.reset();
    }

    private void placeCrystals() {
        if (!place.getValue() || !placeTimer.hasMSPassed(placeDelay.getValue() / 2) || timing.getValue().equals(Timing.SEQUENTIAL) && state.equals(State.EXPLODING)) {
            return;
        }

        ArrayList<BlockPos> placeablePositions = BlockUtil.getSphere(placeRange.getValue().floatValue(), true).stream().filter(this::isPlaceable).collect(Collectors.toCollection(ArrayList::new));

        EntityLivingBase target = findTarget(placeablePositions, 1);

        if (target == null) {
            placementPosition = null;
            placementDamage = 0;
            return;
        }

        BlockPos placement = findBestPlacement(target, placeablePositions);

        if (placement == null) {
            placementPosition = null;
            placementDamage = 0;
            return;
        }

        placementPosition = placement;
        placementDamage = Math.round(calculateDamage(new Vec3d(placement.getX() + 0.5, placement.getY() + 1, placement.getZ() + 0.5), target));

        if (!renderPositions.containsKey(placement)) {
            renderPositions.put(placement, 0f);
        }

        int previousSlot = mc.player.inventory.currentItem;

        // For silent switch
        boolean alreadyHolding = false;

        EnumHand crystalHand = null;

        switch (placePerform.getValue()) {
            case HOLDING:
                if (!InventoryUtil.isHolding(Items.END_CRYSTAL)) {
                    return;
                }

                crystalHand = InventoryUtil.getHandHolding(Items.END_CRYSTAL);

                break;

            case SILENT_SWITCH:
            case KEEP:
                int crystalSlot = InventoryUtil.getItemInHotbar(Items.END_CRYSTAL);

                if (crystalSlot > -1) {
                    if (swapBack.getValue() && originalSlot == -1) {
                        originalSlot = mc.player.inventory.currentItem;
                    }

                    if (InventoryUtil.isHolding(Items.END_CRYSTAL, EnumHand.MAIN_HAND)) {
                        alreadyHolding = true;
                    }

                    else {
                        mc.player.inventory.currentItem = crystalSlot;
                        ((IPlayerControllerMP) mc.playerController).hookSyncCurrentPlayItem();
                    }

                    crystalHand = EnumHand.MAIN_HAND;
                }

                else if (InventoryUtil.isHolding(Items.END_CRYSTAL, EnumHand.OFF_HAND)) {
                    crystalHand = EnumHand.OFF_HAND;
                }

                else {
                    return;
                }

                break;
        }

        if (crystalHand == null) {
            return;
        }

        double eye = mc.player.posY + mc.player.eyeHeight;

        EnumFacing facing = EnumFacing.UP;
        float distance = 0;

        Vec3d facingVec = new Vec3d(0.5, 0.5, 0.5);
        Vec3d raytraceVector = new Vec3d(placement).addVector(0.5, 0.5, 0.5);

        if (!placeRaytrace.getValue().equals(Raytrace.NONE)) {
            if (placement.getY() > eye) {
                for (float x = 0; x <= 1; x += placeRaytrace.getValue().getIncrease()) {
                    for (float y = 0; y <= 1; y += placeRaytrace.getValue().getIncrease()) {
                        for (float z = 0; z <= 1; z += placeRaytrace.getValue().getIncrease()) {
                            Vec3d vector = new Vec3d(placement).addVector(x, y, z);

                            RayTraceResult result = mc.world.rayTraceBlocks(mc.player.getPositionEyes(mc.getRenderPartialTicks()), vector, false, true, false);

                            if (result != null && result.typeOfHit.equals(RayTraceResult.Type.BLOCK) && result.getBlockPos().equals(placement)) {
                                double vectorDistance = vector.distanceTo(mc.player.getPositionEyes(mc.getRenderPartialTicks()));

                                if (vectorDistance < distance || distance == 0) {
                                    distance = (float) vectorDistance;
                                    facing = result.sideHit;

                                    raytraceVector = vector;
                                }
                            }
                        }
                    }
                }
            }
        }

        Vec2f playerRotation = new Vec2f(mc.player.rotationYaw, mc.player.rotationPitch);
        Vec2f rotation = RotationUtil.getRotationToVec3d(raytraceVector);

        rotate(rotation);

        if (placeMode.getValue().equals(PlaceMode.VANILLA)) {
            if (mc.playerController.processRightClickBlock(mc.player, mc.world, placement, facing, facingVec, crystalHand).equals(EnumActionResult.SUCCESS)) {
                if (placeSwing.getValue()) {
                    mc.player.swingArm(crystalHand);
                }
            }
        } else if (placeMode.getValue().equals(PlaceMode.PACKET)) {
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(placement, facing, crystalHand, (float) facingVec.x, (float) facingVec.y, (float) facingVec.z));

            if (placeSwing.getValue()) {
                mc.player.swingArm(crystalHand);
            }
        }

        if (rotateBack.getValue()) {
            rotate(playerRotation);
        }

        if (placePerform.getValue().equals(Perform.SILENT_SWITCH) && !alreadyHolding) {
            mc.player.inventory.currentItem = previousSlot;
            ((IPlayerControllerMP) mc.playerController).hookSyncCurrentPlayItem();
        }

        placeTimer.reset();
    }

    private BlockPos findBestPlacement(EntityLivingBase target, ArrayList<BlockPos> positions) {
        if (positions.isEmpty()) {
            return null;
        }

        BlockPos placement = null;

        float placementDamage = 0;

        for (BlockPos pos : positions) {
            float positionDamage = calculateDamage(new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5), target);

            if (positionDamage < placeMinimum.getValue()) {
                if (!shouldOverride(target)) {
                    continue;
                }
            }

            if (calculateDamage(new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5), mc.player) > placeMaximum.getValue()) {
                if (!(shouldOverride(target) && maxIgnore.getValue())) {
                    continue;
                }
            }

            if (positionDamage >= 1 && positionDamage > placementDamage) {
                placement = pos;
                placementDamage = positionDamage;
            }
        }

        return placement;
    }

    // UTILS

    private boolean isPlaceable(BlockPos pos) {
        BlockPos offset = new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ());
        BlockPos offset2 = new BlockPos(pos.getX(), pos.getY() + 2, pos.getZ());

        if (BlockUtil.getBlockAtPos(pos) != Blocks.BEDROCK && BlockUtil.getBlockAtPos(pos) != Blocks.OBSIDIAN) {
            return false;
        }

        if (BlockUtil.getBlockAtPos(offset) != Blocks.AIR || BlockUtil.getBlockAtPos(offset2) != Blocks.AIR) {
            return false;
        }

        for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(offset.getX(), offset.getY(), offset.getZ(), offset.getX() + 1, offset.getY() + 1, offset.getZ() + 1))) {
            if (entity.isDead) {
                continue;
            }

            if (entity instanceof EntityEnderCrystal && !multiplace.getValue()) {
                continue;
            }

            return false;
        }

        return true;
    }

    /**
     * Calculates the explosion damage based on a Vec3D
     * @param vec The vector to calculate damage from
     * @param entity The target
     * @return The damage done to the target
     */
    public float calculateDamage(Vec3d vec, EntityLivingBase entity) {
        float doubleExplosionSize = 12.0F;
        double distancedSize = entity.getDistance(vec.x, vec.y, vec.z) / (double) doubleExplosionSize;
        double blockDensity = entity.world.getBlockDensity(new Vec3d(vec.x, vec.y, vec.z), entity.getEntityBoundingBox());
        double v = (1.0D - distancedSize) * blockDensity;
        float damage = (float) ((int) ((v * v + v) / 2.0D * 7.0D * (double) doubleExplosionSize + 1.0D));

        int diff = mc.world.getDifficulty().getDifficultyId();

        return getBlastReduction(entity, damage * (diff == 0 ? 0 : (diff == 2 ? 1 : (diff == 1 ? 0.5f : 1.5f))), new Explosion(mc.world, null, vec.x, vec.y, vec.z, 6F, false, true));
    }

    /**
     * Gets the blast reduction
     * @param entity The entity to calculate damage for
     * @param damage The original damage
     * @param explosion The explosion
     * @return The blast reduction
     */
    private float getBlastReduction(EntityLivingBase entity, float damage, Explosion explosion) {
        DamageSource source = DamageSource.causeExplosionDamage(explosion);
        damage = CombatRules.getDamageAfterAbsorb(damage, (float) entity.getTotalArmorValue(), (float) entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());

        try {
            int enchantmentModifier = MathHelper.clamp(EnchantmentHelper.getEnchantmentModifierDamage(entity.getArmorInventoryList(), source), 0, 20);
            damage *= 1.0F - enchantmentModifier / 25.0F;

            if (entity.isPotionActive(MobEffects.WEAKNESS)) {
                damage = damage - (damage / 4);
            }
        } catch (NullPointerException exception) {
            exception.printStackTrace();
        }

        return Math.max(damage, 0.0F);
    }

    private ArrayList<BlockPos> getExplodeableCrystals() {
        ArrayList<BlockPos> explodeableCrystals = new ArrayList<>();

        for (EntityEnderCrystal entity : mc.world.loadedEntityList.stream().filter(EntityEnderCrystal.class::isInstance).map(EntityEnderCrystal.class::cast).collect(Collectors.toList())) {
            if (mc.player.getDistance(entity) >= explodeRange.getValue() || entity.ticksExisted <= explodeTicks.getValue() || entity.isDead) {
                continue;
            }

            explodeableCrystals.add(entity.getPosition());
        }

        return explodeableCrystals;
    }

    private CPacketUseEntity generateInstantHit(int entityID) {
        CPacketUseEntity packet = new CPacketUseEntity();

        ((ICPacketUseEntity) packet).setEntityID(entityID);
        ((ICPacketUseEntity) packet).setAction(CPacketUseEntity.Action.ATTACK);

        return packet;
    }

    private boolean shouldOverride(EntityLivingBase target) {
        if (override.getValue()) {
            // isPressed() doesn't work :(
            if (forceOverride.getValue().getButtonCode() != 0) {
                switch (forceOverride.getValue().getDevice()) {
                    case KEYBOARD:
                        if (Keyboard.isKeyDown(forceOverride.getValue().getButtonCode())) {
                            return true;
                        }

                        break;

                    case MOUSE:
                        if (Mouse.isButtonDown(forceOverride.getValue().getButtonCode())) {
                            return true;
                        }

                        break;
                }
            }

            if (overrideArmour.getValue()) {
                float lowest = armourDurability.getValue() + 1;

                // Iterate through target's armour
                for (ItemStack armourPiece : target.getArmorInventoryList()) {
                    // If it is an actual piece of armour
                    if (armourPiece != null && armourPiece.getItem() != Items.AIR) {
                        // Get durability
                        float durability = (armourPiece.getMaxDamage() - armourPiece.getItemDamage());

                        // If it is less than the last lowest, set the lowest to this durability
                        if (durability < lowest) {
                            lowest = durability;
                        }
                    }
                }

                // We are overriding if the lowest durability is less or equal to the total armour value setting
                if (lowest <= armourDurability.getValue()) {
                    return true;
                }
            }

            if (overrideHealth.getValue()) {
                return EntityUtil.getEntityHealth(target) <= overrideHealthAmount.getValue();
            }
        }

        return false;
    }

    public void rotate(Vec2f rotation) {
        if (!rotate.getValue().equals(Rotate.NONE)) {
            mc.player.connection.sendPacket(new CPacketPlayer.Rotation(rotation.x, rotation.y, mc.player.onGround));

            if (rotate.getValue().equals(Rotate.LEGIT)) {
                mc.player.rotationYaw = rotation.x;
                mc.player.rotationPitch = rotation.y;
            }
        }
    }

    @Override
    public String getData() {
        return placementDamage == 0 ? "" : " " + placementDamage;
    }

    public enum Order {
        /**
         * Explodes ender crystals then places them
         */
        EXPLODE_PLACE(() -> {
            if (AutoCrystalRewrite.INSTANCE.state == null) {
                AutoCrystalRewrite.INSTANCE.state = State.EXPLODING;
            }

            INSTANCE.explodeCrystals();
            INSTANCE.placeCrystals();

            AutoCrystalRewrite.INSTANCE.state = AutoCrystalRewrite.INSTANCE.state == State.EXPLODING ? State.PLACING : State.EXPLODING;
        }),

        /**
         * Places ender crystals then explodes them
         */
        PLACE_EXPLODE(() -> {
            if (AutoCrystalRewrite.INSTANCE.state == null) {
                AutoCrystalRewrite.INSTANCE.state = State.PLACING;
            }

            INSTANCE.placeCrystals();
            INSTANCE.explodeCrystals();

            AutoCrystalRewrite.INSTANCE.state = AutoCrystalRewrite.INSTANCE.state == State.EXPLODING ? State.PLACING : State.EXPLODING;
        });

        private final Runnable function;

        Order(Runnable run) {
            this.function = run;
        }

        public void run() {
            function.run();
        }
    }

    public enum TargetSort {
        /**
         * Sorts targets by distance
         */
        DISTANCE,

        /**
         * Sorts targets by health
         */
        HEALTH,

        /**
         * Sorts targets by amount of damage we can do to them
         */
        DAMAGE
    }

    public enum Timing {
        /**
         * Perform actions on the same tick
         */
        LINEAR,

        /**
         * Spread actions across two ticks
         */
        SEQUENTIAL
    }

    public enum Perform {
        /**
         * Perform when holding crystals
         */
        HOLDING,

        /**
         * Switch to crystals to perform
         */
        KEEP,

        /**
         * Switch to crystals, then switch back once completed
         */
        SILENT_SWITCH
    }

    public enum PlaceMode {
        /**
         * Use a vanilla method
         */
        VANILLA,

        /**
         * Send a packet
         */
        PACKET
    }

    public enum ExplodeMode {
        /**
         * Use a vanilla method
         */
        VANILLA,

        /**
         * Send a packet
         */
        PACKET
    }

    public enum Sync {
        /**
         * Sync on attack
         */
        ATTACK,

        /**
         * Sync on sound
         */
        SOUND,

        /**
         * Do not sync
         */
        NEVER
    }

    public enum Raytrace {
        /**
         * Don't raytrace
         */
        NONE(0),

        /**
         * Check every 0.5
         */
        HALF(0.5),

        /**
         * Check every 0.05
         */
        TWENTY(0.05),

        /**
         * Check every 0.01. Can cause lag if there are too many placeable positions
         */
        HUNDRED(0.01);

        private final double increase;

        Raytrace(double increase) {
            this.increase = increase;
        }

        public double getIncrease() {
            return increase;
        }
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

    public enum Text {
        /**
         * Draw target damage
         */
        TARGET,

        /**
         * Draw self damage
         */
        SELF,

        /**
         * Draw both
         */
        BOTH,

        /**
         * No text
         */
        NONE
    }

    public enum State {
        /**
         * Placing crystals
         */
        PLACING,

        /**
         * Exploding crystals
         */
        EXPLODING
    }

}