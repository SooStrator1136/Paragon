package com.paragon.client.systems.module.impl.render;

import com.paragon.api.event.render.gui.RenderTooltipEvent;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.render.TextRenderer;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.impl.client.Colours;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;

import java.awt.*;

/**
 * @author Wolfsurge
 */
public class ShulkerViewer extends Module implements TextRenderer {

    public ShulkerViewer() {
        super("ShulkerViewer", ModuleCategory.RENDER, "Shows the contents of a shulker box in your inventory without having to open it");
    }

    @Listener
    public void onRenderTooltip(RenderTooltipEvent event) {
        if (event.getStack().getItem() instanceof ItemShulkerBox) {
            // Get stack compound
            NBTTagCompound compound = event.getStack().getTagCompound();

            if (compound != null && compound.hasKey("BlockEntityTag")) {
                // Has items
                if (compound.getCompoundTag("BlockEntityTag").hasKey("Items", 9)) {
                    event.cancel();

                    // Translate so the tooltip is above other items etc
                    GlStateManager.translate(0, 0, 500);

                    // Get item list in shulker
                    NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
                    ItemStackHelper.loadAllItems(event.getStack().getTagCompound().getCompoundTag("BlockEntityTag"), items);

                    // Y offset
                    float y = event.getY() - 31;

                    // Background
                    RenderUtil.drawRect(event.getX() + 2, y, 168, 71, new Color(23, 23, 25).getRGB());

                    // Border
                    RenderUtil.drawBorder(event.getX() + 2, y, 168, 71, 1, Colours.mainColour.getColour().getRGB());

                    // Shulker box name
                    renderText(event.getStack().getDisplayName(), event.getX() + 6, y + 2.5f, -1);

                    // Separator thing
                    RenderUtil.drawRect(event.getX() + 2, y + 13, 168, 1, Colours.mainColour.getColour().getRGB());

                    // Item X and Y
                    float itemX = event.getX() + 5;
                    float itemY = y + 16;

                    // Count of thing to determine when to start a new row
                    int a = 0;

                    // Iterate through items
                    for (ItemStack item : items) {
                        // Background thing
                        RenderUtil.drawRect(itemX - 0.5f, itemY - 0.5f, 17, 17, new Color(25, 25, 28).getRGB());

                        // Render stack
                        RenderUtil.renderItemStack(item, itemX, itemY, true);

                        // Increase count
                        a++;

                        // Increase X
                        itemX += 18;

                        // If count is not 0, and is divisible by 9, move to the next row
                        if (a != 0 && a % 9 == 0) {
                            itemX = event.getX() + 5;
                            itemY += 18;
                        }
                    }
                }
            }
        }
    }

}
