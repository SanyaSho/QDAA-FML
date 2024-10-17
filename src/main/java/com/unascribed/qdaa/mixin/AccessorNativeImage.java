package com.unascribed.qdaa.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.blaze3d.platform.NativeImage;

@Mixin(NativeImage.class)
public interface AccessorNativeImage {

	@Accessor("pixels")
	long qdaa$getPointer();
	
}
