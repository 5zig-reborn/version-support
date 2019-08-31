/*
 * Copyright (c) 2019 5zig Reborn
 *
 * This file is part of The 5zig Mod
 * The 5zig Mod is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The 5zig Mod is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with The 5zig Mod.  If not, see <http://www.gnu.org/licenses/>.
 */

import eu.the5zig.mod.MinecraftFactory;
import eu.the5zig.mod.gui.IOverlay;
import eu.the5zig.mod.util.GLUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.achievement.GuiAchievement;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class Overlay extends GuiAchievement implements IOverlay {

	private static final ResourceLocation texture = new ResourceLocation("textures/gui/achievement/achievement_background.png");
	private static final Object LOCK = new Object();
	private static Overlay[] activeOverlays;

	public long timeStarted;
	public int offset;
	private Minecraft mc;
	private String title;
	private String subtitle;
	private int width;
	private int height;
	private int index = -1;

	Overlay() {
		super(Minecraft.getMinecraft());
		mc = Minecraft.getMinecraft();
	}

	public static void updateOverlayCount(int count) {
		activeOverlays = new Overlay[count];
	}

	public static void renderAll() {
		synchronized (LOCK) {
			for (int activeOverlaysSize = activeOverlays.length, i = activeOverlaysSize - 1; i >= 0; i--) {
				Overlay activeOverlay = activeOverlays[i];
				if (activeOverlay == null)
					continue;
				activeOverlay.render();
			}
		}
	}

	@Override
	public void displayMessage(String title, String subtitle) {
		this.title = MinecraftFactory.getVars().shortenToWidth(title, 140);
		this.subtitle = MinecraftFactory.getVars().shortenToWidth(subtitle, 140);

		this.timeStarted = MinecraftFactory.getVars().getSystemTime();

		synchronized (LOCK) {
			for (int i = 0; i < activeOverlays.length; i++) {
				if (activeOverlays[i] == null) {
					setOffset(i);
					break;
				}
			}
			if (index == -1) {
				setOffset(activeOverlays.length - 1);
			}
		}
	}

	@Override
	public void displayMessage(String title, String subtitle, Object uniqueReference) {
		displayMessage(title, subtitle);
	}

	private void setOffset(int index) {
		this.index = index;
		activeOverlays[index] = this;
		this.offset = index * 32;
	}

	@Override
	public void displayMessage(String message) {
		displayMessage("The 5zig Mod", message);
	}

	@Override
	public void displayMessage(String message, Object uniqueReference) {
		displayMessage(message);
	}

	@Override
	public void displayMessageAndSplit(String message) {
		List<String> split = MinecraftFactory.getVars().splitStringToWidth(message, 140);
		String title = null;
		String subTitle = null;
		for (int i = 0; i < split.size(); i++) {
			if (i == 0)
				title = split.get(0);
			if (i == 1)
				subTitle = split.get(1);
		}
		displayMessage(title, subTitle);
	}

	@Override
	public void displayMessageAndSplit(String message, Object uniqueReference) {
		displayMessageAndSplit(message);
	}

	private void updateScale() {
		GlStateManager.viewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
		GLUtil.matrixMode(GL11.GL_PROJECTION);
		GLUtil.loadIdentity();
		GLUtil.matrixMode(GL11.GL_MODELVIEW);
		GLUtil.loadIdentity();
		this.width = this.mc.displayWidth;
		this.height = this.mc.displayHeight;
		ScaledResolution scaledResolution = new ScaledResolution(this.mc);
		this.width = scaledResolution.getScaledWidth();
		this.height = scaledResolution.getScaledHeight();
		GLUtil.clear(256);
		GLUtil.matrixMode(GL11.GL_PROJECTION);
		GLUtil.loadIdentity();
		GlStateManager.ortho(0.0D, (double) this.width, (double) this.height, 0.0D, 1000.0D, 3000.0D);
		GLUtil.matrixMode(GL11.GL_MODELVIEW);
		GLUtil.loadIdentity();
		GLUtil.translate(0.0F, 0.0F, -2000.0F);
	}

	private void render() {
		if ((this.mc == null) || (this.timeStarted == 0L)) {
			return;
		}
		double delta = (MinecraftFactory.getVars().getSystemTime() - this.timeStarted) / 3000.0D;
		if (delta < 0.0D || delta > 1.0D) {
			this.timeStarted = 0L;
			activeOverlays[index] = null;
			return;
		}

		updateScale();

		GLUtil.disableDepth();
		GLUtil.depthMask(false);
		delta *= 2.0D;
		if (delta > 1.0D) {
			delta = 2.0D - delta;
		}
		delta *= 4.0D;
		delta = 1.0D - delta;
		if (delta < 0.0D) {
			delta = 0.0D;
		}
		delta = Math.pow(delta, 3);

		int x = width - 160;
		int y = offset - (int) (delta * 32.0D);
		GLUtil.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableTexture2D();
		MinecraftFactory.getVars().bindTexture(texture);
		GlStateManager.disableLighting();

		drawTexturedModalRect(x, y, 96, 202, 160, 32);
		if (this.title != null) {
			MinecraftFactory.getVars().drawString(this.title, x + 5, y + 7, -256);
		}
		if (this.subtitle != null) {
			MinecraftFactory.getVars().drawString(this.subtitle, x + 5, y + 18, -1);
		}
		RenderHelper.enableGUIStandardItemLighting();
		GlStateManager.disableLighting();
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableColorMaterial();

		GlStateManager.enableLighting();
		GlStateManager.disableLighting();

		GLUtil.depthMask(true);
		GLUtil.enableDepth();
	}

	@Override
	public void clearAchievements() {
		this.timeStarted = 0L;
		this.title = null;
		this.subtitle = null;
	}

}