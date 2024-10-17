package com.unascribed.qdaa;

import com.unascribed.qdaa.mixin.AccessorTitleScreen;

import static org.lwjgl.opengl.GL33.*;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.components.Button;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import com.mojang.math.Vector3f;

public class QDAAScreen extends Screen {

	private final Screen parent;
	private Screen demoScreen;
	
	public QDAAScreen(Screen parent) {
		super(Component.literal("QDAA"));
		this.parent = parent;
	}
	
	@Override
	protected void init() {
		if (QDAA.updateWindow()) return;
		addRenderableWidget(new Button((width/2)+1, height-20, 100, 20, Component.translatable("gui.done"), (b) -> {
			onClose();
		}));
		addRenderableWidget(new Button((width/2)-101, height-20, 100, 20, Component.translatable("options."+(QDAA.isConfigEnabled()?"on":"off")+".composed", "2xSSAA"), (b) -> {
			QDAA.setEnabled(!QDAA.isConfigEnabled());
			QDAA.writeConfig();
			b.setMessage(Component.translatable("options."+(QDAA.isConfigEnabled()?"on":"off")+".composed", "2xSSAA"));
		}));
		if (minecraft.level == null) {
			if (demoScreen == null) {
				demoScreen = new TitleScreen(false);
				((AccessorTitleScreen)demoScreen).qdaa$setSplashText("This long splash helps illustrate supersampling, especially in small windows!");
			}
			demoScreen.init(minecraft, width, height);
		}
	}
	
	@Override
	public void onClose() {
		minecraft.setScreen(parent);
		QDAA.updateWindow();
	}
	
	@Override
	public void tick() {
		super.tick();
		if (demoScreen != null) {
			demoScreen.tick();
		}
	}
	
	@Override
	public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
		if (demoScreen != null) {
			matrices.pushPose();
				matrices.translate(0, 0, -200);
				demoScreen.render(matrices, -400, -400, delta);
			matrices.popPose();
			fill(matrices, 0, 0, width, height, 0x44000000);
		}
		
		var fb = minecraft.getMainRenderTarget();
		var w = minecraft.getWindow();
		
		var ssWidth = fb.width;
		var ssHeight = fb.height;
		var winWidth = w.getScreenWidth();
		var winHeight = w.getScreenHeight();
		
		// draw some demo stuff

		fill(matrices, 0, 0, width, 1, -1);
		for (int i = 0; i < 2; i++) {
			int x = ((width/2)*i);
			minecraft.getItemRenderer().renderGuiItem(Items.LIGHT_GRAY_GLAZED_TERRACOTTA.getDefaultInstance(), x+30, 30);
			minecraft.getItemRenderer().renderGuiItem(Items.CRYING_OBSIDIAN.getDefaultInstance(), x+55, 50);
			matrices.pushPose();
				matrices.translate(x+60, 30, 0);
				matrices.mulPose(Vector3f.ZP.rotationDegrees(30));
				fill(matrices, 1, 1, 3, 41, 0xFF444444);
				fill(matrices, 0, 0, 2, 40, -1);
			matrices.popPose();
		}
		
		// downsample nearest/linear to window size for demonstration
		// we're using the window buffer as scratch space
		glBindFramebuffer(GL_READ_FRAMEBUFFER, fb.frameBufferId);
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
		// left half; nearest downsample (equivalent to drawing without supersampling)
		if (demoScreen != null) {
			// for the title screen, just draw the right half twice since the splash text is the main interesting part
			glBlitFramebuffer(
					ssWidth/2, 0, ssWidth, ssHeight,
					0, 0, winWidth/2, winHeight,
					GL_COLOR_BUFFER_BIT, GL_NEAREST);
		} else {
			glBlitFramebuffer(
					0, 0, ssWidth/2, ssHeight,
					0, 0, winWidth/2, winHeight,
					GL_COLOR_BUFFER_BIT, GL_NEAREST);
		}
		// right half; linear downsample (equivalent to final render step)
		glBlitFramebuffer(
				ssWidth/2, 0, ssWidth, ssHeight,
				winWidth/2, 0, winWidth, winHeight,
				GL_COLOR_BUFFER_BIT, GL_LINEAR);
		
		// put it back on the framebuffer
		glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, fb.frameBufferId);
		// upsample back to the framebuffer size with nearest since we have to
		// this is wasted work since it'll get downsampled again at the end of the frame, but oh well
		glBlitFramebuffer(
				0, 0, winWidth/2, winHeight,
				0, 0, ssWidth/2, ssHeight,
				GL_COLOR_BUFFER_BIT, GL_NEAREST);
		glBlitFramebuffer(
				winWidth/2, 0, winWidth, winHeight,
				ssWidth/2, 0, ssWidth, ssHeight,
				GL_COLOR_BUFFER_BIT, GL_NEAREST);
		
		
		if (minecraft.isWindowActive()) {
			// draw the mouse lens
			int lensSize = 72;
			int lensSizeH = lensSize/3;
			int lensSizeB = lensSize+4;
			int rawMouseX = (int)(minecraft.mouseHandler.xpos());
			int rawMouseY = (int)(minecraft.mouseHandler.ypos());
			rawMouseX = Mth.clamp(rawMouseX, lensSize, winWidth-lensSize);
			rawMouseY = Mth.clamp(rawMouseY, lensSize, winHeight-lensSize);
			rawMouseY = winHeight-rawMouseY;
			
			// copy the finished side-by-side back into scratch space
			glBindFramebuffer(GL_READ_FRAMEBUFFER, fb.frameBufferId);
			glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
			glBlitFramebuffer(
					0, 0, ssWidth, ssHeight,
					0, 0, winWidth, winHeight,
					GL_COLOR_BUFFER_BIT, GL_NEAREST);
			
			// and back, at double size
			glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
			glBindFramebuffer(GL_DRAW_FRAMEBUFFER, fb.frameBufferId);
			// draw the border by copying a pixel of our top white border
			glBlitFramebuffer(
					0, winHeight-1, 1, winHeight,
					(rawMouseX-lensSizeB)*2, (rawMouseY-lensSizeB)*2, (rawMouseX+lensSizeB)*2, (rawMouseY+lensSizeB)*2,
					GL_COLOR_BUFFER_BIT, GL_NEAREST);
			// draw the contents of the lens
			glBlitFramebuffer(
					rawMouseX-lensSizeH, rawMouseY-lensSizeH, rawMouseX+lensSizeH, rawMouseY+lensSizeH,
					(rawMouseX-lensSize)*2, (rawMouseY-lensSize)*2, (rawMouseX+lensSize)*2, (rawMouseY+lensSize)*2,
					GL_COLOR_BUFFER_BIT, GL_NEAREST);
		}
		
		// draw the dividing line
		fill(matrices, (width/2)-1, 0, (width/2)+1, height, -1);

		drawCenteredString(matrices, font, Component.translatable("options.off"), width/4, 20, -1);
		drawCenteredString(matrices, font, Component.translatable("options.on"), (width*3)/4, 20, -1);
		super.render(matrices, mouseX, mouseY, delta);
	}

}
