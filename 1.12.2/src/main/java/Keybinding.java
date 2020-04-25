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

import eu.the5zig.mod.asm.Transformer;
import eu.the5zig.mod.util.IKeybinding;
import net.minecraft.client.settings.KeyBinding;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Created by 5zig.
 * All rights reserved © 2015
 */
public class Keybinding extends KeyBinding implements IKeybinding {

    public Keybinding(String description, int keyCode, String category) {
        super(description, keyCode, category);
        tryRegisterCategory(category);
    }

    @Override
    public boolean callIsPressed() {
        return isPressed();
    }

    public int callGetKeyCode() {
        return getKeyCode();
    }

    @Override
    public int compareTo(KeyBinding o) {
        return super.compareTo(o);
    }

    private static void tryRegisterCategory(String category) {
        Map<String, Integer> categories;
        try {
            Field field = Keybinding.class.getSuperclass().getDeclaredField(Transformer.FORGE ? "field_193627_d" : "d");
            field.setAccessible(true);
            categories = (Map<String, Integer>) field.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (!categories.containsKey(category)) {
            categories.put(category, categories.size() + 1);
        }
    }
}
