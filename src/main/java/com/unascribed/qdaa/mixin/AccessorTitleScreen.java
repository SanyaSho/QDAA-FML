package com.unascribed.qdaa.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.screen.TitleScreen;

@Mixin(TitleScreen.class)
public interface AccessorTitleScreen {

	@Accessor("splashText")
	void qdaa$setSplashText(String splashText);
	
}
