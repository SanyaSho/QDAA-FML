package com.unascribed.qdaa.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.screens.TitleScreen;

@Mixin(TitleScreen.class)
public interface AccessorTitleScreen {

	@Accessor("splash")
	void qdaa$setSplashText(String splashText);
	
}
