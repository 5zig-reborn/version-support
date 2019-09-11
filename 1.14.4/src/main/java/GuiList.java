/*
 * Original: Copyright (c) 2015-2019 5zig [MIT]
 * Current: Copyright (c) 2019 5zig Reborn [GPLv3+]
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

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import eu.the5zig.mod.MinecraftFactory;
import eu.the5zig.mod.gui.Gui;
import eu.the5zig.mod.gui.elements.*;
import eu.the5zig.mod.util.GLUtil;
import eu.the5zig.util.minecraft.ChatColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.SlotGui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;

import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class GuiList<E extends Row> extends SlotGui implements IGuiList<E> {

	protected final List<E> rows;
	private final Clickable<E> clickable;

	private int rowWidth = 95;
	private int bottomPadding;
	private boolean leftbound = false;
	private int scrollX;

	private boolean hasSelected = false;

	protected int mouseX, mouseY;

	private int selected;
	private IButton selectedButton;

	private String header;

	private boolean drawDefaultBackground = false;
	private Object backgroundTexture;
	private int backgroundWidth, backgroundHeight;

	private long lastClicked;

	protected List<Integer> heightMap = Lists.newArrayList();

	public GuiList(Clickable<E> clickable, int width, int height, int top, int bottom, int left, int right, List<E> rows) {
		super(Minecraft.getInstance(), width, height, top, bottom, 18);

		this.rows = rows;
		this.clickable = clickable;
		setLeft(left);
		setRight(right);
	}

	/**
	 * @return the size of all rows
	 */
	@Override
	protected int getItemCount() {
		synchronized (rows) {
			return rows.size();
		}
	}

	@Override
	public int getItemHeight() {
		return callGetContentHeight();
	}

	@Override
	protected boolean selectItem(int id, int p_selectItem_2_, double p_selectItem_3_, double p_selectItem_5_) {
		selected = id;
		boolean var5 = (GuiList.this.selected >= 0) && (GuiList.this.selected < getItemCount());
		if (var5) {
			if (clickable != null) {
				synchronized (rows) {
					boolean doubleClick = id == this.selected && System.currentTimeMillis() - this.lastClicked < 250L;
					onSelect(id, rows.get(id), doubleClick);
					this.lastClicked = System.currentTimeMillis();
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void onSelect(int id, E row, boolean doubleClick) {
		setSelectedId(id);
		if (clickable != null && row != null)
			clickable.onSelect(id, row, doubleClick);
	}

	@Override
	public boolean callIsSelected(int id) {
		return isSelectedItem(id);
	}

	@Override
	protected boolean isSelectedItem(int id) {
		return selected == id;
	}

	/**
	 * drawBackground
	 */
	@Override
	protected void renderBackground() {
	}

	@Override
	public int callGetRowWidth() {
		return getRowWidth();
	}

	@Override
	protected void renderItem(int id, int x, int y, int slotHeight, int mouseX, int mouseY, float pTicks) {
		_drawSlot(id, x, y, slotHeight, mouseX, mouseY, pTicks);
	}

	protected void _drawSlot(int id, int x, int y, int slotHeight, int mouseX, int mouseY, float pTicks) {
		synchronized (rows) {
			if (id < 0 || id >= rows.size())
				return;
			Row selectedRow = rows.get(id);
			selectedRow.draw(x, y);
			if (selectedRow instanceof RowExtended) {
				((RowExtended) selectedRow).draw(x, y, slotHeight, mouseX, mouseY);
			}
		}
	}

	@Override
	public void callDrawScreen(int mouseX, int mouseY, float partialTicks) {
		drawScreen(mouseX, mouseY, partialTicks);
	}

	/**
	 * Draw Screen
	 */
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		calculateHeightMap();
		if (this.visible) {
			this.mouseX = mouseX;
			this.mouseY = mouseY;
			this.renderBackground();
			int var3 = this.getScrollbarPosition();
			int var4 = var3 + 6;
			this.capYPosition();
			GlStateManager.disableLighting();
			GlStateManager.disableFog();
			Tessellator var5 = Tessellator.getInstance();
			BufferBuilder var6 = var5.getBuffer();
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			if (backgroundTexture != null) {
				MinecraftFactory.getVars().bindTexture(backgroundTexture);
				Gui.drawModalRectWithCustomSizedTexture(getLeft(), getTop(), 0, 0, getRight() - getLeft(), getBottom() - getTop(), backgroundWidth, backgroundHeight);
			} else if (drawDefaultBackground || MinecraftFactory.getVars().isPlayerNull()) {
				minecraft.getTextureManager().bindTexture(AbstractGui.BACKGROUND_LOCATION);
				float var7 = 32.0F;
				var6.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
				var6.pos((double) this.x0, (double) this.y1, 0.0D).tex((double) ((float) this.x0 / var7), (double) ((float) (this.y1 + this.getScroll()) / var7))
						.color(32, 32, 32, 255).endVertex();
				var6.pos((double) this.x1, (double) this.y1, 0.0D).tex((double) ((float) this.x1 / var7), (double) ((float) (this.y1 + this.getScroll()) / var7))
						.color(32, 32, 32, 255).endVertex();
				var6.pos((double) this.x1, (double) this.y0, 0.0D).tex((double) ((float) this.x1 / var7), (double) ((float) (this.y0 + this.getScroll()) / var7))
						.color(32, 32, 32, 255).endVertex();
				var6.pos((double) this.x0, (double) this.y0, 0.0D).tex((double) ((float) this.x0 / var7), (double) ((float) (this.y0 + this.getScroll()) / var7))
						.color(32, 32, 32, 255).endVertex();
				var5.draw();
			}
			int var8 = this.x0 + this.width / 2 - this.getRowWidth() / 2 + 2;
			int var9 = this.y0 + 4 - (int) this.getScroll();

			glEnable(GL_SCISSOR_TEST);
			float scaleFactor = MinecraftFactory.getVars().getScaleFactor();
			glScissor((int) Math.ceil(getLeft() * scaleFactor), (int) Math.ceil((getHeight() - getBottom()) * scaleFactor), (int) Math.floor((getRight() - getLeft()) * scaleFactor),
					(int) Math.floor((getBottom() - getTop()) * scaleFactor));
			if (this.renderHeader) {
				this.a(var8, var9, var5);
			}
			this.renderList(var8, var9, mouseX, mouseY, partialTicks);
			glDisable(GL_SCISSOR_TEST);
			byte var10 = 4;
			GlStateManager.enableBlend();
			GlStateManager.blendFuncSeparate(770, 771, 0, 1);
			GlStateManager.disableAlphaTest();
			GlStateManager.shadeModel(7425);
			GlStateManager.disableTexture();
			// Schatten
			int var11 = this.getMaxScroll();
			if (drawDefaultBackground || MinecraftFactory.getVars().isPlayerNull() || var11 > 0) {
				var6.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
				var6.pos((double) this.x0, (double) (this.y0 + var10), 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 0).endVertex();
				var6.pos((double) this.x1, (double) (this.y0 + var10), 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 0).endVertex();
				var6.pos((double) this.x1, (double) this.y0, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
				var6.pos((double) this.x0, (double) this.y0, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
				var5.draw();
				var6.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
				var6.pos((double) this.x0, (double) this.y1, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
				var6.pos((double) this.x1, (double) this.y1, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
				var6.pos((double) this.x1, (double) (this.y1 - var10), 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 0).endVertex();
				var6.pos((double) this.x0, (double) (this.y1 - var10), 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 0).endVertex();
				var5.draw();
			}
			if (var11 > 0) {
				int var12 = (this.y1 - this.y0) * (this.y1 - this.y0) / this.getItemHeight();
				var12 = MathHelper.clamp(var12, 32, this.y1 - this.y0 - 8);
				int var13 = (int) this.getCurrentScroll() * (this.y1 - this.y0 - var12) / var11 + this.y0;
				if (var13 < this.y0) {
					var13 = this.y0;
				}

				var6.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
				var6.pos((double) var3, (double) this.y1, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
				var6.pos((double) var4, (double) this.y1, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
				var6.pos((double) var4, (double) this.y0, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
				var6.pos((double) var3, (double) this.y0, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
				var5.draw();
				var6.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
				var6.pos((double) var3, (double) (var13 + var12), 0.0D).tex(0.0D, 1.0D).color(128, 128, 128, 255).endVertex();
				var6.pos((double) var4, (double) (var13 + var12), 0.0D).tex(1.0D, 1.0D).color(128, 128, 128, 255).endVertex();
				var6.pos((double) var4, (double) var13, 0.0D).tex(1.0D, 0.0D).color(128, 128, 128, 255).endVertex();
				var6.pos((double) var3, (double) var13, 0.0D).tex(0.0D, 0.0D).color(128, 128, 128, 255).endVertex();
				var5.draw();
				var6.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
				var6.pos((double) var3, (double) (var13 + var12 - 1), 0.0D).tex(0.0D, 1.0D).color(192, 192, 192, 255).endVertex();
				var6.pos((double) (var4 - 1), (double) (var13 + var12 - 1), 0.0D).tex(1.0D, 1.0D).color(192, 192, 192, 255).endVertex();
				var6.pos((double) (var4 - 1), (double) var13, 0.0D).tex(1.0D, 0.0D).color(192, 192, 192, 255).endVertex();
				var6.pos((double) var3, (double) var13, 0.0D).tex(0.0D, 0.0D).color(192, 192, 192, 255).endVertex();
				var5.draw();
			}

			this.renderDecorations(mouseX, mouseY);
			GlStateManager.enableTexture();
			GlStateManager.shadeModel(7424);
			GlStateManager.enableAlphaTest();
			GlStateManager.disableBlend();
		}
		getSelectedRow();
	}

	@Override
	protected void renderHoleBackground(int p_renderHoleBackground_1_, int p_renderHoleBackground_2_, int p_renderHoleBackground_3_, int p_renderHoleBackground_4_) {
		super.renderHoleBackground(p_renderHoleBackground_1_, p_renderHoleBackground_2_, p_renderHoleBackground_3_, p_renderHoleBackground_4_);
	}

	@Override
	protected void renderList(int x, int y, int p_renderList_3_, int p_renderList_4_, float pTicks) {
		if (leftbound) {
			x = getLeft() + 2;
		}
		Tessellator localckx = Tessellator.getInstance();
		BufferBuilder localciv = localckx.getBuffer();
		for (int rowIndex = 0; rowIndex < heightMap.size(); ++rowIndex) {
			int newY = y + heightMap.get(rowIndex) + getHeaderPadding();
			int slotHeight = rows.get(rowIndex).getLineHeight() - 4;
			if ((newY > getBottom()) || (newY + slotHeight < getTop())) {
				this.updateItemPosition(rowIndex, x, newY, pTicks);
			} else {
				if (isDrawSelection() && (isSelectedItem(rowIndex))) {
					int x1, x2;
					if (leftbound) {
						x1 = getLeft();
						x2 = getLeft() + getRowWidth();
					} else {
						x1 = getLeft() + (getWidth() / 2 - getRowWidth() / 2);
						x2 = getLeft() + getWidth() / 2 + getRowWidth() / 2;
					}
					GLUtil.color(1.0F, 1.0F, 1.0F, 1.0F);
					GlStateManager.disableTexture();
					localciv.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
					localciv.pos((double) x1, (double) (newY + slotHeight + 2), 0.0D).tex(0.0D, 1.0D)
							.color(128, 128, 128, 255).endVertex();
					localciv.pos((double) x2, (double) (newY + slotHeight + 2), 0.0D).tex(1.0D, 1.0D)
							.color(128, 128, 128, 255).endVertex();
					localciv.pos((double) x2, (double) (newY - 2), 0.0D).tex(1.0D, 0.0D)
							.color(128, 128, 128, 255).endVertex();
					localciv.pos((double) x1, (double) (newY - 2), 0.0D).tex(0.0D, 0.0D)
							.color(128, 128, 128, 255).endVertex();
					localciv.pos((double) (x1 + 1), (double) (newY + slotHeight + 1), 0.0D).tex(0.0D, 1.0D)
							.color(0, 0, 0, 255).endVertex();
					localciv.pos((double) (x2 - 1), (double) (newY + slotHeight + 1), 0.0D).tex(1.0D, 1.0D)
							.color(0, 0, 0, 255).endVertex();
					localciv.pos((double) (x2 - 1), (double) (newY - 1), 0.0D).tex(1.0D, 0.0D)
							.color(0, 0, 0, 255).endVertex();
					localciv.pos((double) (x1 + 1), (double) (newY - 1), 0.0D).tex(0.0D, 0.0D)
							.color(0, 0, 0, 255).endVertex();
					localckx.draw();
					GlStateManager.enableTexture();
				}
				renderItem(rowIndex, x, newY, slotHeight, mouseX, mouseY, pTicks);
			}
		}
	}

	@Override
	public boolean callMouseDragged(double v, double v1, int i, double v2, double v3) {
		if ((this.getFocused() != null && this.hasSelected && i == 0) && this.getFocused().mouseDragged(v, v1, i, v2, v3)) {
			return true;
		} else if (this.isVisible() && i == 0 && this.hasSelected && v > getScrollbarPosition() && v < getScrollbarPosition() + 6) {
			if (v1 < (double)this.y0) {
				this.scroll(0);
			} else if (v1 > (double)this.y1) {
				this.scroll(this.getMaxScroll());
			} else {
				double var10 = this.getMaxScroll();
				if (var10 < 1.0D) {
					var10 = 1.0D;
				}

				int var12 = (int)((float)((this.y1 - this.y0) * (this.y1 - this.y0)) / (float)this.getItemHeight());
				var12 = MathHelper.clamp(var12, 32, this.y1 - this.y0 - 8);
				double var13 = var10 / (double)(this.y1 - this.y0 - var12);
				if (var13 < 1.0D) {
					var13 = 1.0D;
				}

				this.scroll((int) (getScroll() + v3 * var13));
				this.capYPosition();
			}

			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean callMouseScrolled(double var1) {
		if (!this.isVisible() || !(mouseX >= getLeft() && mouseX <= getRight() && mouseY >= getTop() & mouseY <= getBottom())) {
			return false;
		} else {
			this.scroll((int) (getScroll() - var1 * (double)this.itemHeight / 2.0D));
			return true;
		}
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY) {
		hasSelected = mouseX >= getLeft() && mouseX <= getRight() && mouseY >= getTop() & mouseY <= getBottom();

		E item = getHoverItem(mouseX, mouseY);
		if(item == null) return;
		int id = rows.indexOf(item);
		setSelectedId(id);
		this.selectItem(id, 0, (double)mouseX, (double)mouseY);

		if (hasSelected) {
			synchronized (rows) {
				for (int rowIndex = 0; rowIndex < heightMap.size(); ++rowIndex) {
					int newY = (int) (getTop() + heightMap.get(rowIndex) + getHeaderPadding() - getCurrentScroll());
					int slotHeight = rows.get(rowIndex).getLineHeight() - 4;
					if ((newY <= getBottom()) && (newY + slotHeight >= getTop())) {
						Row row = rows.get(rowIndex);
						if (row instanceof RowExtended) {
							IButton pressed = ((RowExtended) row).mousePressed(mouseX, mouseY);
							if (pressed != null) {
								if (selectedButton != null && pressed != selectedButton)
									selectedButton.mouseClicked(mouseX, mouseY);
								selectedButton = pressed;
								return;
							}
						}
					}
				}
			}
		}

	}

	/**
	 * @return x-coordinate of the scroll bar
	 */
	@Override
	protected int getScrollbarPosition() {
		return scrollX > 0 ? scrollX : super.getScrollbarPosition();
	}

	@Override
	public int getItemAtPosition(double x, double y) {
		int var3, var4;
		if (leftbound) {
			var3 = getLeft();
			var4 = getLeft() + getRowWidth();
		} else {
			var3 = getLeft() + getWidth() / 2 - getRowWidth() / 2;
			var4 = getLeft() + getWidth() / 2 + getRowWidth() / 2;
		}
		int var5 = (int) (y - getTop()) - this.headerHeight + (int) getCurrentScroll() - 4;
		int var6 = -1;
		for (int i1 = 0; i1 < heightMap.size(); i1++) {
			Integer integer = heightMap.get(i1);
			Row line = rows.get(i1);
			if (y >= integer && y <= integer + line.getLineHeight()) {
				var6 = i1;
				break;
			}
		}
		return x < this.getScrollbarPosition() && x >= var3 && x <= var4 && var6 >= 0 && var5 >= 0 && var6 < this.getItemCount() ? var6 - 3 : -1;
	}


	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int state) {
		if(this.getFocused() != null) {
			this.getFocused().mouseReleased(mouseX, mouseY, state);
		}
		this.children().forEach(child -> child.mouseReleased(mouseX, mouseY, state));
		return false;
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY, int state) {
		if (this.selectedButton != null && state == 0) {
			this.selectedButton.callMouseReleased(mouseX, mouseY);
			this.selectedButton = null;
		}
	}

	@Override
	public void callHandleMouseInput() {

	}

	@Override
	public void scrollToBottom() {
		scrollTo(getItemHeight());
	}

	@Override
	public float getCurrentScroll() {
		return (float) getScroll();
	}

	@Override
	public void scrollTo(float to) {
		this.scroll((int) to);
	}

	@Override
	public int callGetContentHeight() {
		int height = bottomPadding + (getHeaderPadding() > 0 ? (getHeaderPadding() + 8) : 0);
		List<E> chatLines = Lists.newArrayList(rows);
		for (Row row : chatLines) {
			height += row.getLineHeight();
		}
		return height;
	}

	public void calculateHeightMap() {
		heightMap.clear();

		int curHeight = getHeaderPadding();
		List<E> chatLines = Lists.newArrayList(rows);
		for (Row row : chatLines) {
			heightMap.add(curHeight);
			curHeight += row.getLineHeight();
		}
	}

	public int getRowWidth() {
		return rowWidth;
	}

	@Override
	public void setRowWidth(int rowWidth) {
		this.rowWidth = rowWidth;
	}

	@Override
	public int getSelectedId() {
		synchronized (rows) {
			if (selected < 0 || selected > rows.size())
				selected = setSelectedId(0);
		}
		return selected;
	}

	@Override
	public int setSelectedId(int selected) {
		synchronized (rows) {
			if (selected < 0 || selected > rows.size())
				selected = 0;
		}
		this.selected = selected;
		return selected;
	}

	@Override
	public E getSelectedRow() {
		synchronized (rows) {
			if (rows.isEmpty())
				return null;
			if (selected < 0) {
				selected = 0;
				return rows.get(0);
			}
			while (selected >= rows.size()) {
				selected--;
			}
			return rows.get(selected);
		}
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public void setWidth(int width) {
		this.width = width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void setHeight(int height) {
		this.height = height;
	}

	@Override
	public int getHeight(int id) {
		return heightMap.get(id);
	}

	@Override
	public int getTop() {
		return y0;
	}

	@Override
	public void setTop(int top) {
		this.y0 = top;
	}

	@Override
	public int getBottom() {
		return y1;
	}

	@Override
	public void setBottom(int bottom) {
		this.y1 = bottom;
	}

	@Override
	public int getLeft() {
		return x0;
	}

	@Override
	public void setLeft(int left) {
		this.x0 = left;
	}

	@Override
	public int getRight() {
		return x1;
	}

	@Override
	public void setRight(int right) {
		this.x1 = right;
	}

	@Override
	public int getScrollX() {
		return scrollX;
	}

	@Override
	public void setScrollX(int scrollX) {
		this.scrollX = scrollX;
	}

	@Override
	public boolean isLeftbound() {
		return leftbound;
	}

	@Override
	public void setLeftbound(boolean leftbound) {
		this.leftbound = leftbound;
	}

	@Override
	public boolean isDrawSelection() {
		return renderSelection;
	}

	@Override
	public void setDrawSelection(boolean drawSelection) {
		this.renderSelection = drawSelection;
	}

	@Override
	public int getHeaderPadding() {
		return headerHeight;
	}

	@Override
	public void callSetHeaderPadding(int headerPadding) {
		this.setRenderHeader(headerPadding > 0, headerPadding);
	}

	@Override
	public String getHeader() {
		return header;
	}

	@Override
	public void setHeader(String header) {
		this.header = header;
	}

	protected void a(int x, int y, Tessellator tesselator) {
		if (header != null) {
			MinecraftFactory.getVars().drawCenteredString(ChatColor.UNDERLINE.toString() + ChatColor.BOLD.toString() + header, getLeft() + (getRight() - getLeft()) / 2,
					Math.min(getTop() + 5, y));
		}
	}

	@Override
	public int getBottomPadding() {
		return bottomPadding;
	}

	@Override
	public void setBottomPadding(int bottomPadding) {
		this.bottomPadding = bottomPadding;
	}

	@Override
	public E getHoverItem(int mouseX, int mouseY) {
		int x1, x2;
		if (leftbound) {
			x1 = getLeft();
			x2 = getLeft() + getRowWidth();
		} else {
			x1 = getLeft() + (getWidth() / 2 - getRowWidth() / 2);
			x2 = getLeft() + getWidth() / 2 + getRowWidth() / 2;
		}
		if (mouseX >= x1 && mouseX <= x2) {
			synchronized (rows) {
				for (int i = 0; i < heightMap.size(); i++) {
					Integer y = (int) (heightMap.get(i) + getTop() + getHeaderPadding() - getCurrentScroll());
					E element = rows.get(i);
					if (mouseY >= y && mouseY <= y + element.getLineHeight()) {
						return element;
					}
				}
			}
		}
		return null;
	}

	@Override
	public boolean isDrawDefaultBackground() {
		return drawDefaultBackground;
	}

	@Override
	public void setDrawDefaultBackground(boolean drawDefaultBackground) {
		this.drawDefaultBackground = drawDefaultBackground;
	}

	@Override
	public Object getBackgroundTexture() {
		return backgroundTexture;
	}

	@Override
	public void setBackgroundTexture(Object backgroundTexture, int imageWidth, int imageHeight) {
		this.backgroundTexture = backgroundTexture;

		if (backgroundTexture != null) {
			double w = imageWidth;
			double h = imageHeight;
			int listWidth = getRight() - getLeft();
			int listHeight = getBottom() - getTop();

			while (w > listWidth && h > listHeight) {
				w -= 1;
				h -= h / w;
			}
			while (w < listWidth || h < listHeight) {
				w += 1;
				h += h / w;
			}
			this.backgroundWidth = (int) w;
			this.backgroundHeight = (int) h;
		}
	}

	@Override
	public List<E> getRows() {
		return rows;
	}
}
