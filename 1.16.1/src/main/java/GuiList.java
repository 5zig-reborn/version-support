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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import eu.the5zig.mod.MinecraftFactory;
import eu.the5zig.mod.gui.elements.*;
import eu.the5zig.mod.mixin.MixinEntryListWidget;
import eu.the5zig.mod.util.MatrixStacks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GuiList<E extends Row> extends ElementListWidget implements IGuiList<E> {
	private MatrixStack matrixStack;
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
	private boolean renderSelection;

	public class ListElement extends ElementListWidget.Entry {
		private final E element;

		public ListElement(E element) {
			this.element = element;
		}

		@Override
		public List<? extends Element> children() {
			return ImmutableList.of();
		}

		@Override
		public void render(MatrixStack matrixStack, int slotId, int y, int x, int rowLeft, int slotHeight, int mouseX, int mouseY, boolean focused, float partialTicks) {
			if(element instanceof RowExtended) ((RowExtended) element).draw(x, y, slotHeight, mouseX, mouseY);
			else element.draw(x, y);
		}

		@Override
		public boolean mouseClicked(double d, double e, int i) {
			boolean doubleClick = getSelected() == this && MinecraftFactory.getVars().getSystemTime() - GuiList.this.lastClicked < 250L;
			GuiList.this.lastClicked = MinecraftFactory.getVars().getSystemTime();
			if(element instanceof RowExtended) {
				IButton pressed = ((RowExtended) element).mousePressed((int)d, (int)e);
				if(pressed != null) {
					if (selectedButton != null && pressed != selectedButton) pressed.mouseClicked((int)d, (int)e);
					GuiList.this.selectedButton = pressed;
				}
			}
			GuiList.this.onSelect(GuiList.this.children().indexOf(this), element, doubleClick);
			return true;
		}

		public E getElement() {
			return element;
		}
	}

	@Override
	protected int getScrollbarPositionX() {
		return scrollX > 0 ? scrollX : super.getScrollbarPositionX();
	}

	@Override
	public void addEntry(int slot, E entry) {
		children().add(slot, new ListElement(entry));
		calculateHeight();
	}

	@Override
	public void removeEntry(E entry) {
		children().removeIf(e -> ((ListElement) e).getElement() == entry);
	}

	@Override
	public void setEntry(int slot, E entry) {
		children().set(slot, new ListElement(entry));
		calculateHeight();
	}

	@Override
	public void doClearEntries() {
		clearEntries();
	}

	private void calculateHeight() {
		if(rows != null && !rows.isEmpty()) ((MixinEntryListWidget) this).setItemHeight(rows.get(0).getLineHeight());
	}

	public GuiList(Clickable<E> clickable, int width, int height, int top, int bottom, int left, int right, List<E> rows) {
		super(MinecraftClient.getInstance(), Math.min(width, right - left), height, top, bottom, 18);
		this.rows = rows;
		this.clickable = clickable;
		setLeft(left);
		setRight(right);
		setDrawSelection(true);
		replaceEntries(rows == null ? new ArrayList() : rows.stream().map(ListElement::new).collect(Collectors.toList()));
		calculateHeight();
	}

	@Override
	public void callDrawScreen(int mouseX, int mouseY, float partialTicks) {
		render(this.matrixStack == null ? MatrixStacks.hudMatrixStack : matrixStack, mouseX, mouseY, partialTicks);
	}

	@Override
	public void render(MatrixStack matrixStack, int i, int j, float f) {
		this.matrixStack = matrixStack;
		this.mouseX = i;
		this.mouseY = j;
		super.render(matrixStack, i, j, f);
	}

	@Override
	public void callHandleMouseInput() {

	}

	@Override
	public void onSelect(int id, E row, boolean doubleClick) {
		setSelectedId(id);
		if (clickable != null && row != null)
			clickable.onSelect(id, row, doubleClick);
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY) {
		mouseClicked(mouseX, mouseY, 0);
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY, int state) {
		mouseReleased((double) mouseX, mouseY, state);
		if (this.selectedButton != null && state == 0) {
			this.selectedButton.callMouseReleased(mouseX, mouseY, state);
			this.selectedButton = null;
		}
	}

	@Override
	public boolean callMouseDragged(double v, double v1, int i, double v2, double v3) {
		return mouseDragged(v, v1, i, v2, v3);
	}

	@Override
	public void setScrollAmount(double d) {
		super.setScrollAmount(isMouseOver(mouseX, mouseY) ? d : 0);
	}

	@Override
	public boolean callMouseScrolled(double v) {
		return mouseScrolled(0, 0, v);
	}

	@Override
	public void scrollToBottom() {

	}

	@Override
	public float getCurrentScroll() {
		return 0;
	}

	@Override
	public void scrollTo(float to) {

	}

	@Override
	public boolean callIsSelected(int id) {
		return isSelectedItem(id);
	}

	@Override
	protected boolean isSelectedItem(int i) {
		return selected == i;
	}

	@Override
	public int callGetContentHeight() {
		return 0;
	}

	@Override
	public int callGetRowWidth() {
		return 0;
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
		this.setSelected(selected > children().size() ? getEntry(selected) : null);
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
		return top;
	}

	@Override
	public void setTop(int top) {
		this.top = top;
	}

	@Override
	public int getBottom() {
		return bottom;
	}

	@Override
	public void setBottom(int bottom) {
		this.bottom = bottom;
	}

	@Override
	public int getLeft() {
		return left;
	}

	@Override
	public void setLeft(int left) {
		this.left = left;
	}

	@Override
	public int getRight() {
		return right;
	}

	@Override
	public void setRight(int right) {
		this.right = right;
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
		setRenderSelection(drawSelection);
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
		calculateHeightMap();
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

	@Override
	public void calculateHeightMap() {

	}
}
