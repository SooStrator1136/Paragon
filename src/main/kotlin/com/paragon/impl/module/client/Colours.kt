package com.paragon.impl.module.client

import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.impl.module.Category
import java.awt.Color

object Colours : Module("Colours", Category.CLIENT, "The client's main colour") {

    @JvmField
    var mainColour = Setting(
        "Main Colour", Color(185, 19, 211)
    ) describedBy "The main colour of the client"

}