package com.paragon.impl.module.hud.impl

import com.paragon.impl.setting.Setting
import com.paragon.util.render.font.FontUtil
import com.paragon.impl.module.hud.HUDModule
import com.paragon.impl.module.client.Colours
import com.paragon.util.render.RenderUtil
import me.surge.animation.Animation
import me.surge.animation.Easing
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D

/**
 * @author SooStrator1136
 */
object Keystrokes : HUDModule("Keystrokes", "Keystrokes duh?") {

    //Bounds
    private val keySize = Setting(
        "Size", 20F, 10F, 50F, 1F
    ) describedBy "Size of the keys"
    private val keyDistance = Setting(
        "Distance", 2F, 1F, 10F, 0.1F
    ) describedBy "Distance between the keys"

    //Animation
    private val animationSpeed = Setting(
        "Animation Speed", 300F, 100F, 2000F, 50F
    ) describedBy "The time to fill the rect with a circle"
    private val animationEasing = Setting("Easing", Easing.LINEAR)

    //Coloring
    private val backgroundColor = Setting(
        "Background", Color(0, 0, 0, 35)
    ) describedBy "Color of the background"
    private val circleColor = Setting(
        "Fill color", Colours.mainColour.value
    ) describedBy "Color of the circle filling indicating the pressed keys"

    //Could have used an array but uhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh
    private val keyW = KeyEntry(Keyboard.KEY_W)
    private val keyA = KeyEntry(Keyboard.KEY_A)
    private val keyS = KeyEntry(Keyboard.KEY_S)
    private val keyD = KeyEntry(Keyboard.KEY_D)
    private val keySpace = KeyEntry(Keyboard.KEY_SPACE)

    override fun render() {
        //Set postions
        run {
            keyW.bounds.setRect(
                x + keySize.value + keyDistance.value, y, keySize.value, keySize.value
            )

            keyA.bounds.setRect(
                x, y + keySize.value + keyDistance.value, keySize.value, keySize.value
            )

            keyS.bounds.setRect(
                x + keySize.value + keyDistance.value, y + keySize.value + keyDistance.value, keySize.value, keySize.value
            )

            keyD.bounds.setRect(
                x + (keySize.value * 2F) + (keyDistance.value * 2F), y + keySize.value + keyDistance.value, keySize.value, keySize.value
            )

            keySpace.bounds.setRect(
                x, y + (keySize.value * 2F) + (keyDistance.value * 2F), (keySize.value * 3F) + (keyDistance.value * 2F), keySize.value
            )
        }

        //Render
        run {
            keyW.render()
            keyA.render()
            keyS.render()
            keyD.render()
            keySpace.render()
        }
    }

    internal class KeyEntry(private val keyCode: Int) {
        val bounds = Rectangle2D.Float()

        private val animation = Animation(animationSpeed::value, false, animationEasing::value)

        fun render() {
            animation.state = Keyboard.isKeyDown(keyCode)

            RenderUtil.pushScissor(
                bounds.x.toDouble(), bounds.y.toDouble(), bounds.width.toDouble(), bounds.height.toDouble()
            )

            RenderUtil.drawRect(
                bounds.x, bounds.y, bounds.width, bounds.height, backgroundColor.value.rgb
            )

            if (animation.getAnimationFactor() > 0.0) {
                RenderUtil.drawCircle(
                    bounds.centerX, bounds.centerY, Point2D.distance(
                        bounds.centerX, bounds.centerY, bounds.x.toDouble(), bounds.y.toDouble()
                    ) * animation.getAnimationFactor(), circleColor.value.rgb
                )
            }

            FontUtil.renderCenteredString(
                Keyboard.getKeyName(keyCode), bounds.centerX.toFloat(), bounds.centerY.toFloat(), -1, true
            )

            RenderUtil.popScissor()
        }

    }

    override var width = 0F
        get() = (keySize.value * 3F) + (keyDistance.value * 2F)

    override var height = 0F
        get() = (keySize.value * 3F) + (keyDistance.value * 2F)

}