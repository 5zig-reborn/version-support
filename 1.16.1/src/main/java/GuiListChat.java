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

import eu.the5zig.mod.gui.elements.Row;
import eu.the5zig.mod.util.Display;
import eu.the5zig.mod.util.GuiListChatCallback;
import eu.the5zig.mod.util.Mouse;

import java.util.List;

/**
 * Created by 5zig.
 * All rights reserved © 2015
 */
public class GuiListChat<E extends Row> extends GuiList<E> {

	private final GuiListChatCallback callback;

	public GuiListChat(int width, int height, int top, int bottom, final int left, final int right, int scrollx, List<E> rows, GuiListChatCallback callback) {
		super(null, width, height, top, bottom, left, right, rows);
		this.callback = callback;
		setBottomPadding(6);
		setLeftbound(true);
		setDrawSelection(false);
		setScrollX(scrollx);

		setDrawDefaultBackground(callback.drawDefaultBackground());
		setBackgroundTexture(callback.getResourceLocation(), callback.getImageWidth(), callback.getImageHeight());
	}

	/**
	 * Gets the width of the gui list line
	 *
	 * @return The width of the gui list line
	 */
	@Override
	public int getRowWidth() {
		return getRight() - getLeft() - 5;
	}

	/**
	 * Handle Mouse Input
	 */
	@Override
	public void callHandleMouseInput() {
		super.callHandleMouseInput();

		if (!Display.isActive()) // Don't allow multiple URL-Clicks if Display is not focused.
			return;

		int mouseX = this.mouseX;
		int mouseY = this.mouseY;

		if (this.getHoverItem(mouseX, mouseY) != null) {
			if (Mouse.isButtonDown(0)) {
				int y = mouseY - this.getTop() - this.getHeaderPadding() + (int) this.getCurrentScroll() - 4;
				int id = -1;
				int minY = -1;
				// Search for the right ChatLine-ID
				for (int i1 = 0; i1 < heightMap.size(); i1++) {
					Integer integer = heightMap.get(i1);
					Row line = rows.get(i1);
					if (y >= integer - 2 && y <= integer + line.getLineHeight() - 2) {
						id = i1;
						minY = integer;
						break;
					}
				}

				if (id < 0 || id >= rows.size())
					return;

				callback.chatLineClicked(rows.get(id), mouseX, y, minY, getLeft());
			}
		}
	}

}
