package com.paragon.client.systems.module.impl.combat;

import com.paragon.Paragon;
import com.paragon.api.module.Category;
import com.paragon.api.module.Module;
import com.paragon.api.setting.Setting;
import com.paragon.api.util.player.InventoryUtil;
import com.paragon.api.util.player.PlayerUtil;
import com.paragon.api.util.player.RotationUtil;
import com.paragon.api.util.render.ColourUtil;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.render.builder.BoxRenderMode;
import com.paragon.api.util.render.builder.RenderBuilder;
import com.paragon.api.util.world.BlockUtil;
import com.paragon.client.managers.rotation.Rotate;
import com.paragon.client.managers.rotation.Rotation;
import com.paragon.client.managers.rotation.RotationPriority;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

@SideOnly(Side.CLIENT)
public final class HoleFill extends Module {

    private static final Setting<Float> range = new Setting<>("Range", 5.0f, 0.0f, 6.0f, 0.1f)
            .setDescription("The maximum range to fill a hole");

    private static final Setting<Boolean> obsidianBedrock = new Setting<>("Obsidian Bedrock", true)
            .setDescription("Only fill holes that are surrounded by obsidian or bedrock");

    private static final Setting<Float> blocksPerTick = new Setting<>("Blocks Per Tick", 1.0f, 1.0f, 10.0f, 1.0f)
            .setDescription("The amount of blocks to fill per tick");

    private static final Setting<Float> pauseTicks = new Setting<>("Pause Ticks", 2.0f, 0.0f, 10.0f, 1.0f)
            .setDescription("The amount of ticks to wait before filling the next holes");

    // Rotate settings
    public static final Setting<Rotate> rotate = new Setting<>("Rotate", Rotate.LEGIT)
            .setDescription("How to rotate the player");

    private static final Setting<Boolean> rotateBack = new Setting<>("Rotate Back", true)
            .setDescription("Rotate the player back to their original rotation")
            .setParentSetting(rotate);

    // Render settings
    public static final Setting<Boolean> render = new Setting<>("Render", true)
            .setDescription("Render the placement");

    private static final Setting<BoxRenderMode> renderMode = new Setting<>("Mode", BoxRenderMode.BOTH)
            .setDescription("How to render placement")
            .setParentSetting(render);

    private static final Setting<Float> renderOutlineWidth = new Setting<>("Outline Width", 0.5f, 0.1f, 2f, 0.1f)
            .setDescription("The width of the lines")
            .setParentSetting(render);

    private static final Setting<Color> renderColour = new Setting<>("Fill Colour", new Color(185, 19, 255, 130))
            .setDescription("The colour of the fill")
            .setParentSetting(render);

    private static final Setting<Color> renderOutlineColour = new Setting<>("Outline Colour", new Color(185, 19, 255))
            .setParentSetting(render);

    private final Map<BlockPos, EnumFacing> positions = new HashMap<>(5);
    private final Map<BlockPos, Integer> attemptedPositions = new HashMap<>(5);

    private int ticks = 0;

    public HoleFill() {
        super("HoleFill", Category.COMBAT, "Fills safe holes with blocks to prevent opponents from hiding in them");
    }

    @Override
    public void onTick() {
        if (this.nullCheck()) {
            return;
        }

        if (ticks >= pauseTicks.getValue()) {
            ticks = 0;
        } else if (ticks < pauseTicks.getValue()) {
            ticks++;
            return;
        }

        if (PlayerUtil.isMoving()) {
            attemptedPositions.clear();
            return;
        }

        positions.clear();

        List<BlockPos> valid = BlockUtil.getSphere(range.getValue(), false).stream().filter(block ->
                attemptedPositions.getOrDefault(block, 0) < 3
                        && BlockUtil.isSafeHole(block, obsidianBedrock.getValue())
                        && BlockUtil.canSeePos(block)
                        && mc.player.getPosition() != block
        ).sorted(Comparator.comparingDouble(block -> mc.player.getDistanceSq(block))).collect(Collectors.toList());

        Collections.reverse(valid);

        valid.forEach(block -> positions.put(block, BlockUtil.getFacing(block)));

        // Get our original rotation
        Vec2f originalRotation = new Vec2f(mc.player.rotationYaw, mc.player.rotationPitch);

        // Place blocks until either:
        // A. We run out of blocks
        // B. We have placed the maximum amount of blocks for this tick
        for (double i = 0; i < blocksPerTick.getValue() && i < positions.size(); i += 1) {
            // Get position
            BlockPos pos = positions.keySet().toArray(new BlockPos[0])[(int) i];

            // Get facing
            EnumFacing facing = positions.get(pos);

            // Get rotation
            Vec2f rotation = RotationUtil.getRotationToBlockPos(pos, 0.5);

            Paragon.INSTANCE.getRotationManager().addRotation(new Rotation(rotation.x, rotation.y, rotate.getValue(), RotationPriority.HIGH));

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

                attemptedPositions.put(pos, attemptedPositions.getOrDefault(pos, 0) + 1);

                // Swing hand
                mc.player.swingArm(EnumHand.MAIN_HAND);

                // Stop crouching
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            }

            // Reset slot to our original slot
            mc.player.inventory.currentItem = slot;

            // Rotate back
            if (rotateBack.getValue()) {
                Paragon.INSTANCE.getRotationManager().addRotation(new Rotation(originalRotation.x, originalRotation.y, rotate.getValue(), RotationPriority.NORMAL));
            }
        }
    }

    @Override
    public void onRender3D() {
        // Render highlights
        if (render.getValue()) {
            positions.forEach((block, facing) -> {
                new RenderBuilder()
                        .boundingBox(BlockUtil.getBlockBox(block))
                        .outer(ColourUtil.integrateAlpha(renderColour.getValue(), 255f))
                        .type(renderMode.getValue())

                        .start()

                        .blend(true)
                        .depth(true)
                        .texture(true)
                        .lineWidth(renderOutlineWidth.getValue())

                        .build(false);
            });
        }
    }

}