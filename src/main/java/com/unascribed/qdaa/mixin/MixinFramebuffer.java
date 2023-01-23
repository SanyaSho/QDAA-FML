package com.unascribed.qdaa.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.framebuffer.Framebuffer;
import com.unascribed.qdaa.QDAA;

import static org.lwjgl.opengl.GL33.*;

import net.minecraft.client.MinecraftClient;

@Mixin(Framebuffer.class)
public class MixinFramebuffer {

	@ModifyConstant(constant=@Constant(intValue=GL_NEAREST), method="create")
	public int qdaa$linearFiltering(int orig) {
		Object self = this;
		if (QDAA.isEnabled() && self == MinecraftClient.getInstance().getFramebuffer()) {
			return GL_LINEAR;
		}
		return orig;
	}
	
	@Inject(at=@At("HEAD"), method="drawInternal", cancellable=true)
	public void qdaa$sssaDraw(int w, int h, boolean disableBlend, CallbackInfo ci) {
		Framebuffer self = (Framebuffer)(Object)this;
		if (QDAA.isEnabled() && self == MinecraftClient.getInstance().getFramebuffer()) {
			glBindFramebuffer(GL_READ_FRAMEBUFFER, self.framebufferId);
			glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
			glBlitFramebuffer(0, 0, w, h, 0, 0, w/2, h/2, GL_COLOR_BUFFER_BIT, GL_LINEAR);
			ci.cancel();
		}
	}
	
}
