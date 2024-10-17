package com.unascribed.qdaa;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.io.Files;
import com.google.common.io.Resources;

import net.minecraft.client.Minecraft;

// FML
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("qdaa")
public class QDAA {

	public static boolean windowSupersampled = false;
	private static boolean enabled = true;

	public QDAA(FMLJavaModLoadingContext context) {
		ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((mc, current) -> new QDAAScreen( current )));
	}
	
	public static boolean isEnabled() {
		return enabled || Minecraft.getInstance().screen instanceof QDAAScreen;
	}
	
	public static boolean isConfigEnabled() {
		return enabled;
	}
	
	public static void setEnabled(boolean enabled) {
		QDAA.enabled = enabled;
		updateWindow();
	}
	
	public static boolean updateWindow() {
		boolean enabled = isEnabled();
		if (windowSupersampled != enabled) {
			windowSupersampled = enabled;
			var w = Minecraft.getInstance().getWindow();
			if (!windowSupersampled) {
				w.setWidth(w.getWidth()/2);
				w.setHeight(w.getHeight()/2);
			} else {
				w.setWidth(w.getWidth()*2);
				w.setHeight(w.getHeight()*2);
			}
			Minecraft.getInstance().resizeDisplay();
			return true;
		}
		return false;
	}

	static {
		var f = new File("config/qdaa.ini");
		try {
			if (!f.exists()) {
				Files.createParentDirs(f);
				writeConfig();
			}
			var s = Splitter.on('=').trimResults().limit(2);
			var entries = Files.asCharSource(f, Charsets.UTF_8).lines()
				.filter(l -> !l.isBlank() && !l.startsWith(";"))
				.map(l -> s.splitToList(l))
				.toList();
			for (var en : entries) {
				if ("enabled".equals(en.get(0))) {
					enabled = Boolean.parseBoolean(en.get(1));
				}
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public static void writeConfig() {
		var f = new File("config/qdaa.ini");
		var src = Resources.asCharSource(QDAA.class.getClassLoader().getResource("qdaa-config.ini"), Charsets.UTF_8);
		var sink = Files.asCharSink(f, Charsets.UTF_8);
		try {
			sink.write(src.read().replace("{enabled}", Boolean.toString(enabled)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
