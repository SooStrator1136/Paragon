package com.paragon.client.systems.module.hud.impl

import com.paragon.Paragon
import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.calculations.Timer
import com.paragon.api.util.render.RenderUtil
import com.paragon.api.util.render.font.FontUtil
import com.paragon.client.systems.module.hud.HUDModule
import com.paragon.client.systems.module.impl.client.ClickGUI
import com.paragon.client.systems.module.impl.client.Colours
import com.paragon.client.ui.configuration.discord.GuiDiscord
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent
import org.lwjgl.input.Keyboard
import java.awt.Color

/**
 * @author SooStrator1136
 */
object TabGui : HUDModule("TabGui", "Gui with tabs or smth") {

    private val color = Setting(
        "Color",
        Colours.mainColour.value
    ) describedBy "Color that will be used in most of the styles"
    private val style = Setting(
        "Style",
        ClickGUI.style.value
    ) describedBy "Style of the TabGui"
    private val background = Setting(
        "Background",
        Color(100, 100, 100, 150)
    ) subOf style visibleWhen { style.value == ClickGUI.Style.ZERODAY }

    private var focusedModule: Module? = null
    private var shownModules: Array<Module>? = null
    private var moduleY = 0F

    private var focusedCategory = Category.values()[0]

    private val keyTimer = Timer()

    private var catWidth = FontUtil.getStringWidth(Category.values().maxWith(
        Comparator.comparingDouble { FontUtil.getStringWidth(it.Name).toDouble() }
    ).Name) + 4F

    private val catHeight: Float
        get() = ((FontUtil.getHeight() + 1F) * Category.values().size) - 1F

    private val moduleWidth: Float
        get() = if (shownModules == null || focusedModule == null) 0F else FontUtil.getStringWidth(
            shownModules!!.maxWith(
                Comparator.comparingDouble { FontUtil.getStringWidth(it.name).toDouble() }
            ).name
        ) + 4F

    private val moduleHeight: Float
        get() = ((FontUtil.getHeight() + 1F) * if (shownModules == null || focusedModule == null) 0 else shownModules!!.size) - 1

    //Chinese asf, probably will never clean up though lel
    override fun render() {
        // Gotta update this
        catWidth = FontUtil.getStringWidth(Category.values().maxWith(
            Comparator.comparingDouble { FontUtil.getStringWidth(it.Name).toDouble() }
        ).Name) + 4F

        when (style.value) {
            ClickGUI.Style.ZERODAY -> {
                RenderUtil.drawRoundedRect(
                    x.toDouble(),
                    y.toDouble(),
                    catWidth + 15.0,
                    catHeight + 1.0,
                    5.0,
                    5.0,
                    5.0,
                    5.0,
                    background.value.rgb
                )
                RenderUtil.drawRoundedRect(
                    x.toDouble(),
                    (y + ((FontUtil.getHeight() + 1F) * Category.values().indexOf(focusedCategory))).toDouble(),
                    3.0,
                    FontUtil.getHeight().toDouble(),
                    3.0,
                    3.0,
                    3.0,
                    3.0,
                    color.value.rgb
                )


                if (focusedModule != null && shownModules != null) {
                    moduleY = y + ((FontUtil.getHeight() + 1F) * Category.values().indexOf(focusedCategory))
                    RenderUtil.drawRoundedRect(
                        x + catWidth + 15.0,
                        moduleY - 1.0,
                        moduleWidth + 5.0,
                        moduleHeight + 2.0,
                        5.0,
                        5.0,
                        5.0,
                        5.0,
                        background.value.rgb
                    )
                    RenderUtil.drawRoundedRect(
                        (x + catWidth + 20.0 + moduleWidth) - 3.0,
                        (moduleY + ((FontUtil.getHeight() + 1F) * shownModules!!.indexOf(focusedModule))).toDouble(),
                        3.0,
                        FontUtil.getHeight().toDouble(),
                        3.0,
                        3.0,
                        3.0,
                        3.0,
                        color.value.rgb
                    )
                }
            }

            ClickGUI.Style.WINDOWS_98 -> {
                RenderUtil.drawRect(
                    x + 1,
                    y + 1,
                    catWidth,
                    catHeight,
                    Color(100, 100, 100).rgb
                )
                RenderUtil.drawRect(
                    x,
                    y,
                    catWidth,
                    catHeight,
                    Color(148, 148, 148).rgb
                )
                RenderUtil.drawRect(
                    x,
                    y + ((FontUtil.getHeight() + 1F) * Category.values().indexOf(focusedCategory)),
                    catWidth,
                    FontUtil.getHeight(),
                    Color(100, 100, 100).rgb
                )

                if (focusedModule != null && shownModules != null) {
                    moduleY = y + ((FontUtil.getHeight() + 1F) * Category.values().indexOf(focusedCategory))
                    RenderUtil.drawRect(
                        x + catWidth + 1,
                        moduleY + 1,
                        moduleWidth - 1,
                        moduleHeight,
                        Color(100, 100, 100).rgb
                    )
                    RenderUtil.drawRect(
                        x + catWidth,
                        moduleY,
                        moduleWidth - 1,
                        moduleHeight,
                        Color(148, 148, 148).rgb
                    )
                    RenderUtil.drawRect(
                        x + catWidth,
                        moduleY + ((FontUtil.getHeight() + 1F) * shownModules!!.indexOf(focusedModule)),
                        moduleWidth - 1,
                        FontUtil.getHeight(),
                        Color(100, 100, 100).rgb
                    )
                }
            }

            ClickGUI.Style.OLD -> {
                RenderUtil.drawRect(
                    x,
                    y,
                    catWidth,
                    catHeight,
                    Color(100, 100, 100, 100).rgb
                )

                RenderUtil.drawRect(
                    x,
                    y + ((FontUtil.getHeight() + 1F) * Category.values().indexOf(focusedCategory)),
                    catWidth,
                    FontUtil.getHeight(),
                    color.value.rgb
                )

                if (focusedModule != null && shownModules != null) {
                    moduleY = y + ((FontUtil.getHeight() + 1F) * Category.values().indexOf(focusedCategory))
                    RenderUtil.drawRect(
                        x + catWidth,
                        moduleY,
                        moduleWidth - 1,
                        moduleHeight,
                        Color(100, 100, 100, 100).rgb
                    )
                    RenderUtil.drawRect(
                        x + catWidth,
                        moduleY + ((FontUtil.getHeight() + 1F) * shownModules!!.indexOf(focusedModule)),
                        moduleWidth - 1,
                        FontUtil.getHeight(),
                        color.value.rgb
                    )
                }
            }

            ClickGUI.Style.DISCORD -> {
                RenderUtil.drawRoundedRect(
                    x.toDouble(),
                    y - 1.0,
                    (catWidth + FontUtil.getStringWidth("# ")).toDouble(),
                    catHeight + 1.0,
                    5.0,
                    5.0,
                    5.0,
                    5.0,
                    GuiDiscord.CHANNEL_BAR_BACKGROUND.rgb
                )
                RenderUtil.drawRoundedRect(
                    x.toDouble(),
                    (y + ((FontUtil.getHeight() + 1F) * Category.values().indexOf(focusedCategory))).toDouble() - 1.0,
                    (catWidth + FontUtil.getStringWidth("# ")).toDouble(),
                    FontUtil.getHeight() + 2.0,
                    3.5,
                    3.5,
                    3.5,
                    3.5,
                    GuiDiscord.CHANNEL_HOVERED_COLOR.rgb
                )


                if (focusedModule != null && shownModules != null) {
                    moduleY = y + ((FontUtil.getHeight() + 1F) * Category.values().indexOf(focusedCategory))
                    RenderUtil.drawRoundedRect(
                        (x + catWidth + FontUtil.getStringWidth("# ")).toDouble(),
                        moduleY.toDouble(),
                        (moduleWidth + FontUtil.getStringWidth("# ")).toDouble(),
                        moduleHeight + 2.0,
                        5.0,
                        5.0,
                        5.0,
                        5.0,
                        GuiDiscord.CHANNEL_BAR_BACKGROUND.rgb
                    )
                    RenderUtil.drawRoundedRect(
                        (x + catWidth + FontUtil.getStringWidth("# ")).toDouble(),
                        (moduleY + ((FontUtil.getHeight() + 1F) * shownModules!!.indexOf(focusedModule))).toDouble(),
                        (moduleWidth + FontUtil.getStringWidth("# ")).toDouble(),
                        FontUtil.getHeight().toDouble(),
                        3.5,
                        3.5,
                        3.5,
                        3.5,
                        GuiDiscord.CHANNEL_HOVERED_COLOR.rgb
                    )
                }
            }

            ClickGUI.Style.SIMPLE -> {
                RenderUtil.drawRect(
                    x,
                    y,
                    catWidth,
                    catHeight,
                    Color.BLACK.rgb
                )
                RenderUtil.drawRect(
                    x,
                    y + ((FontUtil.getHeight() + 1F) * Category.values().indexOf(focusedCategory)),
                    catWidth,
                    FontUtil.getHeight(),
                    color.value.darker().rgb
                )
                RenderUtil.drawBorder(
                    x,
                    y,
                    catWidth,
                    catHeight,
                    1F,
                    color.value.rgb
                )

                if (focusedModule != null && shownModules != null) {
                    moduleY = y + ((FontUtil.getHeight() + 1F) * Category.values().indexOf(focusedCategory))
                    RenderUtil.drawRect(
                        x + catWidth,
                        moduleY - 1,
                        moduleWidth,
                        moduleHeight + 2,
                        color.value.rgb
                    )
                    RenderUtil.drawRect(
                        x + catWidth,
                        moduleY,
                        moduleWidth - 1,
                        moduleHeight,
                        Color.BLACK.rgb
                    )
                    RenderUtil.drawRect(
                        x + catWidth,
                        moduleY + ((FontUtil.getHeight() + 1F) * shownModules!!.indexOf(focusedModule)),
                        moduleWidth - 1,
                        FontUtil.getHeight(),
                        color.value.darker().rgb
                    )

                    RenderUtil.drawRect(
                        x + catWidth,
                        y + catHeight,
                        1F,
                        ((shownModules!!.size * (FontUtil.getHeight() + 1F)) - catHeight) + ((Category.values().indexOf(focusedCategory) * (FontUtil.getHeight() + 1F))),
                        color.value.rgb
                    )
                }
            }

            ClickGUI.Style.PARAGON -> {
                RenderUtil.drawRect(
                    x,
                    y,
                    catWidth,
                    catHeight,
                    Color(40, 40, 60).rgb
                )

                RenderUtil.drawRect(
                    x,
                    y + ((FontUtil.getHeight() + 1F) * Category.values().indexOf(focusedCategory)),
                    catWidth,
                    FontUtil.getHeight() + 1F,
                    Color(60, 60, 80).rgb
                )

                if (focusedModule != null && shownModules != null) {
                    moduleY = y + ((FontUtil.getHeight() + 1F) * Category.values().indexOf(focusedCategory))

                    RenderUtil.drawRect(
                        x + catWidth,
                        moduleY,
                        moduleWidth - 1,
                        moduleHeight,
                        Color(40, 40, 60).rgb
                    )

                    RenderUtil.drawRect(
                        x + catWidth,
                        moduleY + ((FontUtil.getHeight() + 1F) * shownModules!!.indexOf(focusedModule)),
                        moduleWidth - 1,
                        FontUtil.getHeight(),
                        Color(60, 60, 80).rgb
                    )
                }
            }
        }

        //The most chinese 😭 (i'll probably clean this up)
        if (style.value == ClickGUI.Style.ZERODAY) {
            var catY = y + 1F
            for (cat in Category.values()) {
                FontUtil.drawStringWithShadow(
                    cat.Name,
                    if (cat == focusedCategory) x + 10F else x + 2F,
                    catY,
                    -1
                )
                catY += FontUtil.getHeight() + 1F
            }

            if (focusedModule != null && shownModules != null) {
                var modY = moduleY + 1F
                for (mod in shownModules!!) {
                    FontUtil.drawStringWithShadow(
                        mod.name,
                        x + catWidth + 17F,
                        modY,
                        -1
                    )
                    modY += FontUtil.getHeight() + 1F
                }
            }
        } else if (style.value == ClickGUI.Style.DISCORD) {
            var catY = y + 1F
            for (cat in Category.values()) {
                FontUtil.drawStringWithShadow(
                    "# ${cat.Name}",
                    x + 2F,
                    catY,
                    GuiDiscord.CHANNEL_TEXT_COLOR.rgb
                )
                catY += FontUtil.getHeight() + 1F
            }

            if (focusedModule != null && shownModules != null) {
                var modY = moduleY + 1F
                for (mod in shownModules!!) {
                    FontUtil.drawStringWithShadow(
                        "# ${mod.name}",
                        x + catWidth + 2F + FontUtil.getStringWidth("# "),
                        modY,
                        GuiDiscord.CHANNEL_TEXT_COLOR.rgb
                    )
                    modY += FontUtil.getHeight() + 1F
                }
            }
        } else {
            var catY = y + 1F
            for (cat in Category.values()) {
                FontUtil.drawStringWithShadow(
                    cat.Name,
                    x + 2F,
                    catY,
                    -1
                )
                catY += FontUtil.getHeight() + 1F
            }

            if (focusedModule != null && shownModules != null) {
                var modY = moduleY + 1F
                for (mod in shownModules!!) {
                    FontUtil.drawStringWithShadow(
                        mod.name,
                        x + catWidth + 2F,
                        modY,
                        -1
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
                } else {
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
                } else {
                    val index = shownModules!!.indexOf(focusedModule) - 1
                    focusedModule = shownModules!![if (index <= -1) shownModules!!.size - 1 else index]
                }
            }

            Keyboard.KEY_DOWN -> {
                if (focusedModule == null) {
                    val index = Category.values().indexOf(focusedCategory) + 1
                    focusedCategory = Category.values()[if (index >= Category.values().size) 0 else index]
                } else {
                    val index = shownModules!!.indexOf(focusedModule) + 1
                    focusedModule = shownModules!![if (index >= shownModules!!.size) 0 else index]
                }
            }
        }
    }

    override var width = 0F
        get() = if (style.value != ClickGUI.Style.ZERODAY) {
            catWidth
        } else if (style.value == ClickGUI.Style.DISCORD) {
            catWidth + FontUtil.getStringWidth("# ")
        } else {
            catWidth + 15F
        }

    override var height = 0F
        get() = catHeight

}