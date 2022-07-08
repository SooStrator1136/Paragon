package com.paragon.client.systems.module.impl.combat;

import com.paragon.api.event.network.PacketEvent;
import com.paragon.api.util.calculations.Timer;
import com.paragon.api.util.entity.EntityUtil;
import com.paragon.api.util.player.InventoryUtil;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.world.BlockUtil;
import com.paragon.asm.mixins.accessor.ICPacketUseEntity;
import com.paragon.asm.mixins.accessor.IPlayerControllerMP;
import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.setting.Setting;
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
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.Explosion;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Wolfsurge
 */
public class AutoCrystalRewrite extends Module {

    public static AutoCrystalRewrite INSTANCE;

    public static Setting<Order> order = new Setting<>("Order", Order.PLACE_EXPLODE)
            .setDescription("The order in which to complete actions");

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
            .setParentSetting(place);

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

    public static Setting<ExplodeMode> explodeMode = new Setting<>("Explode", ExplodeMode.VANILLA)
            .setDescription("How to explode the crystal")
            .setParentSetting(explode);

    public static Setting<Sync> sync = new Setting<>("Sync", Sync.ATTACK)
            .setDescription("When to sync the explosion")
            .setParentSetting(explode);

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

    private final Map<BlockPos, Float> renderPositions = new HashMap<>();

    public AutoCrystalRewrite() {
        super("AutoCrystalRewrite", Category.COMBAT, "Automatically places and explodes ender crystals");

        INSTANCE = this;
    }

    @Override
    public void onDisable() {
        renderPositions.clear();

        if (originalSlot != -1 && swapBack.getValue()) {
            mc.player.inventory.currentItem = originalSlot;
            originalSlot = -1;
        }
    }

    @Override
    public void onTick() {
        if (nullCheck()) {
            return;
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
                        RenderUtil.drawFilledBox(highlightBB, renderColour.getValue());
                        break;

                    case OUTLINE:
                        RenderUtil.drawBoundingBox(highlightBB, renderOutlineWidth.getValue(), renderOutlineColour.getValue());
                        break;

                    case BOTH:
                        RenderUtil.drawFilledBox(highlightBB, renderColour.getValue());
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
                }

                else {
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
                        // mc.world.removeEntityFromWorld(entity.getEntityId());
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
        List<EntityLivingBase> validEntities = mc.world.loadedEntityList.stream().filter(entity -> mc.player.getDistance(entity) <= targetRange.getValue() && EntityUtil.isEntityAllowed(entity, players.getValue(), mobs.getValue(), animals.getValue())).sorted(Comparator.comparingDouble(entity -> {
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
        if (!explode.getValue() || !explodeTimer.hasMSPassed(explodeDelay.getValue())) {
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

        positions.sort(Comparator.comparingDouble(pos -> calculateDamage(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), target)));

        EntityEnderCrystal crystal = mc.world.getEntitiesWithinAABB(EntityEnderCrystal.class, new AxisAlignedBB(positions.get(0))).get(0);

        if (crystal == null) {
            return;
        }

        switch (explodeMode.getValue()) {
            case VANILLA:
                mc.playerController.attackEntity(mc.player, crystal);
                break;

            case PACKET:
                mc.player.connection.sendPacket(generateInstantHit(crystal.getEntityId()));
                break;
        }

        if (sync.getValue().equals(Sync.ATTACK)) {
            crystal.setDead();
            // mc.world.removeEntityFromWorld(crystal.getEntityId());
        }

        explodeTimer.reset();
    }

    private void placeCrystals() {
        if (!place.getValue() || !placeTimer.hasMSPassed(placeDelay.getValue() / 2)) {
            return;
        }

        ArrayList<BlockPos> placeablePositions = BlockUtil.getSphere(placeRange.getValue().floatValue(), true).stream().filter(this::isPlaceable).collect(Collectors.toCollection(ArrayList::new));

        EntityLivingBase target = findTarget(placeablePositions, 1);

        if (target == null) {
            return;
        }

        BlockPos placement = findBestPlacement(target, placeablePositions);

        if (placement == null) {
            placementPosition = null;
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

        if (!placeRaytrace.getValue().equals(Raytrace.NONE)) {
            if (placement.getY() > eye) {
                for (float x = 0; x <= 1; x += placeRaytrace.getValue().getIncrease()) {
                    for (float y = 0; y <= 1; y += placeRaytrace.getValue().getIncrease()) {
                        for (float z = 0; z <= 1; z += placeRaytrace.getValue().getIncrease()) {
                            Vec3d vector = new Vec3d(placement).add(x, y, z);

                            RayTraceResult result = mc.world.rayTraceBlocks(mc.player.getPositionEyes(mc.getRenderPartialTicks()), vector, false, true, false);

                            if (result != null && result.typeOfHit.equals(RayTraceResult.Type.BLOCK) && result.getBlockPos().equals(placement)) {
                                double vectorDistance = vector.distanceTo(mc.player.getPositionEyes(mc.getRenderPartialTicks()));

                                if (vectorDistance < distance || distance == 0) {
                                    distance = (float) vectorDistance;
                                    facing = result.sideHit;
                                }
                            }
                        }
                    }
                }
            }
        }

        if (placeMode.getValue().equals(PlaceMode.VANILLA)) {
            mc.playerController.processRightClickBlock(mc.player, mc.world, placement, facing, facingVec, crystalHand);
        }

        else if (placeMode.getValue().equals(PlaceMode.PACKET)) {
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(placement, facing, crystalHand, (float) facingVec.x, (float) facingVec.y, (float) facingVec.z));
        }

        if (placeSwing.getValue()) {
            mc.player.swingArm(crystalHand);
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

            if (positionDamage < placeMinimum.getValue() || calculateDamage(new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5), mc.player) > placeMaximum.getValue()) {
                continue;
            }

            if (positionDamage > placementDamage) {
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

        for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos.up()))) {
            if (entity.isDead || !multiplace.getValue() && entity instanceof EntityEnderCrystal) {
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
    private float calculateDamage(Vec3d vec, EntityLivingBase entity) {
        float doubleExplosionSize = 12.0F;
        double distancedSize = entity.getDistance(vec.x, vec.y, vec.z) / (double) doubleExplosionSize;
        double blockDensity = entity.world.getBlockDensity(new Vec3d(vec.x, vec.y, vec.z), entity.getEntityBoundingBox());
        double v = (1.0D - distancedSize) * blockDensity;
        float damage = (float) ((int) ((v * v + v) / 2.0D * 7.0D * (double) doubleExplosionSize + 1.0D));

        int diff = mc.world.getDifficulty().getId();

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

        int enchantmentModifier = MathHelper.clamp(EnchantmentHelper.getEnchantmentModifierDamage(entity.getArmorInventoryList(), source), 0, 20);
        damage *= 1.0F - enchantmentModifier / 25.0F;

        if (entity.isPotionActive(MobEffects.WEAKNESS)) {
            damage = damage - (damage / 4);
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

    @Override
    public String getData() {
        return placementDamage == 0 ? "" : " " + placementDamage;
    }

    public enum Order {
        /**
         * Explodes ender crystals then places them
         */
        EXPLODE_PLACE(() -> {
            INSTANCE.explodeCrystals();
            INSTANCE.placeCrystals();
        }),

        /**
         * Places ender crystals then explodes them
         */
        PLACE_EXPLODE(() -> {
            INSTANCE.placeCrystals();
            INSTANCE.explodeCrystals();
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
         * Sync on spawn
         */
        SPAWN,

        /**
         * Sync on sound
         */
        SOUND
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
         * Check every 0.01
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

}