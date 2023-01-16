package com.unascribed.qdaa.mixin;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.blaze3d.glfw.Window;

@Mixin(Window.class)
public class MixinWindow {

	@ModifyVariable(at=@At("HEAD"), method="onFramebufferSizeChanged", ordinal=0)
	public int qdaa$sssaWidth(int orig) {
		return orig*2;
	}
	
	@ModifyVariable(at=@At("HEAD"), method="onFramebufferSizeChanged", ordinal=1)
	public int qdaa$sssaHeight(int orig) {
		return orig*2;
	}
	
	@Redirect(at=@At(value="INVOKE", target="org/lwjgl/glfw/GLFW.glfwGetFramebufferSize(J[I[I)V", remap=false),
			method="updateFramebufferSize")
	public void qdaa$modifyInitialSize(long window, int[] w, int[] h) {
		GLFW.glfwGetFramebufferSize(window, w, h);
		w[0] *= 2;
		h[0] *= 2;
	}
	
}
