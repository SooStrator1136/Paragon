package com.paragon.mixins.accessor;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import net.minecraft.util.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface IMinecraft {

    @Accessor("rightClickDelayTimer")
    void setRightClickDelayTimer(int newTimer);

    @Accessor("timer")
    Timer getTimer();

    @Accessor("session")
    Session getSession();

    @Accessor("session")
    void setSession(Session newSession);

}
