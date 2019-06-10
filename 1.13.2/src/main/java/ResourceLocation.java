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

import eu.the5zig.mod.util.IResourceLocation;

public class ResourceLocation extends net.minecraft.util.ResourceLocation implements IResourceLocation {

	public ResourceLocation(String resourcePath) {
		super(resourcePath);
	}

	public ResourceLocation(String resourceDomain, String resourcePath) {
		super(resourceDomain, resourcePath);
	}

	public static ResourceLocation fromObfuscated(net.minecraft.util.ResourceLocation resourceLocation) {
		return new ResourceLocation(resourceLocation.getNamespace(), resourceLocation.getPath());
	}

	@Override
	public String callGetResourcePath() {
		return path;
	}

	@Override
	public String callGetResourceDomain() {
		return namespace;
	}
}
