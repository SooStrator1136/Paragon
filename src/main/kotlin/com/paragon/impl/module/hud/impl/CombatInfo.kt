package com.paragon.impl.module.hud.impl

import com.paragon.util.render.font.FontUtil
import com.paragon.util.render.font.FontUtil.drawStringWithShadow
import com.paragon.util.render.font.FontUtil.getStringWidth
import com.paragon.impl.module.hud.HUDModule
import com.paragon.impl.module.client.Colours
import com.paragon.impl.module.combat.Aura
import com.paragon.impl.module.combat.AutoCrystal
import net.minecraft.util.text.TextFormatting

/**
 * @author Surge
 */
object CombatInfo : HUDModule("CombatInfo", "Shows what combat modules are enabled") {

    override fun render() {
        drawStringWithShadow(
            "KA " + if (Aura.isEnabled) TextFormatting.GREEN.toString() + "Enabled" else TextFormatting.RED.toString() + "Disabled", x, y, Colours.mainColour.value.rgb
        )

        drawStringWithShadow(
            "CA " + if (AutoCrystal.isEnabled) TextFormatting.GREEN.toString() + "Enabled" else TextFormatting.RED.toString() + "Disabled", x, y + FontUtil.getHeight(), Colours.mainColour.value.rgb
        )
    }

    override var width = 10F
        get() = getStringWidth("AC Disabled")

    override var height = 20F
        get() = FontUtil.getHeight() * 3

}