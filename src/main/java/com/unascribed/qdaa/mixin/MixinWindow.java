package com.unascribed.qdaa.mixin;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.blaze3d.platform.Window;
import com.unascribed.qdaa.QDAA;

@Mixin(Window.class)
public class MixinWindow {
	
	@ModifyVariable(at=@At("HEAD"), method="onFramebufferResize", ordinal=0)
	public int qdaa$sssaWidth(int orig) {
		if (QDAA.isEnabled()) {
			QDAA.windowSupersampled = true;
			return orig*2;
		}
		return orig;
	}
	
	@ModifyVariable(at=@At("HEAD"), method="onFramebufferResize", ordinal=1)
	public int qdaa$sssaHeight(int orig) {
		return QDAA.isEnabled() ? orig*2 : orig;
	}
	
	@Redirect(at=@At(value="INVOKE", target="org/lwjgl/glfw/GLFW.glfwGetFramebufferSize(J[I[I)V", remap=false),
			method="refreshFramebufferSize")
	public void qdaa$modifyInitialSize(long window, int[] w, int[] h) {
		if (QDAA.isEnabled()) {
			QDAA.windowSupersampled = true;
			GLFW.glfwGetFramebufferSize(window, w, h);
			w[0] *= 2;
			h[0] *= 2;
		} else {
			QDAA.windowSupersampled = false;
		}
	}
	
}
