package com.paragon.client.ui.alt

import com.paragon.Paragon
import com.paragon.api.util.Wrapper
import com.paragon.api.util.render.RenderUtil.drawRect
import com.paragon.api.util.render.RenderUtil.popScissor
import com.paragon.api.util.render.RenderUtil.pushScissor
import com.paragon.api.util.render.font.FontUtil.drawStringWithShadow
import com.paragon.api.util.render.font.FontUtil.renderCenteredString
import com.paragon.mixins.accessor.IMinecraft
import com.paragon.client.managers.alt.Alt
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiMainMenu
import net.minecraft.client.gui.GuiMultiplayer
import net.minecraft.client.gui.GuiScreen
import net.minecraft.util.text.TextFormatting
import org.lwjgl.input.Mouse
import java.io.IOException
import java.util.function.Consumer

class AltManagerGUI : GuiScreen(), Wrapper {

    private val altEntries = ArrayList<AltEntry?>(3)

    override fun initGui() {
        renderString = TextFormatting.GRAY.toString() + "Idle"
        altEntries.clear()
        var offset = 150f

        for (alt in Paragon.INSTANCE.altManager.alts) {
            altEntries.add(AltEntry(alt, offset))
            offset += 20.0f
        }

        buttonList.add(GuiButton(0, 5, 5, 75, 20, "Back"))
        buttonList.add(GuiButton(1, width / 2 - 80, height - 25, 75, 20, "Add Alt"))
        buttonList.add(GuiButton(2, width / 2 + 5, height - 25, 75, 20, "Delete"))
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
        buttonList[2].enabled = selectedAltEntry != null

        scroll()

        drawRect(0f, 150f, width.toFloat(), 200f, -0x70000000)

        pushScissor(0.0, 150.0, width.toDouble(), 200.0)

        altEntries.forEach(Consumer { altEntry: AltEntry? -> altEntry!!.drawAlt(mouseX, mouseY, width) })

        popScissor()

        drawStringWithShadow("Logged in as " + TextFormatting.GRAY + (Minecraft.getMinecraft() as IMinecraft).session.username, 5f, 30f, -1)

        renderCenteredString("Paragon Alt Manager", width / 2f, 75f, -1, false)
        renderCenteredString(renderString, width / 2f, 100f, -1, false)

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    private fun scroll() {
        val scroll = Mouse.getDWheel()

        if (scroll > 0) {
            if (altEntries[0]!!.offset < 150) {
                for (altEntry in altEntries) {
                    altEntry!!.offset = altEntry.offset + 10
                }
            }
            return
        }

        if (scroll < 0) {
            if (altEntries[altEntries.size - 1]!!.offset > 340) {
                for (altEntry in altEntries) {
                    altEntry!!.offset = altEntry.offset - 10
                }
            }
        }
    }

    @Throws(IOException::class)
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        altEntries.forEach(Consumer { altEntry: AltEntry? ->
            if (isHovered(0f, 150f, width.toFloat(), 350f, mouseX, mouseY)) {
                if (isHovered(0f, altEntry!!.offset, width.toFloat(), altEntry.offset + 20, mouseX, mouseY)) {
                    if (selectedAltEntry == altEntry) {
                        renderString = "Logging in with the email: " + altEntry.alt.email
                        altEntry.clicked(mouseX, mouseY, width)
                    }
                    else {
                        selectedAltEntry = altEntry
                    }
                }
            }
        })

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            0 -> minecraft.displayGuiScreen(GuiMultiplayer(GuiMainMenu()))
            1 -> minecraft.displayGuiScreen(AddAltGUI())
            2 -> {
                Paragon.INSTANCE.altManager.alts.removeIf { alt: Alt -> alt.email == selectedAltEntry!!.alt.email && alt.password == selectedAltEntry!!.alt.password }
                altEntries.remove(selectedAltEntry)
            }
        }
    }

    override fun onGuiClosed() {
        Paragon.INSTANCE.storageManager.saveAlts()
    }

    companion object {
        var selectedAltEntry: AltEntry? = null
        var renderString = TextFormatting.GRAY.toString() + "Idle"
    }
}