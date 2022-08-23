package com.paragon.client.systems.module.impl.misc

import com.paragon.Paragon
import com.paragon.api.event.combat.TotemPopEvent
import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.anyNull
import com.paragon.api.util.system.backgroundThread
import com.paragon.bus.listener.Listener
import com.paragon.client.managers.notifications.Notification
import com.paragon.client.managers.notifications.NotificationType
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import java.util.*

/**
 * I didn't test any of the checks Idek if they work lmao
 *
 * @author SooStrator1136
 */
object Alert : Module("Alert", Category.MISC, "Alerts you on certain things") {

    private val performanceMode = Setting(
        "Performance",
        false
    ) describedBy "Can cause notifications to appear later, only activate when needed"

    private val alertMode = Setting("AlertMode", AlertMode.CHAT)
    private val alertType = Setting(
        "AlertType",
        NotificationType.WARNING
    ) visibleWhen { alertMode.value == AlertMode.NOTIFICATION }

    private val armor = Setting("Armor", false)
    private val armorThreshold = Setting(
        "ArmorThreshold",
        20F,
        1F,
        75F,
        1F
    ) visibleWhen { armor.value }

    private val alertForFriends = Setting("Friends", false)
    private val friendArmor = Setting("Armor", false) subOf alertForFriends
    private val friendArmorThreshold = Setting(
        "ArmorThreshold",
        20F,
        1F,
        75F,
        1F
    ) subOf alertForFriends visibleWhen { friendArmor.value }
    private val friendPop = Setting("Pops", false) subOf alertForFriends
    private val friendPopCoords = Setting("IncludeCoords", true) subOf alertForFriends visibleWhen { friendPop.value }
    private val friendHp = Setting("Hp", false) subOf alertForFriends
    private val friendHpThreshold = Setting(
        "HpThreshold",
        10F,
        0.5F,
        20F,
        0.5F
    ) subOf alertForFriends visibleWhen { friendHp.value }

    private val warnedArmors = booleanArrayOf(false, false, false, false)
    private val warnedFriendsArmor: MutableMap<Entity, BooleanArray> = HashMap()
    private val warnedFriendsHp: MutableMap<EntityPlayer, Boolean> = HashMap()

    private var lastJob: Job? = null

    override fun onEnable() {
        Arrays.fill(warnedArmors, false)
        warnedFriendsArmor.clear()
    }

    override fun onTick() {
        if (minecraft.anyNull) {
            return
        }

        if (performanceMode.value) {
            if (lastJob == null || lastJob!!.isCompleted) {
                backgroundThread {
                    lastJob = launch {
                        doChecks()
                    }
                }
            }
        } else {
            doChecks()
        }
    }

    private fun doChecks() {
        if (armor.value) {
            minecraft.player.armorInventoryList.forEachIndexed { i, stack ->
                if ((100F - (1 - (stack.maxDamage - stack.itemDamage)) / stack.maxDamage * 100F) <= armorThreshold.value) {
                    if (!warnedArmors[i]) {
                        warnedArmors[i] = true
                        alert("${stack.displayName} is about to break!")
                    }
                } else {
                    warnedArmors[i] = false
                }
            }
        }

        if (!alertForFriends.value) {
            return
        }

        if (friendArmor.value) {
            minecraft.world.loadedEntityList.filter {
                it is EntityPlayer && Paragon.INSTANCE.socialManager.isFriend(it.name)
            }.forEach {
                if (!warnedFriendsArmor.containsKey(it)) {
                    warnedFriendsArmor[it] = booleanArrayOf(false, false, false, false)
                }

                it.armorInventoryList.forEachIndexed { i, stack ->
                    if ((100F - (1 - (stack.maxDamage - stack.itemDamage)) / stack.maxDamage * 100F) <= friendArmorThreshold.value) {
                        if (!warnedFriendsArmor[it]!![i]) {
                            warnedFriendsArmor[it]!![i] = true
                            alert("${it.name} is about to loose a piece of armor!")
                        }
                    } else {
                        warnedFriendsArmor[it]!![i] = false
                    }
                }
            }
        }

        if (friendHp.value) {
            minecraft.world.loadedEntityList.filter {
                it is EntityPlayer && Paragon.INSTANCE.socialManager.isFriend(it.name)
            }.forEach {
                if (!warnedFriendsHp.containsKey(it as EntityPlayer)) {
                    warnedFriendsHp[it] = false
                }

                if (it.health <= friendHpThreshold.value) {
                    if (!warnedFriendsHp[it]!!) {
                        warnedFriendsHp[it] = true
                        alert("${it.name} has low hp!")
                    }
                } else {
                    warnedFriendsHp[it] = false
                }
            }
        }
    }

    @Listener
    fun onPop(event: TotemPopEvent) {
        if (!alertForFriends.value || !friendPop.value || !Paragon.INSTANCE.socialManager.isFriend(event.player.name)) {
            return
        }

        val player = event.player

        alert(
            if (friendPopCoords.value) {
                "Your friend ${player.name} just popped at ${player.posX.toInt()}, ${player.posY.toInt()}, ${player.posZ.toInt()}!"
            } else {
                "Your friend ${player.name} just popped!"
            }
        )
    }

    private fun alert(str: String) {
        if (alertMode.value == AlertMode.NOTIFICATION) {
            Paragon.INSTANCE.notificationManager.addNotification(Notification(str, alertType.value))
        } else {
            Paragon.INSTANCE.commandManager.sendClientMessage(str, false)
        }
    }

    internal enum class AlertMode {
        CHAT, NOTIFICATION
    }

}