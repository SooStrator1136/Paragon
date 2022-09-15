package com.paragon.impl.command.impl

import com.paragon.Paragon

/**
 * @author EBS
 */
object NearestStronghold : com.paragon.impl.command.Command("Nearest", "nearest") {

    private val endPortalCoords = arrayOf(
        intArrayOf(1888, -32), intArrayOf(-560, 1504), intArrayOf(2064, -4400), intArrayOf(-4992, -512), intArrayOf(2960, 4208), intArrayOf(-3200, 4480), intArrayOf(-5568, 608), intArrayOf(-2496, 5296)
    )

    override fun whenCalled(args: Array<String>, fromConsole: Boolean) {
        //check if server is 2b2t.org using  Minecraft.getCurrentServerData()
        if ((minecraft.currentServerData ?: return).serverIP == "connect.2b2t.org") {
            //get stronghold location nearest to player on 2b2t.org
            if (minecraft.player.dimension == 1) {
                Paragon.INSTANCE.commandManager.sendClientMessage(
                    "don't you feel stupid... don't you feel a little ashamed...", false
                )
            }
            var closestX = endPortalCoords[0][0]
            var closestZ = endPortalCoords[0][1]
            var shortestDistance = minecraft.player.getDistanceSq(
                endPortalCoords[0][0].toDouble(), 0.0, endPortalCoords[0][1].toDouble()
            )
            for (i in 1 until endPortalCoords.size) {
                val d = minecraft.player.getDistanceSq(
                    endPortalCoords[i][0].toDouble(), 0.0, endPortalCoords[i][1].toDouble()
                )
                if (d < shortestDistance) {
                    closestX = endPortalCoords[i][0]
                    closestZ = endPortalCoords[i][1]
                    shortestDistance = d
                }
            }
            Paragon.INSTANCE.commandManager.sendClientMessage("Nearest stronghold around ($closestX, $closestZ)", false)
        }
        else {
            Paragon.INSTANCE.commandManager.sendClientMessage("you are not in 2b2t, please join to use this", false)
        }
    }
}