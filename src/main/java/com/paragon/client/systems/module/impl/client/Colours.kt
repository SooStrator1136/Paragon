package com.paragon.client.systems.module.impl.client

import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import java.awt.Color

object Colours : Module("Colours", Category.CLIENT, "The client's main colour") {

    @JvmField
    var mainColour = Setting(
        "Main Colour",
        Color(185, 19, 211)
    ) describedBy "The main colour of the client"

}