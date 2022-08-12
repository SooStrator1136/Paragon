package com.paragon.client.systems.module.impl.misc

import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.anyNull
import net.minecraft.client.gui.GuiMainMenu
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

/**
 * @author Surge
 */
@SideOnly(Side.CLIENT)
object AutoLog : Module("AutoLog", Category.MISC, "Automatically logs you out when you reach a certain health") {

    private val logMode = Setting(
        "LogMode",
        DisconnectMode.DISCONNECT
    ) describedBy "How to log you out of the server"

    private val health = Setting(
        "Health",
        6f,
        1f,
        20f,
        1f
    ) describedBy "The health to log you out at"

    private val autoDisable = Setting(
        "AutoDisable",
        true
    ) describedBy "Disables the module after logging you out"

    override fun onTick() {
        if (minecraft.anyNull) {
            return
        }

        if (minecraft.player.health <= health.value) {
            when (logMode.value) {
                DisconnectMode.KICK ->                     // Set current item to an invalid number
                    minecraft.player.inventory.currentItem = -1

                DisconnectMode.DISCONNECT -> {
                    // Disconnect from server
                    minecraft.world.sendQuittingDisconnectingPacket()
                    minecraft.loadWorld(null)
                    minecraft.displayGuiScreen(GuiMainMenu())
                }
            }

            if (autoDisable.value) {
                // Toggle module state
                toggle()
            }
        }
    }

    enum class DisconnectMode {
        /**
         * Disconnects you from the server
         */
        DISCONNECT,

        /**
         * Kicks you from the server by setting your current item to an invalid slot
         */
        KICK
    }

}