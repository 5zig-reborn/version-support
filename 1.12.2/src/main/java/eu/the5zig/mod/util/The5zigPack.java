/*
 * Copyright (c) 2019-2020 5zig Reborn
 * Copyright (c) 2015-2019 5zig
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

package eu.the5zig.mod.util;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.resources.DefaultResourcePack;
import net.minecraft.client.resources.ResourceIndex;

import java.util.HashMap;
import java.util.Set;

public class The5zigPack extends DefaultResourcePack {

    public static final Set<String> resourceDomains = ImmutableSet.of("the5zigmod");

    public The5zigPack() {
        super(new ResourceIndex() {

        });
    }

    /**
     * getResourceDomains
     *
     * @return a Set with all resource domains of this resource pack.
     */
    @Override
    public Set<String> getResourceDomains() {
        return resourceDomains;
    }

    /**
     * getName
     *
     * @return the Name of this Pack.
     */
    @Override
    public String getPackName() {
        return "The 5zig Mod";
    }
}