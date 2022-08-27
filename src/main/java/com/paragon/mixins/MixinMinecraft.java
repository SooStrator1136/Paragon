package com.paragon.mixins;

import com.paragon.Paragon;
import com.paragon.client.ui.menu.ParagonMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Shadow
    public WorldClient world;

    @Shadow
    public GameSettings gameSettings;

    @Shadow
    public GuiScreen currentScreen;

    @Inject(method = "displayGuiScreen", at = @At("HEAD"), cancellable = true)
    public void onDisplayGuiScreen(GuiScreen guiScreenIn, CallbackInfo ci) {
        if (guiScreenIn instanceof GuiMainMenu && Paragon.INSTANCE.isParagonMainMenu()) {
            Minecraft.getMinecraft().displayGuiScreen(new ParagonMenu());
            ci.cancel();
        }
    }

    /**
     * @author Surge
     * @reason GUIs can look laggy when not ingame without this
     */
    @Overwrite
    public int getLimitFramerate() {
        return world == null && this.currentScreen != null ? MathHelper.clamp(this.gameSettings.limitFramerate, 0, 240) : this.gameSettings.limitFramerate;
    }

}
