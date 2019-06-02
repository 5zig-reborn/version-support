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

import com.google.common.base.Objects;
import com.mojang.authlib.GameProfile;
import eu.the5zig.mod.MinecraftFactory;
import eu.the5zig.mod.asm.Transformer;
import eu.the5zig.mod.chat.network.packets.PacketBuffer;
import eu.the5zig.mod.gui.elements.IButton;
import eu.the5zig.mod.gui.elements.ITextfield;
import eu.the5zig.mod.manager.SearchEntry;
import eu.the5zig.util.Callback;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.ResourcePackListEntry;
import net.minecraft.client.resources.ResourcePackListEntryFound;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.storage.SaveFormatComparator;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by 5zig.
 * All rights reserved © 2015
 */
public class ClassProxy {

	private static final Field byteBuf;
	private static Field buttonList;
	private static final Field serverList;

	private static boolean tryFix = false;

	private static final UUID SPRINT_MODIFIER_UUID = UUID.fromString("662A6B8D-DA3E-4C1C-8813-96EA6097278D");

	private ClassProxy() {
	}

	static {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		try {
			Class<?> c = classLoader.loadClass(PacketBuffer.class.getName());
			byteBuf = c.getDeclaredField(Transformer.REFLECTION.ByteBuf().get());
			byteBuf.setAccessible(true);
			buttonList = classLoader.loadClass(GuiScreen.class.getName()).getDeclaredField(Transformer.REFLECTION.ButtonList().get());
			buttonList.setAccessible(true);
			Class<ServerSelectionList> aClass = ServerSelectionList.class;
			serverList = aClass.getDeclaredField(Transformer.REFLECTION.ServerList().get());
			serverList.setAccessible(true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static IButton getThe5zigModButton(Object instance) {
		GuiScreen guiScreen = (GuiScreen) instance;
		return MinecraftFactory.getVars().createButton(42, guiScreen.width / 2 - 155, guiScreen.height / 6 + 24 - 6, 150, 20, MinecraftFactory.getClassProxyCallback().translate("menu.the5zigMod"));
	}

	public static void guiOptionsActionPerformed(Object instance, Object b) {
		GuiScreen guiScreen = (GuiScreen) instance;
		GuiButton button = (GuiButton) b;
		if (button.id == 42) {
			MinecraftFactory.getClassProxyCallback().displayGuiSettings(new WrappedGui(guiScreen));
		}
	}

	public static IButton getMCPVPButton(GuiScreen guiScreen) {
		return MinecraftFactory.getVars().createButton(9, guiScreen.width / 2 - 23, guiScreen.height - 28, 46, 20, MinecraftFactory.getClassProxyCallback().translate("menu.mcpvp"));
	}

	public static void guiMultiplayerActionPerformed(GuiScreen guiScreen, GuiButton button) {
		if (button.id == 9) {
//			The5zigMod.getVars().displayScreen(new ServerListMCPvP(guiScreen));
		}
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
				.addCrashSection("The 5zig Mod Version", MinecraftFactory.getClassProxyCallback().getVersion());
		((CrashReport) crashReport).getCategory()
				.addCrashSection("The 5zig Mod Plugins", MinecraftFactory.getClassProxyCallback().getModList());
		((CrashReport) crashReport).getCategory()
				.addCrashSection("Forge", Transformer.FORGE);
		((CrashReport) crashReport).getCategory()
				.addCrashSection("GUI", MinecraftFactory.getVars().getCurrentScreen());
	}

	public static void publishCrashReport(Throwable cause, File crashFile) {
		if (MinecraftFactory.getClassProxyCallback() != null) {
			MinecraftFactory.getClassProxyCallback().launchCrashHopper(cause, crashFile);
		}
	}

	public static void handleGuiDisconnectedDraw(Object instance) {
		GuiDisconnected gui = (GuiDisconnected) instance;
		MinecraftFactory.getClassProxyCallback().checkAutoreconnectCountdown(gui.width, gui.height);
	}

	public static void setServerData(Object serverData) {
		String host = ((ServerData) serverData).serverIP;
		if (!"5zig.eu".equalsIgnoreCase(host) && !"5zig.net".equalsIgnoreCase(host)) {
			MinecraftFactory.getClassProxyCallback().setAutoreconnectServerData(serverData);
		} else {
			MinecraftFactory.getClassProxyCallback().setAutoreconnectServerData(null);
		}
	}

	public static void fixOptionButtons(Object instance) {
		if (!tryFix)
			return;
		GuiScreen guiScreen = (GuiScreen) instance;
		tryFix = false;
		List<GuiButton> list;
		try {
			list = ((List<GuiButton>) buttonList.get(guiScreen));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		for (GuiButton button : list) {
			if (button.id != 42 && button.xPosition == guiScreen.width / 2 - 155 && button.yPosition == guiScreen.height / 6 + 24 - 6) {
				button.xPosition = guiScreen.width / 2 + 5;
				button.width = 150;
			}
		}
	}

	public static void handleGuiResourcePackInit(Object instance, Object listObject, Object listObject2) {
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
				return resourcePackListEntryFound1.func_148318_i().getResourcePackName().toLowerCase(Locale.ROOT)
						.compareTo(resourcePackListEntryFound2.func_148318_i().getResourcePackName().toLowerCase(Locale.ROOT));
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
						return resourcePackListEntryFound.func_148318_i().getResourcePackName()
								.toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT))
								|| resourcePackListEntryFound.func_148318_i().getResourcePackName().toLowerCase(
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
						return resourcePackListEntryFound.func_148318_i().getTexturePackDescription().toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT)) ||
								resourcePackListEntryFound.func_148318_i().getTexturePackDescription().toLowerCase(
								Locale.ROOT).contains(text.toLowerCase(Locale.ROOT));
					}

					@Override
					protected int getAddIndex() {
						return 1;
					}
				});
	}

	public static void handleGuiMultiplayerInit(Object instance, Object serverSelectionListInstance) {
		final GuiMultiplayer guiMultiplayer = (GuiMultiplayer) instance;
		final ServerSelectionList serverSelectionList = (ServerSelectionList) serverSelectionListInstance;
		final List<ServerListEntryNormal> list;
		try {
			list = (List<ServerListEntryNormal>) ClassProxy.serverList.get(serverSelectionList);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		ITextfield textfield = MinecraftFactory.getVars().createTextfield(MinecraftFactory.getClassProxyCallback().translate("gui.search"), 9991, (guiMultiplayer.width - 305) / 2 + 6,
				guiMultiplayer.height - 84, 170, 16);
		final SearchEntry<ServerListEntryNormal> searchEntry = new SearchEntry<ServerListEntryNormal>(textfield, list) {

			@Override
			public void draw() {
				super.draw();
				if (serverSelectionList.getListWidth() >= list.size()) {
					serverSelectionList.setSelectedSlotIndex(-1);
				}
			}

			@Override
			public boolean filter(String text, ServerListEntryNormal serverListEntry) {
				return serverListEntry.getServerData().serverName.toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT))
						|| serverListEntry.getServerData().serverMOTD.toLowerCase(Locale.ROOT).contains(
						text.toLowerCase(Locale.ROOT));
			}
		};
		Callback<ServerListEntryNormal> enterCallback = new Callback<ServerListEntryNormal>() {
			@Override
			public void call(ServerListEntryNormal callback) {
				guiMultiplayer.selectServer(0);
				guiMultiplayer.connectToSelected();
				searchEntry.reset();
			}
		};
		searchEntry.setEnterCallback(enterCallback);
		MinecraftFactory.getClassProxyCallback().addSearch(searchEntry);
	}

	public static void handleGuiSelectWorldInit(Object instance, List list) {
		final GuiSelectWorld guiSelectWorld = (GuiSelectWorld) instance;
		ITextfield textfield = MinecraftFactory.getVars().createTextfield(MinecraftFactory.getClassProxyCallback().translate("gui.search"), 9991, (guiSelectWorld.width - 220) / 2 + 6,
				guiSelectWorld.height - 84, 170, 16);
		final SearchEntry<SaveFormatComparator> searchEntry = new SearchEntry<SaveFormatComparator>(textfield, list) {
			@Override
			public boolean filter(String text, SaveFormatComparator saveFormatComparator) {
				return saveFormatComparator.getDisplayName().toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT))
						|| saveFormatComparator.getFileName().contains(text.toLowerCase(Locale.ROOT));
			}
		};
		searchEntry.setEnterCallback(new Callback<SaveFormatComparator>() {
			@Override
			public void call(SaveFormatComparator callback) {
				guiSelectWorld.func_146615_e(0);
			}
		});
		searchEntry.setComparator(new Comparator<SaveFormatComparator>() {
			@Override
			public int compare(SaveFormatComparator saveFormatComparator1, SaveFormatComparator saveFormatComparator2) {
				return Long.compare(saveFormatComparator1.getLastTimePlayed(), saveFormatComparator2.getLastTimePlayed());
			}
		});
		MinecraftFactory.getClassProxyCallback().addSearch(searchEntry);
	}

	public static float getCustomFOVModifier(Object object) {
		EntityPlayer playerInstance = (EntityPlayer) object;

		float modifier = 1.0F;
		if (playerInstance.capabilities.isFlying) {
			// is Flying
			modifier *= 1.1F;
		}
		IAttributeInstance movementSpeedAttribute = playerInstance.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
		if (movementSpeedAttribute.getModifier(SPRINT_MODIFIER_UUID) != null) {
			modifier = (float) ((double) modifier * (movementSpeedAttribute.getAttributeValue() * 1.30000001192092896D
					/ playerInstance.capabilities.getWalkSpeed() + 1.0) / 2.0D);
		}
		if (playerInstance.capabilities.getWalkSpeed() == 0.0F || Float.isNaN(modifier) || Float.isInfinite(modifier)) {
			modifier = 1.0F;
		}

		if (playerInstance.isUsingItem() && playerInstance.getItemInUse() != null && playerInstance.getItemInUse().getItem() == Items.bow) {
			// is using bow
			int itemInUseDuration = playerInstance.getItemInUseDuration();
			float itemInUseDurationSeconds = (float) itemInUseDuration / 20.0F;
			if (itemInUseDurationSeconds > 1.0F) {
				itemInUseDurationSeconds = 1.0F;
			} else {
				itemInUseDurationSeconds *= itemInUseDurationSeconds;
			}

			modifier *= 1.0F - itemInUseDurationSeconds * 0.15F;
		}

		return modifier;
	}

	public static String[] getSignText(Object signTile) {
		IChatComponent[] lines = ((TileEntitySign) signTile).signText;
		String[] result = new String[lines.length];
		for (int i = 0; i < lines.length; i++) {
			result[i] = lines[i].getFormattedText();
		}
		return result;
	}

	public static void setSignText(Object signTile, String[] text) {
		for (int i = 0; i < text.length; i++) {
			IChatComponent[] components = ((TileEntitySign) signTile).signText;
			if (!Objects.equal(components[i].getFormattedText(), text[i])) {
				components[i] = new ChatComponentText(text[i]);
			}
		}
	}

}
