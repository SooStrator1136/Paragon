package com.paragon.client.systems.module.impl.misc

import com.paragon.Paragon
import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.anyNull
import com.paragon.api.util.calculations.Timer
import com.paragon.client.managers.notifications.Notification
import com.paragon.client.managers.notifications.NotificationType
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.entity.passive.EntityDonkey
import net.minecraft.entity.passive.EntityHorse
import net.minecraft.entity.passive.EntityLlama
import net.minecraft.entity.passive.EntityMule
import java.util.*

/**
 * @author EBS, SooStrator1136
 */
object DonkeyAlert : Module("DonkeyAlert", Category.MISC, "Allows you to find donkeys and other rideable entities easier") {

    private val donkeyAlert = Setting(
        "Donkeys",
        true
    ) describedBy "Alert for Donkeys"
    private val llamaAlert = Setting(
        "Llamas",
        true
    ) describedBy "Alert for Llamas"
    private val horseAlert = Setting(
        "Horses", true
    ) describedBy "Alert for Horses"
    private val muleAlert = Setting(
        "Mules",
        true
    ) describedBy "Alert for Mules"

    private val alertMode = Setting(
        "Alert",
        AlertType.MESSAGE
    )
    private val multiAlert = Setting(
        "MultiAlert",
        false
    ) describedBy "Alert multiple times for the same animal"
    private val alertDelay = Setting(
        "Delay",
        200F,
        0F,
        5000F,
        250F
    ) describedBy "The delay between alerts"

    private val addedEntities: MutableList<Entity> = ArrayList(3)
    private val founds: Queue<Found> = LinkedList()
    private val timer = Timer()

    override fun onTick() {
        if (minecraft.anyNull) {
            return
        }

        for (e in Minecraft.getMinecraft().world.loadedEntityList) {
            var animalType: String? = null

            if (this.founds.any { it.entity == e }) {
                continue
            }

            when {
                e is EntityDonkey && donkeyAlert.value -> animalType = "Donkey"
                e is EntityMule && muleAlert.value -> animalType = "Mule"
                e is EntityLlama && llamaAlert.value -> animalType = "Llama"
                e is EntityHorse && horseAlert.value -> animalType = "Horse"
            }

            if (animalType != null) {
                var add = false

                if (!addedEntities.contains(e)) {
                    add = true
                } else if (multiAlert.value) {
                    add = true
                }

                if (add) {
                    if (!multiAlert.value) {
                        addedEntities.add(e)
                    }
                    founds.add(Found(animalType, e))
                }
            }
        }

        if (timer.hasMSPassed(alertDelay.value.toDouble()) && !founds.isEmpty()) {
            val nextFound = founds.poll()
            notify(nextFound.animalType, nextFound.entity)
            timer.reset()
        }
    }

    override fun onDisable() {
        addedEntities.clear()
        founds.clear()
    }

    private fun notify(animalType: String, e: Entity) {
        val str = "Found $animalType! X: ${e.posX.toInt()} Z: ${e.posZ.toInt()}"
        if (alertMode.value == AlertType.NOTIFICATION) {
            Paragon.INSTANCE.notificationManager.addNotification(Notification(str, NotificationType.INFO))
        } else {
            Paragon.INSTANCE.commandManager.sendClientMessage(str, false)
        }
    }

    private class Found(val animalType: String, val entity: Entity)

    private enum class AlertType {
        NOTIFICATION, MESSAGE
    }

}