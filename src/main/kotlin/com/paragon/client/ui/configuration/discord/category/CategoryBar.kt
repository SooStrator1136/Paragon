package com.paragon.client.ui.configuration.discord.category

import com.paragon.Paragon
import com.paragon.api.module.Category
import com.paragon.client.ui.configuration.discord.GuiDiscord
import com.paragon.client.ui.configuration.discord.IRenderable
import com.paragon.client.ui.configuration.discord.module.DiscordModule
import com.paragon.client.ui.configuration.discord.module.ModuleBar
import com.paragon.client.ui.configuration.discord.settings.SettingsBar
import net.minecraft.client.gui.Gui
import org.lwjgl.util.Rectangle

/**
 * @author SooStrator1136
 */
object CategoryBar : IRenderable {

    val rect = Rectangle(
        GuiDiscord.BASE_RECT.x,
        GuiDiscord.BASE_RECT.y,
        GuiDiscord.BASE_RECT.x + (GuiDiscord.BASE_RECT.width / 10),
        GuiDiscord.BASE_RECT.height
    )
    private val categories = arrayOfNulls<DiscordCategory>(Category.values().size)

    init {
        val catAmount = Category.values().size
        for (i in 0 until catAmount) {
            categories[i] = DiscordCategory(Category.values()[i])
        }
    }

    override fun render(mouseX: Int, mouseY: Int) {
        Gui.drawRect(
            rect.x,
            rect.y,
            rect.x + rect.width,
            rect.y + rect.height,
            GuiDiscord.CATEGORY_BAR_BACKGROUND.rgb
        )

        val catAmount = categories.size
        val catHeight = ((rect.height / catAmount) / 1.25).toInt()
        val catOffset = ((rect.height) - (catHeight * catAmount)) / (catAmount - 1)
        var currY = rect.y
        for (i in 0 until catAmount) {
            (categories[i] ?: continue).rect.setBounds(rect.x, currY, rect.width, catHeight)
            currY = rect.y + catHeight * (i + 1) + catOffset * (i + 1)
        }

        for (category in categories) {
            category?.render(mouseX, mouseY)
        }

        rect.setBounds(
            GuiDiscord.BASE_RECT.x,
            GuiDiscord.BASE_RECT.y,
            catHeight,
            GuiDiscord.BASE_RECT.height
        )
    }

    override fun onClick(mouseX: Int, mouseY: Int, button: Int) {
        if (!rect.contains(mouseX, mouseY)) {
            return
        }

        for (category in categories) {
            if ((category ?: continue).rect.contains(mouseX, mouseY)) {
                if (ModuleBar.shownModules.isNotEmpty() && ModuleBar.shownModules[0].module.category == category.category) {
                    return
                }
                ModuleBar.focusedModule = null
                ModuleBar.shownModules.clear()
                ModuleBar.scrollOffset = 0
                Paragon.INSTANCE.moduleManager.getModulesThroughPredicate { it.category == category.category }.forEach {
                    ModuleBar.shownModules.add(DiscordModule(it))
                }
                SettingsBar.shownSettings.clear()
                break
            }
        }
    }

    override fun onRelease(mouseX: Int, mouseY: Int, button: Int) {}

    override fun onKey(keyCode: Int) {}

}