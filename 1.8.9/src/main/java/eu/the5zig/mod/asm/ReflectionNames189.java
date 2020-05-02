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

package eu.the5zig.mod.asm;

public class ReflectionNames189 implements ReflectionNames {
    @Override
    public ReflSimpleTuple GuiChatInput() {
        return new ReflSimpleTripleDev("field_146415_a", "a", "inputField");
    }

    @Override
    public ReflSimpleTuple ChatComponentClick() {
        return new ReflSimpleTripleDev("func_175276_a", "a", "handleComponentClick");
    }

    @Override
    public ReflSimpleTuple ChatComponentHover() {
        return new ReflSimpleTripleDev("func_175272_a", "a", "handleComponentHover");
    }

    @Override
    public ReflSimpleTuple RightClickMouse() {
        return new ReflSimpleTripleDev("func_147121_ag", "ax", "rightClickMouse");
    }

    @Override
    public ReflSimpleTuple ButtonList() {
        return new ReflSimpleTripleDev("field_146292_n", "n", "buttonList");
    }

    @Override
    public ReflSimpleTuple ByteBuf() {
        return new ReflSimpleTripleDev("field_150794_a", "a", "buf");
    }

    @Override
    public ReflSimpleTuple ServerList() {
        return new ReflSimpleTripleDev("field_148198_l", "v", "serverListInternet");
    }

    @Override
    public ReflSimpleTuple SetMaxValue() {
        return new ReflSimpleTripleDev("func_148263_a", "a", "setValueMax");
    }

    @Override
    public ReflSimpleTuple GAMMA() {
        return new ReflSimpleTripleDev("GAMMA", "d", "GAMMA");
    }

    @Override
    public ReflSimpleTuple FOV() {
        return new ReflSimpleTripleDev("FOV", "c", "FOV");
    }
}
