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
import eu.the5zig.mod.util.GLUtil;
import eu.the5zig.mod.util.SliderCallback;
import eu.the5zig.util.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;

/**
 * Created by 5zig.
 * All rights reserved © 2015
 */
public class Slider extends Button {

	private final String str;
	public float value;
	public boolean dragging;
	private SliderCallback sliderItem;

	public Slider(int id, int x, int y, SliderCallback sliderItem) {
		this(id, x, y, 150, 20, sliderItem);
	}

	public Slider(int id, int x, int y, int width, int height, SliderCallback sliderItem) {
		super(id, x, y, width, height, "");
		this.sliderItem = sliderItem;
		this.str = sliderItem.translate();
		this.value = normalizeValue(sliderItem.get());
		setLabel(this.str);
	}

	@Override
	protected int getYImage(boolean p_getYImage_1_) {
		return 0;
	}

	@Override
	public void setLabel(String label) {
		this.value = normalizeValue(sliderItem.get());
		super.setLabel(label + ": " + getValue());
	}

	@Override
	protected void renderBg(MatrixStack matrixStack, MinecraftClient minecraftClient, int i, int j) {
		if (isVisible()) {
			if (this.dragging) {
				this.value = (i - (getX() + 4)) / (float) (callGetWidth() - 8);
				this.value = Utils.clamp(value, 0f, 1f);

				float v = denormalizeValue(value);
				sliderItem.set(v);
				this.value = normalizeValue(v);

				setLabel(this.str);
			}

			MinecraftFactory.getVars().bindTexture(ButtonWidget.WIDGETS_LOCATION);
			GLUtil.color(1, 1, 1, 1);
			drawTexture(matrixStack, getX() + (int) (this.value * (callGetWidth() - 8)), getY(), 0, 66, 4, 20);
			drawTexture(matrixStack, getX() + (int) (this.value * (callGetWidth() - 8)) + 4, getY(), 196, 66, 4, 20);
		}
	}

	public float denormalizeValue(float value) {
		return this.snapToStepClamp(sliderItem.getMinValue() + (sliderItem.getMaxValue() - sliderItem.getMinValue()) * Utils.clamp(value, 0.0F, 1.0F));
	}

	public float normalizeValue(float value) {
		return Utils.clamp((snapToStepClamp(value) - sliderItem.getMinValue()) / (sliderItem.getMaxValue() - sliderItem.getMinValue()), 0.0F, 1.0F);
	}

	public float snapToStepClamp(float value) {
		value = snapToStep(value);
		return Utils.clamp(value, sliderItem.getMinValue(), sliderItem.getMaxValue());
	}

	protected float snapToStep(float value) {
		if (sliderItem.getSteps() != -1) {
			value = sliderItem.getSteps() * (float) Math.round(value / sliderItem.getSteps());
		}

		return value;
	}

	@Override
	public boolean mouseClicked(int x, int y) {
		if (super.mouseClicked(x, y)) {
			this.value = (x - (this.x + 4)) / (float) (this.y - 8);
			if (this.value < 0.0F) {
				this.value = 0.0F;
			}
			if (this.value > 1.0F) {
				this.value = 1.0F;
			}

			setLabel(this.str + getValue());
			this.dragging = true;
			return true;
		}
		return false;
	}

	private String getValue() {
		String customValue = sliderItem.getCustomValue(value);
		if (customValue != null)
			return customValue;
		return Math.round(value * (sliderItem.getMaxValue() - sliderItem.getMinValue()) + sliderItem.getMinValue()) + sliderItem.getSuffix();
	}

	@Override
	public void callMouseReleased(int mouseX, int mouseY, int state) {
		if (this.dragging) {
			// play button press sound
			playClickSound();

			sliderItem.action();
		}
		this.dragging = false;
	}
}
