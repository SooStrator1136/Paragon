package com.paragon.impl.module.hud.impl

import com.paragon.Paragon
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.util.render.font.FontUtil
import com.paragon.impl.module.hud.HUDModule
import com.paragon.impl.module.client.ClickGUI
import com.paragon.impl.module.client.Colours
import com.paragon.impl.ui.configuration.discord.GuiDiscord
import com.paragon.impl.module.Category
import com.paragon.util.calculations.Timer
import com.paragon.util.render.RenderUtil
import com.paragon.util.string.StringUtil
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent
import org.lwjgl.input.Keyboard
import java.awt.Color

/**
 * @author SooStrator1136
 */
object TabGui : HUDModule("TabGui", "Gui with tabs or smth") {

    private val color = Setting(
        "Color", Colours.mainColour.value
    ) describedBy "Color that will be used in most of the styles"

    private val style = Setting(
        "Style", ClickGUI.style.value
    ) describedBy "Style of the TabGui"

    private var focusedModule: Module? = null
    private var shownModules: Array<Module>? = null
    private var moduleY = 0F

    private var focusedCategory = Category.values()[0]

    private val keyTimer = Timer()

    private var catWidth = FontUtil.getStringWidth(StringUtil.getFormattedText(Category.values().maxWith(Comparator.comparingDouble { FontUtil.getStringWidth(StringUtil.getFormattedText(it)).toDouble() }))) + 4F

    private val catHeight: Float
        get() = ((FontUtil.getHeight() + 1F) * Category.values().size) - 1F

    private val moduleWidth: Float
        get() = if (shownModules == null || focusedModule == null) 0F
        else FontUtil.getStringWidth(shownModules!!.maxWith(Comparator.comparingDouble { FontUtil.getStringWidth(it.name).toDouble() }).name) + 4F

    private val moduleHeight: Float
        get() = ((FontUtil.getHeight() + 1F) * if (shownModules == null || focusedModule == null) 0 else shownModules!!.size) - 1

    //Chinese asf, probably will never clean up though lel
    override fun render() {
        // Gotta update this
        catWidth = FontUtil.getStringWidth(StringUtil.getFormattedText(Category.values().maxWith(Comparator.comparingDouble { FontUtil.getStringWidth(StringUtil.getFormattedText(it)).toDouble() }))) + 4F

        when (style.value) {
            ClickGUI.Style.WINDOWS_98 -> {
                RenderUtil.drawRect(
                    x + 1, y + 1, catWidth, catHeight, Color(100, 100, 100)
                )
                RenderUtil.drawRect(
                    x, y, catWidth, catHeight, Color(148, 148, 148)
                )
                RenderUtil.drawRect(
                    x, y + ((FontUtil.getHeight() + 1F) * Category.values().indexOf(focusedCategory)), catWidth, FontUtil.getHeight(), Color(100, 100, 100)
                )

                if (focusedModule != null && shownModules != null) {
                    moduleY = y + ((FontUtil.getHeight() + 1F) * Category.values().indexOf(focusedCategory))
                    RenderUtil.drawRect(
                        x + catWidth + 1, moduleY + 1, moduleWidth - 1, moduleHeight, Color(100, 100, 100)
                    )
                    RenderUtil.drawRect(
                        x + catWidth, moduleY, moduleWidth - 1, moduleHeight, Color(148, 148, 148)
                    )
                    RenderUtil.drawRect(
                        x + catWidth, moduleY + ((FontUtil.getHeight() + 1F) * shownModules!!.indexOf(focusedModule)), moduleWidth - 1, FontUtil.getHeight(), Color(100, 100, 100)
                    )
                }
            }

            ClickGUI.Style.DISCORD -> {
                RenderUtil.drawRoundedRect(
                    x, y - 1f, (catWidth + FontUtil.getStringWidth("# ")), catHeight + 1f, 5f, GuiDiscord.channelBarBackground
                )
                RenderUtil.drawRoundedRect(
                    x, (y + ((FontUtil.getHeight() + 1F) * Category.values().indexOf(focusedCategory))) - 1f, (catWidth + FontUtil.getStringWidth("# ")), FontUtil.getHeight() + 2f, 3.5f, GuiDiscord.channelHoveredColor
                )


                if (focusedModule != null && shownModules != null) {
                    moduleY = y + ((FontUtil.getHeight() + 1F) * Category.values().indexOf(focusedCategory))
                    RenderUtil.drawRoundedRect(
                        (x + catWidth + FontUtil.getStringWidth("# ")), moduleY, (moduleWidth + FontUtil.getStringWidth("# ")), moduleHeight + 2f, 5f, GuiDiscord.channelBarBackground
                    )
                    RenderUtil.drawRoundedRect(
                        (x + catWidth + FontUtil.getStringWidth("# ")), (moduleY + ((FontUtil.getHeight() + 1F) * shownModules!!.indexOf(focusedModule))), (moduleWidth + FontUtil.getStringWidth("# ")), FontUtil.getHeight(), 3.5f, GuiDiscord.channelHoveredColor
                    )
                }
            }

            ClickGUI.Style.PLUGIN -> {
                FontUtil.drawStringWithShadow("Feature not implemented!", x, y, Color.WHITE)
            }

            ClickGUI.Style.PANEL -> {
                RenderUtil.drawRect(
                    x, y, catWidth, catHeight, Color(40, 40, 60)
                )

                RenderUtil.drawRect(
                    x, y + ((FontUtil.getHeight() + 1F) * Category.values().indexOf(focusedCategory)), catWidth, FontUtil.getHeight() + 1F, Color(60, 60, 80)
                )

                if (focusedModule != null && shownModules != null) {
                    moduleY = y + ((FontUtil.getHeight() + 1F) * Category.values().indexOf(focusedCategory))

                    RenderUtil.drawRect(
                        x + catWidth, moduleY, moduleWidth - 1, moduleHeight, Color(40, 40, 60)
                    )

                    RenderUtil.drawRect(
                        x + catWidth, moduleY + ((FontUtil.getHeight() + 1F) * shownModules!!.indexOf(focusedModule)), moduleWidth - 1, FontUtil.getHeight(), Color(60, 60, 80)
                    )
                }
            }

            else -> {}
        }

        //The most chinese 😭 (I'll probably clean this up)
        if (style.value == ClickGUI.Style.DISCORD) {
            var catY = y + 1F
            for (cat in Category.values()) {
                FontUtil.drawStringWithShadow(
                    "# ${StringUtil.getFormattedText(cat)}", x + 2F, catY, GuiDiscord.channelTextColor
                )
                catY += FontUtil.getHeight() + 1F
            }

            if (focusedModule != null && shownModules != null) {
                var modY = moduleY + 1F
                for (mod in shownModules!!) {
                    FontUtil.drawStringWithShadow(
                        "# ${mod.name}", x + catWidth + 2F + FontUtil.getStringWidth("# "), modY, GuiDiscord.channelTextColor
                    )
                    modY += FontUtil.getHeight() + 1F
                }
            }
        }
        else {
            var catY = y + 1F
            for (cat in Category.values()) {
                FontUtil.drawStringWithShadow(
                    StringUtil.getFormattedText(cat), x + 2F, catY, Color.WHITE
                )
                catY += FontUtil.getHeight() + 1F
            }

            if (focusedModule != null && shownModules != null) {
                var modY = moduleY + 1F
                for (mod in shownModules!!) {
                    FontUtil.drawStringWithShadow(
                        mod.name, x + catWidth + 2F, modY, Color.WHITE
                    )
                    modY += FontUtil.getHeight() + 1F
                }
            }
        }
    }

    @SubscribeEvent
    fun onKey(event: KeyInputEvent) {
        if (!keyTimer.hasMSPassed(140.0)) { //Event gets fired on press and release -> Average length of a press is 140 apparently
            return
        }
        keyTimer.reset()
        when (Keyboard.getEventKey()) {
            Keyboard.KEY_RIGHT -> {
                if (focusedModule == null) {
                    val mods = Paragon.INSTANCE.moduleManager.getModulesThroughPredicate {
                        it.category == focusedCategory
                    }
                    moduleY = y + ((FontUtil.getHeight() + 1F) * Category.values().indexOf(focusedCategory))
                    shownModules = mods.toTypedArray()
                    focusedModule = mods[0]
                }
                else {
                    focusedModule!!.toggle()
                }
            }

            Keyboard.KEY_LEFT -> {
                if (focusedModule != null) {
                    focusedModule = null
                    shownModules = null
                }
            }

            Keyboard.KEY_UP -> {
                if (focusedModule == null) {
                    val index = Category.values().indexOf(focusedCategory) - 1
                    focusedCategory = Category.values()[if (index <= -1) Category.values().size - 1 else index]
                }
                else {
                    val index = shownModules!!.indexOf(focusedModule) - 1
                    focusedModule = shownModules!![if (index <= -1) shownModules!!.size - 1 else index]
                }
            }

            Keyboard.KEY_DOWN -> {
                if (focusedModule == null) {
                    val index = Category.values().indexOf(focusedCategory) + 1
                    focusedCategory = Category.values()[if (index >= Category.values().size) 0 else index]
                }
                else {
                    val index = shownModules!!.indexOf(focusedModule) + 1
                    focusedModule = shownModules!![if (index >= shownModules!!.size) 0 else index]
                }
            }
        }
    }

    override var width = 0F
        get() = if (style.value == ClickGUI.Style.DISCORD) {
            catWidth + FontUtil.getStringWidth("# ")
        }
        else {
            catWidth + 15F
        }

    override var height = 0F
        get() = catHeight

}