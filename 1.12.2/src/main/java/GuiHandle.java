/*
 * Copyright (c) 2019-2020 5zig Reborn
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
import eu.the5zig.mod.gui.Gui;
import eu.the5zig.mod.gui.IGuiHandle;
import eu.the5zig.mod.gui.elements.IButton;
import eu.the5zig.mod.util.GLUtil;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import java.io.IOException;
import java.util.List;

public class GuiHandle extends GuiScreen implements IGuiHandle {

	private Gui child;

	public GuiHandle(Gui child) {
		this.child = child;
	}

	@Override
	public void initGui() {
		child.initGui0();
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		child.actionPerformed0((IButton) button);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		callDrawDefaultBackground();
		child.drawScreen0(mouseX, mouseY, partialTicks);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void updateScreen() {
		child.tick0();
	}

	@Override
	public void handleMouseInput() throws IOException {
		child.handleMouseInput0();
		super.handleMouseInput();
	}

	@Override
	public void onGuiClosed() {
		child.guiClosed0();
	}

	@Override
	protected void keyTyped(char c, int i) {
		child.keyTyped0(c, i);
		child.keyPressed0(i, 0, 0);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
		super.mouseClicked(mouseX, mouseY, button);
		child.mouseClicked0(mouseX, mouseY, button);
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);
		child.mouseReleased0(mouseX, mouseY, state);
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void setResolution(int width, int height) {
		setWorldAndResolution(((Variables) MinecraftFactory.getVars()).getMinecraft(), width, height);
	}

	@Override
	public void callDrawDefaultBackground() {
		drawDefaultBackground();
	}

	@Override
	public void drawMenuBackground() {
		drawBackground(0);
	}

	@Override
	public void callDrawTexturedModalRect(int x, int y, int texX, int texY, int width, int height) {
		drawTexturedModalRect(x, y, texX, texY, width, height);
	}

	@Override
	public void callDrawHoveringText(List<String> lines, int x, int y) {
		if (!lines.isEmpty()) {
			GlStateManager.disableRescaleNormal();
			RenderHelper.disableStandardItemLighting();
			GLUtil.disableLighting();
			int maxWidth = 0;

			for (String line : lines) {
				int width = MinecraftFactory.getVars().getStringWidth(line);
				if (width > maxWidth) {
					maxWidth = width;
				}
			}

			int x1 = x + 12;
			int y1 = y - 12;
			int height = 8;
			if (lines.size() > 1) {
				height += 2 + (lines.size() - 1) * 10;
			}

			if (x1 + maxWidth > getWidth()) {
				x1 -= 28 + maxWidth;
			}

			if (y1 + height + 6 > getHeight()) {
				y1 = getHeight() - height - 6;
			}

			this.drawGradientRect(x1 - 3, y1 - 4, x1 + maxWidth + 3, y1 - 3, -267386864, -267386864);
			this.drawGradientRect(x1 - 3, y1 + height + 3, x1 + maxWidth + 3, y1 + height + 4, -267386864, -267386864);
			this.drawGradientRect(x1 - 3, y1 - 3, x1 + maxWidth + 3, y1 + height + 3, -267386864, -267386864);
			this.drawGradientRect(x1 - 4, y1 - 3, x1 - 3, y1 + height + 3, -267386864, -267386864);
			this.drawGradientRect(x1 + maxWidth + 3, y1 - 3, x1 + maxWidth + 4, y1 + height + 3, -267386864, -267386864);
			this.drawGradientRect(x1 - 3, y1 - 3 + 1, x1 - 3 + 1, y1 + height + 3 - 1, 1347420415, 1344798847);
			this.drawGradientRect(x1 + maxWidth + 2, y1 - 3 + 1, x1 + maxWidth + 3, y1 + height + 3 - 1, 1347420415, 1344798847);
			this.drawGradientRect(x1 - 3, y1 - 3, x1 + maxWidth + 3, y1 - 3 + 1, 1347420415, 1347420415);
			this.drawGradientRect(x1 - 3, y1 + height + 2, x1 + maxWidth + 3, y1 + height + 3, 1344798847, 1344798847);

			for (int i = 0; i < lines.size(); ++i) {
				String line = lines.get(i);
				MinecraftFactory.getVars().drawString(line, x1, y1, -1);
				if (i == 0) {
					y1 += 2;
				}

				y1 += 10;
			}

			RenderHelper.enableStandardItemLighting();
			GlStateManager.enableRescaleNormal();
			GLUtil.disableLighting();
		}
	}

	public static void callDrawModalRectWithCustomSizedTexture(int x, int y, float u, float v, int width, int height, float textureWidth, float textureHeight) {
		drawModalRectWithCustomSizedTexture(x, y, u, v, width, height, textureWidth, textureHeight);
	}

	public static void drawRect(double left, double top, double right, double bottom, int color) {
		double i;
		if (left < right) {
			i = left;
			left = right;
			right = i;
		}

		if (top < bottom) {
			i = top;
			top = bottom;
			bottom = i;
		}

		float a = (float) (color >> 24 & 255) / 255.0F;
		float b = (float) (color >> 16 & 255) / 255.0F;
		float g = (float) (color >> 8 & 255) / 255.0F;
		float r = (float) (color & 255) / 255.0F;
		Tessellator worldRenderer = Tessellator.getInstance();
		BufferBuilder tesselator = worldRenderer.getBuffer();
		GLUtil.enableBlend();
		GlStateManager.disableTexture2D();
		GLUtil.tryBlendFuncSeparate(770, 771, 1, 0);
		GLUtil.color(b, g, r, a);
		tesselator.begin(7, DefaultVertexFormats.POSITION);
		tesselator.pos(left, bottom, 0.0D).endVertex();
		tesselator.pos(right, bottom, 0.0D).endVertex();
		tesselator.pos(right, top, 0.0D).endVertex();
		tesselator.pos(left, top, 0.0D).endVertex();
		worldRenderer.draw();
		GlStateManager.enableTexture2D();
		GLUtil.disableBlend();
	}

	public static void drawGradientRect(double left, double top, double right, double bottom, int startColor, int endColor, boolean vertical) {
		float a1 = (float) (startColor >> 24 & 255) / 255.0F;
		float r1 = (float) (startColor >> 16 & 255) / 255.0F;
		float g1 = (float) (startColor >> 8 & 255) / 255.0F;
		float b1 = (float) (startColor & 255) / 255.0F;
		float a2 = (float) (endColor >> 24 & 255) / 255.0F;
		float r2 = (float) (endColor >> 16 & 255) / 255.0F;
		float g2 = (float) (endColor >> 8 & 255) / 255.0F;
		float b2 = (float) (endColor & 255) / 255.0F;

		GlStateManager.disableTexture2D();
		GLUtil.enableBlend();
		GLUtil.disableAlpha();
		GLUtil.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.shadeModel(7425);
		Tessellator tesselator = Tessellator.getInstance();
		BufferBuilder vertex = tesselator.getBuffer();
		vertex.begin(7, DefaultVertexFormats.POSITION_COLOR);
		if (!vertical) {
			vertex.pos(right, top, 1.0).color(r1, g1, b1, a1).endVertex();
			vertex.pos(left, top, 1.0).color(r1, g1, b1, a1).endVertex();
			vertex.pos(left, bottom, 1.0).color(r2, g2, b2, a2).endVertex();
			vertex.pos(right, bottom, 1.0).color(r2, g2, b2, a2).endVertex();
		} else {
			vertex.pos(right, top, 1.0).color(r2, g2, b2, a2).endVertex();
			vertex.pos(left, top, 1.0).color(r1, g1, b1, a1).endVertex();
			vertex.pos(left, bottom, 1.0).color(r1, g1, b1, a1).endVertex();
			vertex.pos(right, bottom, 1.0).color(r2, g2, b2, a2).endVertex();
		}
		tesselator.draw();
		GlStateManager.shadeModel(7424);
		GLUtil.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.enableTexture2D();
	}

	public Gui getChild() {
		return child;
	}
}
