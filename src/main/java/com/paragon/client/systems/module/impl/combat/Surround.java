package com.paragon.client.systems.module.impl.combat;

import com.paragon.Paragon;
import com.paragon.api.util.player.InventoryUtil;
import com.paragon.api.util.player.RotationUtil;
import com.paragon.api.util.render.ColourUtil;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.world.BlockUtil;
import com.paragon.client.managers.rotation.Rotate;
import com.paragon.api.module.Module;
import com.paragon.api.module.Category;
import com.paragon.api.setting.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextFormatting;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Wolfsurge
 * @since 08/05/2022
 */
public class Surround extends Module {

    public static Surround INSTANCE;

    // General settings
    public static Setting<Disable> disable = new Setting<>("Disable", Disable.NEVER)
            .setDescription("When to automatically disable the module");

    public static Setting<Double> blocksPerTick = new Setting<>("BlocksPerTick", 4D, 1D, 10D, 1D)
            .setDescription("The maximum amount of blocks to be placed per tick");

    public static Setting<Center> center = new Setting<>("Center", Center.MOTION)
            .setDescription("Center the player on the block when enabled");

    public static Setting<Air> air = new Setting<>("Air", Air.SUPPORT)
            .setDescription("Place blocks beneath where we are placing");

    // Rotate settings
    public static Setting<Rotate> rotate = new Setting<>("Rotate", Rotate.LEGIT)
            .setDescription("How to rotate the player");

    public static Setting<Boolean> rotateBack = new Setting<>("RotateBack", true)
            .setDescription("Rotate the player back to their original rotation")
            .setParentSetting(rotate);

    // Render
    public static Setting<Boolean> render = new Setting<>("Render", true)
            .setDescription("Render a highlight on the positions we need to place blocks at");

    public static Setting<Color> renderColour = new Setting<>("Colour", new Color(185, 17, 255, 130))
            .setDescription("The colour of the highlight")
            .setParentSetting(render);

    // Map of blocks to render
    private Map<BlockPos, EnumFacing> renderBlocks = new HashMap<>();

    public Surround() {
        super("Surround", Category.COMBAT, "Places obsidian around you to protect you from crystals");

        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        if (nullCheck()) {
            return;
        }

        // No obsidian to place
        if (InventoryUtil.getHotbarBlockSlot(Blocks.OBSIDIAN) == -1) {
            return;
        }

        // Center player
        switch (center.getValue()) {
            case MOTION:
                // Set player's motion to walk to the center of the block
                mc.player.motionX = ((MathHelper.floor(mc.player.posX) + 0.5D) - mc.player.posX) / 2;
                mc.player.motionZ = ((MathHelper.floor(mc.player.posZ) + 0.5D) - mc.player.posZ) / 2;
                break;

            case SNAP:
                // Send movement packet
                mc.player.connection.sendPacket(new CPacketPlayer.Position(MathHelper.floor(mc.player.posX) + 0.5, mc.player.posY, MathHelper.floor(mc.player.posZ) + 0.5, mc.player.onGround));

                // Set position client-side
                mc.player.setPosition(MathHelper.floor(mc.player.posX) + 0.5, mc.player.posY, MathHelper.floor(mc.player.posZ) + 0.5);
                break;
        }

    }

    @Override
    public void onTick() {
        if (nullCheck()) {
            return;
        }

        // Check we are in air
        if (disable.getValue().equals(Disable.AIR) && !mc.player.onGround) {
            toggle();
            return;
        }

        // We don't have obsidian in our hotbar
        if (InventoryUtil.getHotbarBlockSlot(Blocks.OBSIDIAN) == -1) {
            Paragon.INSTANCE.getCommandManager().sendClientMessage(TextFormatting.RED + "No obsidian available, Surround disabled!", false);
            toggle();
            return;
        }

        // Blocks we need to place
        Map<BlockPos, EnumFacing> blocks = new HashMap<>();

        // The player's position
        BlockPos playerPos = new BlockPos(MathHelper.floor(mc.player.posX), MathHelper.floor(mc.player.posY), MathHelper.floor(mc.player.posZ));

        for (int x = -1; x <= 1; x++) {
            // Our X position
            if (x == 0) {
                continue;
            }

            // Get position
            BlockPos pos = playerPos.add(x, 0, 0);

            // Check if we need to support the block
            if (canPlaceBlock(pos.down()) && air.getValue().equals(Air.SUPPORT)) {
                // Get offset
                for (EnumFacing facing : EnumFacing.values()) {
                    // Make sure we can place on offset
                    if (BlockUtil.getBlockAtPos(pos.down().offset(facing)) != Blocks.AIR) {
                        // Add block
                        blocks.put(pos.down(), facing);
                        break;
                    }
                }
            }

            // We can place the block or we are supporting it
            if (canPlaceBlock(pos) || air.getValue().equals(Air.SUPPORT) && canPlaceBlock(pos.down())) {
                // Get offset
                for (EnumFacing facing : EnumFacing.values()) {
                    // Make sure we can place on offset
                    if (BlockUtil.getBlockAtPos(pos.offset(facing)) != Blocks.AIR) {
                        // Add block
                        blocks.put(pos, facing);
                        break;
                    }
                }
            }

            for (int z = -1; z <= 1; z++) {
                // Our Z position
                if (z == 0) {
                    continue;
                }

                // Get position
                pos = playerPos.add(0, 0, z);

                // Check if we need to support the block
                if (canPlaceBlock(pos.down()) && air.getValue().equals(Air.SUPPORT)) {
                    // Get offset
                    for (EnumFacing facing : EnumFacing.values()) {
                        // Make sure we can place on offset
                        if (BlockUtil.getBlockAtPos(pos.down().offset(facing)) != Blocks.AIR) {
                            // Add block
                            blocks.put(pos.down(), facing);
                            break;
                        }
                    }
                }

                // We can place the block or we are supporting it
                if (canPlaceBlock(pos) || air.getValue().equals(Air.SUPPORT) && canPlaceBlock(pos.down())) {
                    // Get offset
                    for (EnumFacing facing : EnumFacing.values()) {
                        // Make sure we can place on offset
                        if (BlockUtil.getBlockAtPos(pos.offset(facing)) != Blocks.AIR) {
                            // Add block
                            blocks.put(pos, facing);
                            break;
                        }
                    }
                }
            }
        }

        // Set render blocks
        this.renderBlocks = blocks;

        // Get our original rotation
        Vec2f originalRotation = new Vec2f(mc.player.rotationYaw, mc.player.rotationPitch);

        // Place blocks until either:
        // A. We run out of blocks
        // B. We have placed the maximum amount of blocks for this tick
        for (double i = 0; i < blocksPerTick.getValue() && i < blocks.size(); i += 1) {
            // Get position
            BlockPos pos = blocks.keySet().toArray(new BlockPos[0])[(int) i];

            // Get facing
            EnumFacing facing = blocks.get(pos);

            // Get rotation
            Vec2f rotation = RotationUtil.getRotationToBlockPos(pos, 0.5);

            // Rotate to position
            if (rotate.getValue().equals(Rotate.LEGIT)) {
                mc.player.rotationYaw = rotation.x;
                mc.player.rotationPitch = rotation.y;
                mc.player.rotationYawHead = rotation.x;
            } else if (rotate.getValue().equals(Rotate.PACKET)) {
                mc.player.connection.sendPacket(new CPacketPlayer.Rotation(rotation.x, rotation.y, mc.player.onGround));
            }

            // Get current item
            int slot = mc.player.inventory.currentItem;

            // Slot to switch to
            int obsidianSlot = InventoryUtil.getHotbarBlockSlot(Blocks.OBSIDIAN);

            if (obsidianSlot != -1) {
                // Switch
                mc.player.inventory.currentItem = obsidianSlot;

                // Make the server think we are crouching, so we can place on interactable blocks (e.g. chests, furnaces, etc.)
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));

                // Place block
                mc.playerController.processRightClickBlock(mc.player, mc.world, pos.offset(facing), facing.getOpposite(), new Vec3d(pos), EnumHand.MAIN_HAND);

                // Swing hand
                mc.player.swingArm(EnumHand.MAIN_HAND);

                // Stop crouching
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            }

            // Reset slot to our original slot
            mc.player.inventory.currentItem = slot;
        }

        // Rotate back
        if (rotateBack.getValue()) {
            if (rotate.getValue().equals(Rotate.LEGIT)) {
                mc.player.rotationYaw = originalRotation.x;
                mc.player.rotationPitch = originalRotation.y;
                mc.player.rotationYawHead = originalRotation.x;
            } else if (rotate.getValue().equals(Rotate.PACKET)) {
                mc.player.connection.sendPacket(new CPacketPlayer.Rotation(originalRotation.x, originalRotation.y, mc.player.onGround));
            }
        }

        // Toggle if we have placed all the blocks
        if (this.renderBlocks.isEmpty() && disable.getValue().equals(Disable.FINISHED)) {
            toggle();
        }
    }

    @Override
    public void onRender3D() {
        // We want to render
        if (render.getValue()) {
            renderBlocks.forEach((pos, facing) -> {
                // Render highlight
                RenderUtil.drawFilledBox(BlockUtil.getBlockBox(pos), renderColour.getValue());
                RenderUtil.drawBoundingBox(BlockUtil.getBlockBox(pos), 1, ColourUtil.integrateAlpha(renderColour.getValue(), 255));
            });
        }
    }

    public boolean canPlaceBlock(BlockPos pos) {
        // Iterate through entities in the block above
        for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
            // If the entity is dead, continue
            if (entity.isDead) {
                continue;
            }

            return false;
        }

        // Block is air
        if (BlockUtil.getBlockAtPos(pos) != Blocks.AIR) {
            return false;
        }

        // There is a block we can place on
        return BlockUtil.getBlockAtPos(pos.down()) != Blocks.AIR ||
                BlockUtil.getBlockAtPos(pos.up()) != Blocks.AIR ||
                BlockUtil.getBlockAtPos(pos.north()) != Blocks.AIR ||
                BlockUtil.getBlockAtPos(pos.south()) != Blocks.AIR ||
                BlockUtil.getBlockAtPos(pos.east()) != Blocks.AIR ||
                BlockUtil.getBlockAtPos(pos.west()) != Blocks.AIR;
    }

    public enum Disable {
        /**
         * Disable once all blocks have been placed
         */
        FINISHED,

        /**
         * Never disable
         */
        NEVER,

        /**
         * Disable when the player is in air
         */
        AIR
    }

    public enum Center {
        /**
         * Move the player to the center
         */
        MOTION,

        /**
         * Snap the player to the center
         */
        SNAP,

        /**
         * Don't center the player
         */
        OFF
    }

    public enum Air {
        /**
         * Place blocks below air blocks, so we can place on top of them
         */
        SUPPORT,

        /**
         * Do not place on air blocks
         */
        IGNORE
    }

}