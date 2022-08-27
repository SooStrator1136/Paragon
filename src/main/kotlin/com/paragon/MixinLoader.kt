package com.paragon

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin
import org.spongepowered.asm.launch.MixinBootstrap
import org.spongepowered.asm.mixin.MixinEnvironment
import org.spongepowered.asm.mixin.Mixins

@IFMLLoadingPlugin.Name("ParagonMixinLoader")
@IFMLLoadingPlugin.MCVersion("1.12.2")
class MixinLoader : IFMLLoadingPlugin {

    init {
        MixinBootstrap.init()
        Mixins.addConfiguration("mixins.paragon.json")
        MixinEnvironment.getDefaultEnvironment().obfuscationContext = "searge"

        Paragon.INSTANCE.logger.info("Mixins loaded with context '${MixinEnvironment.getDefaultEnvironment().obfuscationContext}'")
    }

    override fun getASMTransformerClass(): Array<String> {
        return emptyArray()
    }

    override fun getModContainerClass(): String? {
        return null
    }

    override fun getSetupClass(): String? {
        return null
    }

    override fun injectData(data: Map<String, Any>) {

    }

    override fun getAccessTransformerClass(): String? {
        return null
    }
}