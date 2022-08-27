package com.paragon.mixins.render.gui;

import com.paragon.Paragon;
import com.paragon.api.event.render.gui.RenderChatEvent;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiNewChat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GuiNewChat.class)
public class MixinGuiNewChat {

    @Redirect(method = "drawChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiNewChat;drawRect(IIIII)V"))
    public void onDrawChatRect(int x, int y, int x2, int y2, int colour) {
        RenderChatEvent chatEvent = new RenderChatEvent(colour);
        Paragon.INSTANCE.getEventBus().post(chatEvent);

        if (!chatEvent.isCancelled()) {
            Gui.drawRect(x, y, x2, y2, chatEvent.getColour());
        }
    }

}
