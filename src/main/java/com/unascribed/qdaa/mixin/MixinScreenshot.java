package com.unascribed.qdaa.mixin;

import static org.lwjgl.system.MemoryUtil.*;

import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.stb.STBImageResize.*;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.unascribed.qdaa.QDAA;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;

@Mixin(Screenshot.class)
public class MixinScreenshot {

	@Inject(at=@At("HEAD"), method="Lnet/minecraft/client/Screenshot;takeScreenshot(Lcom/mojang/blaze3d/pipeline/RenderTarget;)Lcom/mojang/blaze3d/platform/NativeImage;", cancellable=true, require=0)
	private static void takeScreenshot(RenderTarget renderTarget, CallbackInfoReturnable<NativeImage> cir) {
		if (QDAA.isEnabled() && renderTarget == Minecraft.getInstance().getMainRenderTarget()) {
			int w = renderTarget.width;
			int h = renderTarget.height;
			var data = memAlloc(w*h*4);
			try {
				renderTarget.bindRead();
				glGetTexImage(GL_TEXTURE_2D, 0, GL_RGBA, GL_UNSIGNED_BYTE, data);
				for (int i = 0; i < data.limit(); i += 4)
					data.put(i+3, (byte)0xFF);
				var img = new NativeImage(w/2, h/2, false);
				var odata = MemoryUtil.memByteBuffer(((AccessorNativeImage)(Object)img).qdaa$getPointer(), w*h*2);
				stbir_resize_uint8_generic(data, w, h, 0,
						odata, w/2, h/2, 0,
						4, 0, 3, STBIR_EDGE_CLAMP,
						STBIR_FILTER_BOX, STBIR_COLORSPACE_LINEAR);
				img.flipY();
				cir.setReturnValue(img);
			} finally {
				memFree(data);
			}
		}
	}
	
}
