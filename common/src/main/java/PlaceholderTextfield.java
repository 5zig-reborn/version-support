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
import eu.the5zig.mod.gui.elements.IPlaceholderTextfield;
import org.apache.commons.lang3.StringUtils;

public class PlaceholderTextfield extends Textfield implements IPlaceholderTextfield {

	private String placeholder;

	public PlaceholderTextfield(String placeholder, int id, int x, int y, int width, int height, int maxStringLength) {
		super(id, x, y, width, height, maxStringLength);
		this.placeholder = placeholder;
	}

	public PlaceholderTextfield(String placeholder, int id, int x, int y, int width, int height) {
		super(id, x, y, width, height);
		this.placeholder = placeholder;
	}

	@Override
	public String getPlaceholder() {
		return placeholder;
	}

	@Override
	public void setPlaceholder(String placeholder) {
		this.placeholder = placeholder;
	}

	@Override
	public void callDraw() {
		super.callDraw();
		if (StringUtils.isEmpty(callGetText()) && !callIsFocused()) {
			MinecraftFactory.getVars().drawString(placeholder, isBackgroundDrawing() ? getX() + 4 : getX(), isBackgroundDrawing() ? getY() + (getHeight() - 8) / 2 : getY(), 7368816);
		}
	}
}
