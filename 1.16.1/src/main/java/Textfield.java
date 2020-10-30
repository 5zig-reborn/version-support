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

import com.google.common.base.Strings;
import eu.the5zig.mod.gui.elements.ITextfield;
import eu.the5zig.mod.util.MatrixStacks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

/**
 * Created by 5zig.
 * All rights reserved © 2015
 * <p/>
 * Textfield class is a Simple Wrapper for default Minecraft Textfields.
 * Since I use obfuscated code, this textfield class simply refactors all required methods of the original Textfield.
 */
public class Textfield extends TextFieldWidget implements ITextfield {

	/**
	 * Width and height need to be declared in this class, since there is no direct access to it in original Textfield class.
	 */
	private final int width, height, id, maxLength;

	/**
	 * Whether this field is a password textbox.
	 */
	private boolean password;

	/**
	 * Creates a new Textfield, using the original super constructor.
	 *
	 * @param id              The id of the Textfield. Currently, only used, to identify textfield by iterating though all textfields.
	 * @param x               The x-location of the Textfield.
	 * @param y               The y-location of the Textfield.
	 * @param width           The width of the Textfield
	 * @param height          The height of the Textfield.
	 * @param maxStringLength The Max Length the Text of the Textfield can have.
	 */
	public Textfield(int id, int x, int y, int width, int height, int maxStringLength) {
		super(MinecraftClient.getInstance().textRenderer, x, y, width, height, new LiteralText(""));
		this.id = id;
		this.width = width;
		this.height = height;
		this.maxLength = maxStringLength;
		callSetMaxStringLength(maxStringLength);
	}

	/**
	 * Creates a new Textfield, using the original super constructor and 32 as max String length.
	 *
	 * @param id     The id of the Textfield. Currently, only used, to identify textfield by iterating though all textfields.
	 * @param x      The x-location of the Textfield.
	 * @param y      The y-location of the Textfield.
	 * @param width  The width of the Textfield
	 * @param height The height of the Textfield.
	 */
	public Textfield(int id, int x, int y, int width, int height) {
		this(id, x, y, width, height, 32);
	}

	/**
	 * Gets the id of the Textfield.
	 *
	 * @return The id of the Textfield.
	 */
	public int callGetId() {
		return id;
	}

	/**
	 * Focuses this Textfield.
	 *
	 * @param selected if this Textfield should be focused or not.
	 */
	public void setSelected(boolean selected) {
		setFocused(selected);
	}

	/**
	 * Checks, if this Textfield is currently focused/selected.
	 *
	 * @return if this Textfield is currently focused.
	 */
	public boolean callIsFocused() {
		return isFocused();
	}

	/**
	 * Sets focus to this Textfield.
	 *
	 * @param focused if the Textfield should be focused.
	 */
	public void callSetFocused(boolean focused) {
		setSelected(focused);
	}

	/**
	 * Checks, if the Textfield is currently drawing its background texture.
	 *
	 * @return if the background of the Textfield is being drawed.
	 */
	public boolean isBackgroundDrawing() {
		return false;
	}

	/**
	 * Gets the x-location of the Textfield.
	 *
	 * @return the x-location of the Textfield.
	 */
	public int getX() {
		return x;
	}

	/**
	 * Sets the x-location of the Textfield.
	 *
	 * @param x The new x-location of the Textfield.
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * Gets the y-location of the Textfield.
	 *
	 * @return the y-location of the Textfield.
	 */
	public int getY() {
		return y;
	}

	/**
	 * Sets the y-location of the Textfield.
	 *
	 * @param y The new y-location of the Textfield.
	 */
	public void setY(int y) {
		this.y = y;
	}

	/**
	 * Gets the width of the Textfield.
	 *
	 * @return the width of the Textfield.
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Gets the height of the Textfield.
	 *
	 * @return the height of the Textfield.
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Gets the max String Length the Text of the Textfield can have.
	 *
	 * @return the max String Length the Text of the Textfield can have.
	 */
	@Override
	public int callGetMaxStringLength() {
		return maxLength;
	}

	/**
	 * Sets the max String Length the Text of the Textfield can have.
	 *
	 * @param length The max String length of the Text.
	 */
	public void callSetMaxStringLength(int length) {
		setMaxLength(length);
	}

	/**
	 * Gets the current Text of the Textfield.
	 *
	 * @return The current Text of the Textfield.
	 */
	public String callGetText() {
		return getText();
	}

	/**
	 * Sets the Text of the Textfield. Also checks if the text is larger, than callGetMaxStringLength().
	 *
	 * @param string The Text that should be put into the Textfield.
	 */
	public void callSetText(String string) {
		setText(string);
	}

	/**
	 * Simulates a Mouse click in the Textfield. Used in Gui when iterating through all textfields.
	 *
	 * @param x      The x-location of the Mouse.
	 * @param y      The y-location of the Mouse.
	 * @param button The button that has been pressed of the Mouse.
	 */
	public void callMouseClicked(int x, int y, int button) {
		mouseClicked(x, y, button);
	}

	/**
	 * Simulates a Key type in the Textfield. Used in Gui when iterating through all textfields.
	 *
	 * @param character The character that has been typed.
	 * @param key       The LWJGL-Integer of the typed key.
	 */
	public boolean callKeyTyped(char character, int key) {
		return super.charTyped(character, key);
	}

	@Override
	public boolean onKeyPressed(int i, int j, int k) {
		return keyPressed(i, j, k);
	}

	/**
	 * Used for blinking caret. Called from Gui when iterating through all textfields.
	 */
	public void callTick() {
		tick();
	}

	/**
	 * Draws the Textfield. Called from Gui when iterating through all textfields.
	 */
	public void callDraw() {
		render(MatrixStacks.hudMatrixStack, 0, 0, 0);
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		if(password) {
			String previous = getText();
			makePasswordText(Strings.repeat("*", previous.length()));
			super.render(matrixStack, mouseX, mouseY, partialTicks);
			makePasswordText(previous);
		}
		else super.render(matrixStack, mouseX, mouseY, partialTicks);
	}

	@Override
	public void setIsPassword(boolean b) {
		this.password = b;
	}

	private void makePasswordText(String replaceWith) {
		int cursor = getCursor();
		int selEnd = getSelectedText().length() - 1;

		setText(replaceWith);
		setCursor(cursor);
		if(selEnd != -1) setSelectionEnd(selEnd);
	}
}
