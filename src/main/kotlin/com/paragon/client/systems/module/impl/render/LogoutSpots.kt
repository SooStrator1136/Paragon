package com.paragon.client.systems.module.impl.render

import com.paragon.Paragon
import com.paragon.api.event.network.PlayerEvent
import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.anyNull
import com.paragon.api.util.render.RenderUtil
import com.paragon.api.util.render.builder.BoxRenderMode
import com.paragon.api.util.render.builder.RenderBuilder
import com.paragon.api.util.world.BlockUtil
import com.paragon.bus.listener.Listener
import com.paragon.client.managers.notifications.Notification
import com.paragon.client.managers.notifications.NotificationType
import io.ktor.util.collections.*
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.Vec3d
import java.awt.Color
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * @author Surge
 * @since 06/09/2022
 */
object LogoutSpots : Module("LogoutSpots", Category.RENDER, "Shows where players have logged out") {

    private val range = Setting("Range", 64.0, 16.0, 256.0, 1.0) describedBy "The range to check for players"

    private val notify = Setting("Notify", false) describedBy "Notify you when a player logs out or logs back in"
    private val logIn = Setting("LogIn", true) describedBy "Notify you when a player logs in" subOf notify
    private val logInType = Setting("LogInType", NotificationType.WARNING) describedBy "The type of notification" subOf notify visibleWhen { logIn.value }
    private val logOut = Setting("LogOut", false) describedBy "Notify you when a player logs out" subOf notify
    private val logOutType = Setting("LogOutType", NotificationType.INFO) describedBy "The type of notification" subOf notify visibleWhen { logOut.value }

    private val render = Setting("Render", BoxRenderMode.BOTH) describedBy "Render the placement"
    private val renderOutlineWidth = Setting("OutlineWidth", 0.5f, 0.1f, 2f, 0.1f) describedBy "The width of the lines" subOf render

    private val renderNametag = Setting("Nametag", true) describedBy "Render the nametag" subOf render
    private val nametagYOffset = Setting("NametagYOffset", 1.1, 0.0, 3.0, 0.1) subOf render visibleWhen { renderNametag.value }

    private val timeNametag = Setting("TimeNametag", true) describedBy "Render the time nametag" subOf render
    private val timeNametagYOffset = Setting("TimeNametagYOffset", 0.8, 0.0, 3.0, 0.1) describedBy "The Y offset of the time nametag" subOf render visibleWhen { renderNametag.value }

    private val enemyRenderColour = Setting("EnemyFillColour", Color(185, 19, 255, 130)) describedBy "The colour of the fill" subOf render
    private val enemyRenderOutlineColour = Setting("EnemyOutlineColour", Color(185, 19, 255)) subOf render
    private val friendRenderColour = Setting("FriendFillColour", Color(185, 19, 255, 130)) describedBy "The colour of the fill" subOf render
    private val friendRenderOutlineColour = Setting("FriendOutlineColour", Color(185, 19, 255)) subOf render

    private val boxHeight = Setting("BoxHeight", 2.0, 0.0, 3.0, 0.1) describedBy "The height of the box" subOf render

    // List of players in the world, refreshed each tick
    private val playerSet = ConcurrentSet<EntityPlayer>()

    // List of logged players
    private val logged = ConcurrentHashMap<EntityPlayer, String>()

    override fun onTick() {
        if (minecraft.anyNull) {
            // Clear if we aren't in a world
            playerSet.clear()
            logged.clear()
            return
        }

        // Refresh player set
        minecraft.world.playerEntities.filter { it != minecraft.player }.forEach { playerSet.add(it) }
    }

    override fun onRender3D() {
        logged.forEach { (player, date) ->
            // Do not render if they are far away
            if (player.getDistance(minecraft.player) >= range.value) {
                return@forEach
            }

            // Original box
            val originalBox = BlockUtil.getBlockBox(player.position)

            // Original box, with modified height
            val boundingBox: AxisAlignedBB = originalBox.setMaxY(originalBox.minY + boxHeight.value)

            // Render box
            RenderBuilder()
                .boundingBox(boundingBox)
                .inner(if (Paragon.INSTANCE.socialManager.isFriend(player.name)) friendRenderColour.value else enemyRenderColour.value)
                .outer(if (Paragon.INSTANCE.socialManager.isFriend(player.name)) friendRenderOutlineColour.value else enemyRenderOutlineColour.value)
                .type(render.value)

                .start()
                .lineWidth(renderOutlineWidth.value)
                .blend(true)
                .depth(true)
                .texture(true)
                .build(false)

            // Vec3d of the player's position
            val posVec = Vec3d(player.position)

            // Render name nametag
            if (renderNametag.value) {
                RenderUtil.drawNametagText(player.name, posVec.add(Vec3d(0.5, nametagYOffset.value, 0.5)), -1)
            }

            // Render time nametag
            if (timeNametag.value) {
                RenderUtil.drawNametagText(date, posVec.add(Vec3d(0.5, timeNametagYOffset.value, 0.5)), -1)
            }
        }
    }

    @Listener
    fun onPlayerJoin(event: PlayerEvent.PlayerJoinEvent) {
        if (minecraft.anyNull) {
            return
        }

        logged.entries.removeIf { (player, _) ->
            // If the entry is the player joining
            if (player.name.equals(event.name)) {
                // Notify ourselves
                if (notify.value && logIn.value) {
                    Paragon.INSTANCE.notificationManager.addNotification(Notification("${player.name} has logged back in!", logInType.value))
                }

                // Remove from list
                true
            } else {
                false
            }
        }
    }

    @Listener
    fun onPlayerLeave(event: PlayerEvent.PlayerLeaveEvent) {
        if (minecraft.anyNull) {
            return
        }

        playerSet.removeIf {
            // If the player leaving is in the player set
            if (it.name.equals(event.name)) {
                // Notify ourselves
                if (notify.value && logOut.value) {
                    Paragon.INSTANCE.notificationManager.addNotification(Notification("${it.name} has logged out!", logOutType.value))
                }

                // Put player in logged map, and set the time
                logged[it] = SimpleDateFormat("k:mm").format(Date())
                true
            } else {
                false
            }
        }
    }

}