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

import eu.the5zig.mod.gui.elements.IButton;
import net.minecraft.client.Minecraft;

/**
 * Created by 5zig.
 * All rights reserved © 2015
 */
public class Button extends net.minecraft.client.gui.widget.button.Button implements IButton {

	private int disabledTicks = 0;
	private int id;

	public Button(int id, int x, int y, String label) {
		super(x, y, 200,20, label, makeCallback());
		this.id = id;
	}

	public Button(int id, int x, int y, String label, boolean enabled) {
		super(x, y, 200, 20, label, makeCallback());
		this.id = id;
		setEnabled(enabled);
	}

	private static IPressable makeCallback() {
		return button -> { };
	}

	public Button(int id, int x, int y, int width, int height, String label) {
		super(x, y, width, height, label, makeCallback());
		this.id = id;
	}

	public Button(int id, int x, int y, int width, int height, String label, boolean enabled) {
		super(x, y, width, height, label, makeCallback());
		this.id = id;
		setEnabled(enabled);
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public String getLabel() {
		return getMessage();
	}

	@Override
	public void setLabel(String label) {
		this.setMessage(label);
	}

	@Override
	public int callGetWidth() {
		return width;
	}

	@Override
	public void callSetWidth(int width) {
		this.width = width;
	}

	@Override
	public int callGetHeight() {
		return height;
	}

	@Override
	public void callSetHeight(int height) {
		this.height = height;
	}

	@Override
	public boolean isEnabled() {
		return active;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.active = enabled;
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
		return isHovered;
	}

	@Override
	public void setHovered(boolean hovered) {
		this.isHovered = hovered;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public void setX(int x) {
		this.x = x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public void setY(int y) {
		this.y = y;
	}

	@Override
	public void draw(int mouseX, int mouseY) {
		render(mouseX, mouseY, 0);
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
		return mouseClicked(mouseX, mouseY, 0);
	}

	@Override
	public void callMouseReleased(int mouseX, int mouseY) {
		mouseReleased((double)mouseX, (double)mouseY, 0);
	}

	@Override
	public void playClickSound() {
		playDownSound(Minecraft.getInstance().getSoundHandler());
	}

	@Override
	public void setTicksDisabled(int ticks) {
		setEnabled(false);
		disabledTicks = ticks;
	}

	@Override
	public void onClick(double mouseX, double mouseY) {

	}

	@Override
	public void guiClosed() {
	}
}
