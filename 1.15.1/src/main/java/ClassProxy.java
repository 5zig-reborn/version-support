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

import com.mojang.authlib.GameProfile;
import eu.the5zig.mod.MinecraftFactory;
import eu.the5zig.mod.asm.Transformer;
import eu.the5zig.mod.chat.network.packets.PacketBuffer;
import eu.the5zig.mod.gui.elements.IButton;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.crash.CrashReport;

import java.io.File;
import java.lang.reflect.Field;

/**
 * Created by 5zig.
 * All rights reserved © 2015
 */
public class ClassProxy {

	private static final Field byteBuf;
	private static Field buttonList;
	private static final Field serverList;

	@SuppressWarnings("unused")
	private static boolean tryFix = false;

	private ClassProxy() {
	}

	static {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		try {
			Class<?> c = classLoader.loadClass(PacketBuffer.class.getName());
			byteBuf = c.getDeclaredField(Transformer.REFLECTION.ByteBuf().get());
			byteBuf.setAccessible(true);
			buttonList = classLoader.loadClass(Screen.class.getName()).getDeclaredField(Transformer.REFLECTION.ButtonList().get());
			buttonList.setAccessible(true);
			Class<ServerSelectionList> aClass = ServerSelectionList.class;
			serverList = aClass.getDeclaredField(Transformer.REFLECTION.ServerList().get());
			serverList.setAccessible(true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static IButton getThe5zigModButton(Object instance) {
		Screen guiScreen = (Screen) instance;
		return MinecraftFactory.getVars().createButton(42, guiScreen.width / 2 - 155, guiScreen.height / 6 + 24 - 6, 150, 20, MinecraftFactory.getClassProxyCallback().translate("menu.the5zigMod"));
	}


	public static void setupPlayerTextures(GameProfile gameProfile) {
		((Variables) MinecraftFactory.getVars()).getResourceManager().loadPlayerTextures(gameProfile);
	}

	public static boolean onRenderItemPerson(Object instance, Object itemStackObject, Object entityPlayerObject, Object cameraTransformTypeObject, boolean leftHand) {
		return ((Variables) MinecraftFactory.getVars()).getResourceManager().renderInPersonMode(instance, itemStackObject, entityPlayerObject, cameraTransformTypeObject);
	}

	public static boolean onRenderItemInventory(Object instance, Object itemStackObject, int x, int y) {
		return ((Variables) MinecraftFactory.getVars()).getResourceManager().renderInInventory(instance, itemStackObject, x, y);
	}

	public static ByteBuf packetBufferToByteBuf(Object packetBuffer) {
		try {
			return (ByteBuf) byteBuf.get(packetBuffer);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static void appendCategoryToCrashReport(Object crashReport) {
		((CrashReport) crashReport).getCategory()
				.addDetail("The 5zig Mod Version", MinecraftFactory.getClassProxyCallback().getVersion());
		((CrashReport) crashReport).getCategory()
				.addDetail("The 5zig Mod Plugins", MinecraftFactory.getClassProxyCallback().getModList());
		((CrashReport) crashReport).getCategory()
				.addDetail("Forge", Transformer.FORGE);
		((CrashReport) crashReport).getCategory()
				.addDetail("GUI", MinecraftFactory.getVars().getCurrentScreen());
	}

	public static void publishCrashReport(Throwable cause, File crashFile) {
		if (MinecraftFactory.getClassProxyCallback() != null) {
			MinecraftFactory.getClassProxyCallback().launchCrashHopper(cause, crashFile);
		}
	}

	public static void setServerData(Object serverData) {
		String host = ((ServerData) serverData).serverIP;
		if (!"5zig.eu".equalsIgnoreCase(host) && !"5zig.net".equalsIgnoreCase(host)) {
			MinecraftFactory.getClassProxyCallback().setAutoreconnectServerData(serverData);
		} else {
			MinecraftFactory.getClassProxyCallback().setAutoreconnectServerData(null);
		}
	}

	public static void handleGuiResourcePackInit(Object instance, Object listObject, Object listObject2) {
		/*
		List list = (List) listObject;
		List list2 = (List) listObject2;
		GuiScreenResourcePacks gui = (GuiScreenResourcePacks) instance;
		Comparator comparator = new Comparator() {
			@Override
			public int compare(Object o1, Object o2) {
				if (!(o1 instanceof ResourcePackListEntryFound) || !(o2 instanceof ResourcePackListEntryFound))
					return 0;
				ResourcePackListEntryFound resourcePackListEntryFound1 = (ResourcePackListEntryFound) o1;
				ResourcePackListEntryFound resourcePackListEntryFound2 = (ResourcePackListEntryFound) o2;
				return resourcePackListEntryFound1.func_195017_i().func_195789_b().getUnformattedComponentText().toLowerCase(Locale.ROOT)
						.compareTo(resourcePackListEntryFound2.func_195017_i().func_195789_b().getUnformattedComponentText().toLowerCase(Locale.ROOT));
			}
		};
		MinecraftFactory.getClassProxyCallback().addSearch(
				new SearchEntry<ResourcePackListEntry>(MinecraftFactory.getVars().createTextfield(MinecraftFactory.getClassProxyCallback().translate("gui.search"), 9991, gui.width / 2 - 200, gui.height - 70, 170, 16),
						list, comparator) {
					@Override
					public boolean filter(String text, ResourcePackListEntry o) {
						if (!(o instanceof ResourcePackListEntryFound))
							return true;
						ResourcePackListEntryFound resourcePackListEntryFound = (ResourcePackListEntryFound) o;
						return resourcePackListEntryFound.func_148318_i().func_110515_d()
								.toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT))
								|| resourcePackListEntryFound.func_148318_i().func_110515_d().toLowerCase(
								Locale.ROOT).contains(text.toLowerCase(Locale.ROOT));
					}
				},
				new SearchEntry<ResourcePackListEntry>(MinecraftFactory.getVars().createTextfield(MinecraftFactory.getClassProxyCallback().translate("gui.search"), 9992, gui.width / 2 + 8, gui.height - 70, 170, 16),
						list2, new Comparator<ResourcePackListEntry>() {
					@Override
					public int compare(ResourcePackListEntry o1, ResourcePackListEntry o2) {
						return 0;
					}
				}) {
					@Override
					public boolean filter(String text, ResourcePackListEntry o) {
						if (!(o instanceof ResourcePackListEntryFound))
							return true;
						ResourcePackListEntryFound resourcePackListEntryFound = (ResourcePackListEntryFound) o;
						return resourcePackListEntryFound.func_148318_i().func_110519_e().toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT)) ||
								resourcePackListEntryFound.func_148318_i().func_110519_e().toLowerCase(
								Locale.ROOT).contains(text.toLowerCase(Locale.ROOT));
					}

					@Override
					protected int getAddIndex() {
						return 1;
					}
				});
				*/
	}

/*
	public static void handleGuiSelectWorldInit(Object instance, List list) {
		final GuiWorldSelection guiSelectWorld = (GuiWorldSelection) instance;
		ITextfield textfield = MinecraftFactory.getVars().createTextfield(MinecraftFactory.getClassProxyCallback().translate("gui.search"), 9991, (guiSelectWorld.width - 220) / 2 + 6,
				guiSelectWorld.height - 84, 170, 16);
		final SearchEntry<GuiListWorldSelectionEntry> searchEntry = new SearchEntry<GuiListWorldSelectionEntry>(textfield, list) {
			@Override
			public boolean filter(String text, GuiListWorldSelectionEntry saveFormatComparator) {
				return saveFormatComparator.getDisplayName().toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT))
						|| saveFormatComparator.getFileName().contains(text.toLowerCase(Locale.ROOT));
			}
		};
		searchEntry.setEnterCallback(new Callback<GuiListWorldSelectionEntry>() {
			@Override
			public void call(GuiListWorldSelectionEntry callback) {
				guiSelectWorld.func_146615_e(0);
			}
		});
		searchEntry.setComparator(new Comparator<GuiListWorldSelectionEntry>() {
			@Override
			public int compare(GuiListWorldSelectionEntry saveFormatComparator1, GuiListWorldSelectionEntry saveFormatComparator2) {
				return Long.compare(saveFormatComparator1.getLastTimePlayed(), saveFormatComparator2.getLastTimePlayed());
			}
		});
		MinecraftFactory.getClassProxyCallback().addSearch(searchEntry);
	}
*/


}
