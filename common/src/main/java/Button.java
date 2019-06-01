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

import eu.the5zig.mod.gui.elements.IButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

/**
 * Created by 5zig.
 * All rights reserved © 2015
 */
public class Button extends GuiButton implements IButton {

	private int disabledTicks = 0;

	public Button(int id, int x, int y, String label) {
		super(id, x, y, label);
	}

	public Button(int id, int x, int y, String label, boolean enabled) {
		super(id, x, y, label);
		setEnabled(enabled);
	}

	public Button(int id, int x, int y, int width, int height, String label) {
		super(id, x, y, width, height, label);
	}

	public Button(int id, int x, int y, int width, int height, String label, boolean enabled) {
		super(id, x, y, width, height, label);
		setEnabled(enabled);
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public String getLabel() {
		return displayString;
	}

	@Override
	public void setLabel(String label) {
		this.displayString = label;
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
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	@Override
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	@Override
	public boolean isHovered() {
		return hovered;
	}

	@Override
	public void setHovered(boolean hovered) {
		this.hovered = hovered;
	}

	@Override
	public int getX() {
		return xPosition;
	}

	@Override
	public void setX(int x) {
		this.xPosition = x;
	}

	@Override
	public int getY() {
		return yPosition;
	}

	@Override
	public void setY(int y) {
		this.yPosition = y;
	}

	@Override
	public void draw(int mouseX, int mouseY) {
		drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
	}

	@Override
	public void tick() {
		if (disabledTicks > 0) {
			disabledTicks--;
			if (disabledTicks == 0) {
				setEnabled(true);
			}
		}
		if (disabledTicks < 0)
			disabledTicks = 1;
	}

	@Override
	public boolean mouseClicked(int mouseX, int mouseY) {
		return mousePressed(Minecraft.getMinecraft(), mouseX, mouseY);
	}

	@Override
	public void callMouseReleased(int mouseX, int mouseY) {
		mouseReleased(mouseX, mouseY);
	}

	@Override
	public void playClickSound() {
		playPressSound(Minecraft.getMinecraft().getSoundHandler());
	}

	@Override
	public void setTicksDisabled(int ticks) {
		setEnabled(false);
		disabledTicks = ticks;
	}

	@Override
	public void guiClosed() {
	}
}
