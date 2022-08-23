package com.paragon.client.systems.module.hud.impl

import com.paragon.api.util.render.font.FontUtil
import com.paragon.api.util.render.font.FontUtil.drawStringWithShadow
import com.paragon.api.util.render.font.FontUtil.getStringWidth
import com.paragon.client.systems.module.hud.HUDModule
import com.paragon.client.systems.module.impl.client.Colours
import com.paragon.client.systems.module.impl.combat.Aura
import com.paragon.client.systems.module.impl.combat.AutoCrystal
import net.minecraft.util.text.TextFormatting

/**
 * @author Surge
 */
object CombatInfo : HUDModule("CombatInfo", "Shows what combat modules are enabled") {

    override fun render() {
        drawStringWithShadow(
            "KA " + if (Aura.isEnabled) TextFormatting.GREEN.toString() + "Enabled" else TextFormatting.RED.toString() + "Disabled",
            x,
            y,
            Colours.mainColour.value.rgb
        )

        drawStringWithShadow(
            "CA " + if (AutoCrystal.isEnabled) TextFormatting.GREEN.toString() + "Enabled" else TextFormatting.RED.toString() + "Disabled",
            x,
            y + FontUtil.getHeight(),
            Colours.mainColour.value.rgb
        )
    }

    override var width = 10F
        get() = getStringWidth("AC Disabled")

    override var height = 20F
        get() = FontUtil.getHeight() * 3

}