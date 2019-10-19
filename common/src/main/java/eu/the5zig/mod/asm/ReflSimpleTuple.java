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

package eu.the5zig.mod.asm;

public class ReflSimpleTuple {
    protected String forge;
    protected String notch;

    public String getForge() {
        return forge;
    }

    public String getNotch() {
        return notch;
    }

    public ReflSimpleTuple(String nameForge, String nameNotch) {
        this.forge = nameForge;
        this.notch = nameNotch;
    }

    public String get() {
        return Transformer.FORGE ? forge : notch;
    }
}
