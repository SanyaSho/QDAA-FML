package com.unascribed.qdaa.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.unascribed.qdaa.QDAA;

import static org.lwjgl.opengl.GL33.*;

import net.minecraft.client.Minecraft;

@Mixin(RenderTarget.class)
public class MixinRenderTarget {

	@ModifyConstant(constant=@Constant(intValue=GL_NEAREST), method="Lcom/mojang/blaze3d/pipeline/RenderTarget;createBuffers(IIZ)V")
	public int qdaa$linearFiltering(int orig) {
		RenderTarget self = (RenderTarget)(Object)this;
		if (QDAA.isEnabled() && self == Minecraft.getInstance().getMainRenderTarget()) {
			return GL_LINEAR;
		}
		return orig;
	}
	
	@Inject(at=@At("HEAD"), method="Lcom/mojang/blaze3d/pipeline/RenderTarget;blitToScreen(IIZ)V", cancellable=true)
	public void qdaa$sssaDraw(int w, int h, boolean disableBlend, CallbackInfo ci) {
		RenderTarget self = (RenderTarget)(Object)this;
		if (QDAA.isEnabled() && self == Minecraft.getInstance().getMainRenderTarget()) {
			glBindFramebuffer(GL_READ_FRAMEBUFFER, self.frameBufferId);
			glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
			glBlitFramebuffer(0, 0, w, h, 0, 0, w/2, h/2, GL_COLOR_BUFFER_BIT, GL_LINEAR);
			ci.cancel();
		}
	}
	
}
