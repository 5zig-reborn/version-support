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

import eu.the5zig.mod.asm.transformers.*;
import net.minecraft.launchwrapper.IClassTransformer;

import java.util.HashMap;

public class Transformer implements IClassTransformer {

	public static boolean FORGE = false;
	public static HashMap<String, IClassTransformer> obfNames = new HashMap<String, IClassTransformer>();
	public static ReflectionNames REFLECTION;

	static {
		obfNames.put(Names.minecraft.getName(), new PatchMinecraft());
		obfNames.put(Names.guiMainMenu.getName(), new PatchGuiMainMenu());
		obfNames.put(Names.guiIngame.getName(), new PatchGuiIngame());
		obfNames.put(Names.guiIngameForge.getName(), new PatchGuiIngameForge());
		obfNames.put(Names.guiIngameMenu.getName(), new PatchGuiIngameMenu());
		obfNames.put(Names.guiAchievement.getName(), new PatchGuiAchievement());
		obfNames.put(Names.gameSettings.getName(), new PatchGameSettings());
		obfNames.put(Names.netHandlerPlayClient.getName(), new PatchNetHandlerPlayClient());
		obfNames.put(Names.guiScreen.getName(), new PatchGuiScreen());
		obfNames.put(Names.guiOptions.getName(), new PatchGuiOptions());
		obfNames.put(Names.abstractClientPlayer.getName(), new PatchAbstractClientPlayer());
		obfNames.put(Names.entityPlayerSP.getName(), new PatchEntityPlayerSP());
		obfNames.put(Names.guiMultiplayer.getName(), new PatchGuiMultiplayer());
		obfNames.put(Names.networkManager.getName(), new PatchNetworkManager());
		obfNames.put(Names.guiChat.getName(), new PatchGuiChat());
		obfNames.put(Names.guiChatNew.getName(), new PatchGuiChatNew());
		obfNames.put(Names.guiConnecting.getName(), new PatchGuiConnecting());
		obfNames.put(Names.guiDisconnected.getName(), new PatchGuiDisconnected());
		obfNames.put(Names.guiResourcePacks.getName(), new PatchGuiResourcePacks());
		obfNames.put(Names.guiSelectWorld.getName(), new PatchGuiSelectWorld());
		obfNames.put(Names.renderItem.getName(), new PatchRenderItem());
		obfNames.put(Names.guiTextfield.getName(), new PatchGuiTextfield());
		obfNames.put(Names.guiEditSign.getName(), new PatchGuiEditSign());
		obfNames.put(Names.guiGameOver.getName(), new PatchGuiGameOver());
		obfNames.put(Names.rendererLivingEntity.getName(), new PatchRendererLivingEntity());
	}

	@Override
	public byte[] transform(String className, String arg1, byte[] bytes) {
		if (!obfNames.containsKey(className)) {
			return bytes;
		}
		try {
			return obfNames.get(className).transform(className, arg1, bytes);
		} catch (Exception e) {
			System.err.println("[5zig] Failed to Transform Class " + className + "!");
			e.printStackTrace();
		}
		return bytes;
	}

}