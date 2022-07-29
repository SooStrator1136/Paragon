package com.paragon.client.systems.command.impl

import com.paragon.Paragon
import com.paragon.client.managers.CommandManager
import com.paragon.client.systems.command.Command
import net.minecraft.client.Minecraft
import net.minecraft.util.math.BlockPos
import net.minecraft.world.GameType
import net.minecraft.world.WorldProvider
import net.minecraft.world.WorldProviderSurface
import net.minecraft.world.WorldSettings
import net.minecraft.world.gen.structure.MapGenStronghold
import net.minecraft.world.storage.WorldInfo

/**
 * @author EBS
 */

object NearestStronghold : Command("Nearest", "nearest"){
    override fun whenCalled(args: Array<String>, fromConsole: Boolean) {
        val mc = Minecraft.getMinecraft()

        //check if server is 2b2t.org using  Minecraft.getCurrentServerData()

        //check if server is 2b2t.org using  Minecraft.getCurrentServerData()
        if (mc.currentServerData!!.serverIP == "connect.2b2t.org") {
            //get stronghold location nearest to player on 2b2t.org
            if (mc.player.dimension == 1) {
                Paragon.INSTANCE.commandManager.sendClientMessage("dont you feel stupid... dont you feel a little ashamed...", false)
            }
            val endPortalCoords = arrayOf(
                intArrayOf(1888, -32),
                intArrayOf(-560, 1504),
                intArrayOf(2064, -4400),
                intArrayOf(-4992, -512),
                intArrayOf(2960, 4208),
                intArrayOf(-3200, 4480),
                intArrayOf(-5568, 608),
                intArrayOf(-2496, 5296)
            )
            var closestX = endPortalCoords[0][0]
            var closestZ = endPortalCoords[0][1]
            var shortestDistance =
                mc.player.getDistanceSq(endPortalCoords[0][0].toDouble(), 0.0, endPortalCoords[0][1].toDouble()).toInt()
            for (i in 1 until endPortalCoords.size) {
                val d = mc.player.getDistanceSq(endPortalCoords[i][0].toDouble(), 0.0, endPortalCoords[i][1].toDouble())
                    .toInt()
                if (d < shortestDistance) {
                    closestX = endPortalCoords[i][0]
                    closestZ = endPortalCoords[i][1]
                    shortestDistance = d
                }
            }
            Paragon.INSTANCE.commandManager.sendClientMessage("Nearest stronghold around (" + closestX.toString() +", " + closestZ.toString() + ")", false)
        } else{
            Paragon.INSTANCE.commandManager.sendClientMessage("you are not in 2b2t please join one to use this", false)

        }
    }
}