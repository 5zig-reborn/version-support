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
import eu.the5zig.mod.The5zigMod;
import eu.the5zig.mod.gui.Gui;
import eu.the5zig.mod.util.GLUtil;
import eu.the5zig.mod.util.IResourceLocation;

/**
 * Created by 5zig.
 * All rights reserved © 2015
 */
public class IconTextButton extends Button {

	private IResourceLocation resourceLocation;
	private int u;
	private int v;

	public IconTextButton(IResourceLocation resourceLocation, int u, int v, int id, int x, int y, String displayString) {
		super(id, x, y, 20, 20, "");
		this.resourceLocation = resourceLocation;
		this.u = u;
		this.v = v;

		this.displayString = displayString;
		this.width = 20 + The5zigMod.getVars().getStringWidth(displayString);
	}

	@Override
	public void draw(int mouseX, int mouseY) {
		super.draw(mouseX, mouseY);
		if (isVisible()) {
			MinecraftFactory.getVars().bindTexture(resourceLocation);
			GLUtil.color(1, 1, 1, 1);
			Gui.drawModalRectWithCustomSizedTexture(getX() + 2, getY() + 2, u, v, 16, 16, 128, 128);

			//Gui.drawCenteredString(this.displayString, 16 + this.x + this.width / 2, this.y + (this.height - 8) / 2, color);
		}
	}
}
