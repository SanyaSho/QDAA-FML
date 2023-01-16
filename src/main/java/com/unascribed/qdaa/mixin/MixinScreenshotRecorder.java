package com.unascribed.qdaa.mixin;

import static org.lwjgl.system.MemoryUtil.*;

import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.stb.STBImageResize.*;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.blaze3d.framebuffer.Framebuffer;
import com.mojang.blaze3d.texture.NativeImage;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.ScreenshotRecorder;

@Mixin(ScreenshotRecorder.class)
public class MixinScreenshotRecorder {

	@Inject(at=@At("HEAD"), method="takeScreenshot", cancellable=true, require=0)
	private static void takeScreenshot(Framebuffer framebuffer, CallbackInfoReturnable<NativeImage> cir) {
		if (framebuffer == MinecraftClient.getInstance().getFramebuffer()) {
			int w = framebuffer.textureWidth;
			int h = framebuffer.textureHeight;
			var data = memAlloc(w*h*4);
			try {
				framebuffer.bindColorAttachmentAsTexture();
				glGetTexImage(GL_TEXTURE_2D, 0, GL_RGBA, GL_UNSIGNED_BYTE, data);
				var img = new NativeImage(w/2, h/2, false);
				var odata = MemoryUtil.memByteBuffer(((AccessorNativeImage)(Object)img).qdaa$getPointer(), w*h*2);
				stbir_resize_uint8_generic(data, w, h, 0,
						odata, w/2, h/2, 0,
						4, 0, 3, STBIR_EDGE_CLAMP,
						STBIR_FILTER_BOX, STBIR_COLORSPACE_LINEAR);
				img.mirrorVertically();
				cir.setReturnValue(img);
			} finally {
				memFree(data);
			}
		}
	}
	
}
