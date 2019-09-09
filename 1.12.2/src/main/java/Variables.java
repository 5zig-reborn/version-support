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

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import eu.the5zig.mod.MinecraftFactory;
import eu.the5zig.mod.The5zigMod;
import eu.the5zig.mod.asm.Transformer;
import eu.the5zig.mod.gui.Gui;
import eu.the5zig.mod.gui.IOverlay;
import eu.the5zig.mod.gui.IWrappedGui;
import eu.the5zig.mod.gui.elements.*;
import eu.the5zig.mod.gui.ingame.IGui2ndChat;
import eu.the5zig.mod.gui.ingame.ItemStack;
import eu.the5zig.mod.gui.ingame.PotionEffectImpl;
import eu.the5zig.mod.gui.ingame.ScoreboardImpl;
import eu.the5zig.mod.util.*;
import eu.the5zig.util.Callback;
import eu.the5zig.util.Utils;
import eu.the5zig.util.minecraft.ChatColor;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.Item;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.realms.RealmsBridge;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.Session;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Proxy;
import java.util.*;

public class Variables implements IVariables {

	private static Field forgeChatField;
	private static Method rightClickMouse;

	private static Field sessionField;

	static {
		try {
			The5zigMod.logger.info("Field: ", Transformer.REFLECTION
					.GuiChatInput()
					.get());
			forgeChatField = GuiChat.class.getDeclaredField(Transformer.REFLECTION.GuiChatInput().get());
			forgeChatField.setAccessible(true);
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
		try {
			rightClickMouse = Minecraft.class.getDeclaredMethod(Transformer.REFLECTION.RightClickMouse().get());
			rightClickMouse.setAccessible(true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		try {
			for(Field f : Minecraft.class.getDeclaredFields()) {
				if(f.getType().isAssignableFrom(Session.class)) {
					sessionField = f;
					break;
				}
			}
			sessionField.setAccessible(true);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	private ScaledResolution scaledResolution;
	private IGui2ndChat gui2ndChat = new Gui2ndChat();

	private final ResourceManager resourceManager;
	private Kernel32.SYSTEM_POWER_STATUS batteryStatus;

	private static final List<PotionEffectImpl> DUMMY_POTIONS = Arrays.asList(new PotionEffectImpl("potion.jump", 20, "0:01", 1, 10, true, true, 0x22ff4c),
			new PotionEffectImpl("potion.moveSpeed", 20 * 50, "0:50", 1, 0, true, true, 0x7cafc6));

	public Variables() {
		Keyboard.initLegacy(new Keyboard.KeyboardHandler() {
			@Override
			public boolean isKeyDown(int key) {
				return org.lwjgl.input.Keyboard.isKeyDown(key);
			}

			@Override
			public void enableRepeatEvents(boolean repeat) {
				org.lwjgl.input.Keyboard.enableRepeatEvents(repeat);
			}
		});
		Mouse.init(new Mouse.MouseHandler() {
			@Override
			public boolean isButtonDown(int button) {
				return org.lwjgl.input.Mouse.isButtonDown(button);
			}

			@Override
			public int getX() {
				return org.lwjgl.input.Mouse.getX();
			}

			@Override
			public int getY() {
				return org.lwjgl.input.Mouse.getY();
			}
		});
		Display.init(new Display.DisplayHandler() {
			@Override
			public boolean isActive() {
				return org.lwjgl.opengl.Display.isActive();
			}
		});
		updateScaledResolution();
		try {
			this.resourceManager = new ResourceManager(getGameProfile());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		try {
			Class<?> enumGameSettings = GameSettings.Options.class;
			Method setMaxValue = enumGameSettings.getDeclaredMethod(Transformer.REFLECTION.SetMaxValue().get(), float.class);
			Field gamma = enumGameSettings.getDeclaredField(Transformer.REFLECTION.GAMMA().get());
			setMaxValue.invoke(gamma.get(null), 10.0f);
			Field fov = enumGameSettings.getDeclaredField(Transformer.REFLECTION.FOV().get());
			setMaxValue.invoke(fov.get(null), 130f);
		} catch (Exception e) {
			MinecraftFactory.getClassProxyCallback().getLogger().warn("Could not patch game settings", e);
		}
		if (Utils.getPlatform() == Utils.Platform.WINDOWS) {
			try {
				batteryStatus = new Kernel32.SYSTEM_POWER_STATUS();
			} catch (Throwable ignored) {
			}
		}
	}

	@Override
	public void drawString(String string, int x, int y, Object... format) {
		drawString(string, x, y, 0xffffff, format);
	}

	@Override
	public void drawString(String string, int x, int y) {
		drawString(string, x, y, 0xffffff);
	}

	@Override
	public void drawCenteredString(String string, int x, int y) {
		drawCenteredString(string, x, y, 0xffffff);
	}

	@Override
	public void drawCenteredString(String string, int x, int y, int color) {
		drawString(string, (x - getStringWidth(string) / 2), y, color);
	}

	@Override
	public void drawString(String string, int x, int y, int color, Object... format) {
		drawString(String.format(string, format), x, y, color);
	}

	@Override
	public void drawString(String string, int x, int y, int color) {
		drawString(string, x, y, color, true);
	}

	@Override
	public void drawString(String string, int x, int y, int color, boolean withShadow) {
		getFontrenderer().drawString(string, x, y, color, withShadow);
	}

	@Override
	public List<String> splitStringToWidth(String string, int width) {
		Validate.isTrue(width > 0);
		if (string == null)
			return Collections.emptyList();
		if (string.isEmpty())
			return Collections.singletonList("");
		return getFontrenderer().listFormattedStringToWidth(string, width);
	}

	@Override
	public int getStringWidth(String string) {
		return getFontrenderer().getStringWidth(string);
	}

	@Override
	public String shortenToWidth(String string, int width) {
		if (StringUtils.isEmpty(string))
			return string;
		Validate.isTrue(width > 0);

		boolean changed = false;
		if (getStringWidth(string) > width) {
			while (getStringWidth(string + "...") > width && !string.isEmpty()) {
				string = string.substring(0, string.length() - 1);
				changed = true;
			}
		}
		if (changed)
			string += "...";
		return string;
	}

	@Override
	public IButton createButton(int id, int x, int y, String label) {
		return new Button(id, x, y, label);
	}

	@Override
	public IButton createButton(int id, int x, int y, String label, boolean enabled) {
		return new Button(id, x, y, label, enabled);
	}

	@Override
	public IButton createButton(int id, int x, int y, int width, int height, String label) {
		return new Button(id, x, y, width, height, label);
	}

	@Override
	public IButton createButton(int id, int x, int y, int width, int height, String label, boolean enabled) {
		return new Button(id, x, y, width, height, label, enabled);
	}

	@Override
	public IButton createStringButton(int id, int x, int y, String label) {
		return new StringButton(id, x, y, label);
	}

	@Override
	public IButton createStringButton(int id, int x, int y, int width, int height, String label) {
		return new StringButton(id, x, y, width, height, label);
	}

	@Override
	public IButton createAudioButton(int id, int x, int y, AudioCallback callback) {
		return new AudioButton(id, x, y, callback);
	}

	@Override
	public IButton createIconButton(IResourceLocation resourceLocation, int u, int v, int id, int x, int y) {
		return new IconButton(resourceLocation, u, v, id, x, y);
	}

	@Override
	public ITextfield createTextfield(int id, int x, int y, int width, int height) {
		return new Textfield(id, x, y, width, height);
	}

	@Override
	public ITextfield createTextfield(int id, int x, int y, int width, int height, int maxStringLength) {
		return new Textfield(id, x, y, width, height, maxStringLength);
	}

	@Override
	public IPlaceholderTextfield createTextfield(String placeholder, int id, int x, int y, int width, int height) {
		return new PlaceholderTextfield(placeholder, id, x, y, width, height);
	}

	@Override
	public IPlaceholderTextfield createTextfield(String placeholder, int id, int x, int y, int width, int height, int maxStringLength) {
		return new PlaceholderTextfield(placeholder, id, x, y, width, height, maxStringLength);
	}

	@Override
	public IWrappedTextfield createWrappedTextfield(Object handle) {
		return new WrappedTextfield((GuiTextField) handle);
	}

	@Override
	public <E extends Row> IGuiList<E> createGuiList(Clickable<E> clickable, int width, int height, int top, int bottom, int left, int right, List<E> rows) {
		return new GuiList<E>(clickable, width, height, top, bottom, left, right, rows);
	}

	@Override
	public <E extends Row> IGuiList<E> createGuiListChat(int width, int height, int top, int bottom, int left, int right, int scrollx, List<E> rows, GuiListChatCallback callback) {
		return new GuiListChat<E>(width, height, top, bottom, left, right, scrollx, rows, callback);
	}

	@Override
	public IFileSelector createFileSelector(File currentDir, int width, int height, int left, int right, int top, int bottom, Callback<File> callback) {
		return new FileSelector(currentDir, width, height, left, right, top, bottom, callback);
	}

	@Override
	public IButton createSlider(int id, int x, int y, SliderCallback sliderCallback) {
		return new Slider(id, x, y, sliderCallback);
	}

	@Override
	public IButton createSlider(int id, int x, int y, int width, int height, SliderCallback sliderCallback) {
		return new Slider(id, x, y, width, height, sliderCallback);
	}

	@Override
	public IColorSelector createColorSelector(int id, int x, int y, int width, int height, String label, ColorSelectorCallback callback) {
		return new ColorSelector(id, x, y, width, height, label, callback);
	}

	@Override
	public IOverlay newOverlay() {
		return new Overlay();
	}

	@Override
	public void updateOverlayCount(int count) {
		Overlay.updateOverlayCount(count);
	}

	@Override
	public void renderOverlay() {
		Overlay.renderAll();
	}

	@Override
	public IWrappedGui createWrappedGui(Object lastScreen) {
		return new WrappedGui((GuiScreen) lastScreen);
	}

	@Override
	public IKeybinding createKeybinding(String description, int keyCode, String category) {
		return new Keybinding(description, keyCode, category);
	}

	@Override
	public IGui2ndChat get2ndChat() {
		return gui2ndChat;
	}

	@Override
	public boolean isChatOpened() {
		return getMinecraftScreen() instanceof GuiChat;
	}

	@Override
	public String getChatBoxText() {
		if (!isChatOpened()) {
			return null;
		}
		return getChatField().getText();
	}

	@Override
	public void typeInChatGUI(String text) {
		if (!isChatOpened()) {
			displayScreen(new GuiChat());
		}
		GuiTextField chatField = getChatField();
		chatField.setText(chatField.getText() + text);
	}

	private GuiTextField getChatField() {
		GuiChat chatGUI = (GuiChat) getMinecraftScreen();
		GuiTextField chatField;
		try {
			chatField = (GuiTextField) forgeChatField.get(chatGUI);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return chatField;
	}

	@Override
	public Object getChatComponentWithPrefix(String prefix, Object originalChatComponent) {
		return new TextComponentString(prefix).appendSibling((ITextComponent) originalChatComponent);
	}

	@Override
	public boolean isSignGUIOpened() {
		return getMinecraftScreen() instanceof GuiEditSign;
	}

	@Override
	public void registerKeybindings(List<IKeybinding> keybindings) {
		KeyBinding[] currentKeybindings = getGameSettings().keyBindings;
		KeyBinding[] customKeybindings = new KeyBinding[keybindings.size()];
		for (int i = 0; i < keybindings.size(); i++) {
			customKeybindings[i] = (KeyBinding) keybindings.get(i);
		}
		getGameSettings().keyBindings = Utils.concat(currentKeybindings, customKeybindings);

		getGameSettings().loadOptions();
	}

	@Override
	public void playSound(String sound, float pitch) {
		playSound("minecraft", sound, pitch);
	}

	@Override
	public void playSound(String domain, String sound, float pitch) {
		getMinecraft().getSoundHandler().playSound(PositionedSoundRecord
				.getMasterRecord(new SoundEvent(new ResourceLocation(domain, sound)), pitch));
	}

	@Override
	public int getFontHeight() {
		return getFontrenderer().FONT_HEIGHT;
	}

	public ServerData getServerData() {
		return getMinecraft().getCurrentServerData();
	}

	@Override
	public void resetServer() {
		getMinecraft().setServerData(null);
	}

	@Override
	public String getServer() {
		ServerData serverData = getServerData();
		if (serverData == null)
			return null;
		return serverData.serverIP;
	}

	@Override
	public List<NetworkPlayerInfo> getServerPlayers() {
		List<NetworkPlayerInfo> result = Lists.newArrayList();
		for (net.minecraft.client.network.NetworkPlayerInfo wrapped : getPlayer().connection.getPlayerInfoMap()) {
			result.add(new WrappedNetworkPlayerInfo(wrapped));
		}
		return result;
	}

	@Override
	public boolean isPlayerListShown() {
		return (getGameSettings().keyBindPlayerList.isKeyDown()) && ((!getMinecraft().isIntegratedServerRunning()) ||
				(getServerPlayers().size() > 1));
	}

	@Override
	public void setFOV(float fov) {
		getGameSettings().fovSetting = fov;
	}

	@Override
	public float getFOV() {
		return getGameSettings().fovSetting;
	}

	@Override
	public void setSmoothCamera(boolean smoothCamera) {
		getGameSettings().smoothCamera = smoothCamera;
	}

	@Override
	public boolean isSmoothCamera() {
		return getGameSettings().smoothCamera;
	}

	@Override
	public String translate(String location, Object... values) {
		return I18n.format(location, values);
	}

	@Override
	public void displayScreen(Gui gui) {
		if (gui == null)
			displayScreen((Object) null);
		else
			displayScreen(gui.getHandle());
	}

	@Override
	public void displayScreen(Object gui) {
		getMinecraft().displayGuiScreen((GuiScreen) gui);
	}

	@Override
	public void joinServer(String host, int port) {
		if (getWorld() != null) {
			getWorld().init();
		}

		MinecraftFactory.getClassProxyCallback().resetServer();
		displayScreen(new GuiConnecting((GuiScreen) getMinecraftScreen(), getMinecraft(), new ServerData(host, host + ":" + port, false)));
	}

	@Override
	public void joinServer(Object parentScreen, Object serverData) {
		if (serverData == null) {
			return;
		}
		if (getWorld() != null) {
			getWorld().init();
		}

		MinecraftFactory.getClassProxyCallback().resetServer();
		displayScreen(new GuiConnecting((GuiConnecting) parentScreen, getMinecraft(), (ServerData) serverData));
	}

	@Override
	public void disconnectFromWorld() {
		boolean isOnIntegratedServer = getMinecraft().isIntegratedServerRunning();
		boolean isConnectedToRealms = getMinecraft().isConnectedToRealms();
		if (getWorld() != null) {
			getWorld().init();
		}
		getMinecraft().loadWorld(null);
		if (isOnIntegratedServer) {
			displayScreen(new GuiMainMenu());
		} else if (isConnectedToRealms) {
			RealmsBridge realmsBridge = new RealmsBridge();
			realmsBridge.switchToRealms(new GuiMainMenu());
		} else {
			displayScreen(new GuiMultiplayer(new GuiMainMenu()));
		}
	}

	@Override
	public IServerPinger getServerPinger() {
		return new ServerPinger();
	}

	@Override
	public long getSystemTime() {
		return Minecraft.getSystemTime();
	}

	public Minecraft getMinecraft() {
		return Minecraft.getMinecraft();
	}

	public FontRenderer getFontrenderer() {
		return getMinecraft().fontRenderer;
	}

	public GameSettings getGameSettings() {
		return getMinecraft().gameSettings;
	}

	public EntityPlayerSP getPlayer() {
		return getMinecraft().player;
	}

	public World getWorld() {
		return getMinecraft().world;
	}

	public GuiIngame getGuiIngame() {
		return getMinecraft().ingameGUI;
	}

	@Override
	public boolean isSpectatingSelf() {
		return getSpectatingEntity() instanceof EntityPlayer;
	}

	@Override
	public PlayerGameMode getGameMode() {
		return PlayerGameMode.values()[getMinecraft().playerController.getCurrentGameType().getID()];
	}

	public Entity getSpectatingEntity() {
		return getMinecraft().getRenderViewEntity();
	}

	public Container getOpenContainer() {
		if(getPlayer() == null) return null;
		return getPlayer().openContainer;
	}

	@Override
	public String getOpenContainerTitle() {
		if (!(getOpenContainer() instanceof ContainerChest))
			return null;
		return ((ContainerChest) getOpenContainer()).getLowerChestInventory().getName();
	}

	@Override
	public void closeContainer() {
		getPlayer().closeScreen();
	}

	@Override
	public String getSession() {
		return getMinecraft().getSession().getToken();
	}

	@Override
	public String getUsername() {
		return getMinecraft().getSession().getUsername();
	}

	@Override
	public Proxy getProxy() {
		return getMinecraft().getProxy();
	}

	@Override
	public GameProfile getGameProfile() {
		return getMinecraft().getSession().getProfile();
	}

	@Override
	public String getFPS() {
		return Integer.toString(Minecraft.getDebugFPS());
	}

	@Override
	public boolean isPlayerNull() {
		return getPlayer() == null;
	}

	@Override
	public boolean isTerrainLoading() {
		return getMinecraftScreen() instanceof GuiDownloadTerrain;
	}

	@Override
	public double getPlayerPosX() {
		return getSpectatingEntity().posX;
	}

	@Override
	public double getPlayerPosY() {
		return getSpectatingEntity().posY;
	}

	@Override
	public double getPlayerPosZ() {
		return getSpectatingEntity().posZ;
	}

	@Override
	public float getPlayerRotationYaw() {
		return getSpectatingEntity().rotationYaw;
	}

	@Override
	public float getPlayerRotationPitch() {
		return getSpectatingEntity().rotationPitch;
	}

	@Override
	public int getPlayerChunkX() {
		BlockPos blockPosition = new BlockPos(getPlayerPosX(), getPlayerPosY(), getPlayerPosZ());
		return blockPosition.getX() >> 4;
	}

	@Override
	public int getPlayerChunkY() {
		BlockPos blockPosition = new BlockPos(getPlayerPosX(), getPlayerPosY(), getPlayerPosZ());
		return blockPosition.getY() >> 4;
	}

	@Override
	public int getPlayerChunkZ() {
		BlockPos blockPosition = new BlockPos(getPlayerPosX(), getPlayerPosY(), getPlayerPosZ());
		return blockPosition.getZ() >> 4;
	}

	@Override
	public int getPlayerChunkRelX() {
		BlockPos blockPosition = new BlockPos(getPlayerPosX(), getPlayerPosY(), getPlayerPosZ());
		return blockPosition.getX() & 15;
	}

	@Override
	public int getPlayerChunkRelY() {
		BlockPos blockPosition = new BlockPos(getPlayerPosX(), getPlayerPosY(), getPlayerPosZ());
		return blockPosition.getY() & 15;
	}

	@Override
	public int getPlayerChunkRelZ() {
		BlockPos blockPosition = new BlockPos(getPlayerPosX(), getPlayerPosY(), getPlayerPosZ());
		return blockPosition.getZ() & 15;
	}

	@Override
	public boolean hasTargetBlock() {
		return getMinecraft().objectMouseOver != null && getMinecraft().objectMouseOver.typeOfHit.ordinal() == 1
				&& getMinecraft().objectMouseOver.getBlockPos() != null;
	}

	@Override
	public int getTargetBlockX() {
		return getMinecraft().objectMouseOver.getBlockPos().getX();
	}

	@Override
	public int getTargetBlockY() {
		return getMinecraft().objectMouseOver.getBlockPos().getY();
	}

	@Override
	public int getTargetBlockZ() {
		return getMinecraft().objectMouseOver.getBlockPos().getZ();
	}

	@Override
	public ResourceLocation getTargetBlockName() {
		BlockPos blockPosition = getMinecraft().objectMouseOver.getBlockPos();
		return ResourceLocation.fromObfuscated(Block.REGISTRY.getNameForObject(getWorld().getBlockState(blockPosition).getBlock()));
	}

	@Override
	public boolean isFancyGraphicsEnabled() {
		return Minecraft.isFancyGraphicsEnabled();
	}

	@Override
	public String getBiome() {
		BlockPos localdt = new BlockPos(getPlayerPosX(), getPlayerPosY(), getPlayerPosZ());
		if (!getWorld().isBlockLoaded(localdt)) {
			return null;
		}
		Chunk localObject = getMinecraft().world.getChunkFromBlockCoords(localdt);
		return localObject.getBiome(localdt, getMinecraft().world.getBiomeProvider()).getBiomeName();
	}

	@Override
	public int getLightLevel() {
		BlockPos localdt = new BlockPos(getPlayerPosX(), getPlayerPosY(), getPlayerPosZ());
		if (!getWorld().isBlockLoaded(localdt)) {
			return 0;
		}
		Chunk localObject = getMinecraft().world.getChunkFromBlockCoords(localdt);
		return localObject.getLightSubtracted(localdt, 0);
	}

	@Override
	public String getEntityCount() {
		return Integer.toString(getMinecraft().world.loadedEntityList.size());
	}

	@Override
	public boolean isRidingEntity() {
		return getSpectatingEntity().getRidingEntity() != null;
	}

	@Override
	public int getFoodLevel() {
		return isSpectatingSelf() ? ((EntityPlayer) getSpectatingEntity()).getFoodStats().getFoodLevel() : getPlayer().getFoodStats().getFoodLevel();
	}

	@Override
	public float getSaturation() {
		return isSpectatingSelf() ? ((EntityPlayer) getSpectatingEntity()).getFoodStats().getSaturationLevel() : 0;
	}

	@Override
	public float getHealth(Object entity) {
		if (!(entity instanceof EntityLivingBase))
			return -1;
		return ((EntityLivingBase) entity).getHealth();
	}

	@Override
	public float getPlayerHealth() {
		return getPlayer().getHealth();
	}

	@Override
	public float getPlayerMaxHealth() {
		return getPlayer().getMaxHealth();
	}

	@Override
	public int getPlayerArmor() {
		return getPlayer().getTotalArmorValue();
	}

	@Override
	public int getAir() {
		return getPlayer().getAir();
	}

	@Override
	public boolean isPlayerInsideWater() {
		return getSpectatingEntity().isInsideOfMaterial(Material.WATER);
	}

	@Override
	public float getResistanceFactor() {
		float referenceDamage = 100;
		int i1 = 25 - getPlayer().getTotalArmorValue();
		float d1 = referenceDamage * (float) i1;
		referenceDamage = d1 / 25.0F;

		if (getPlayer().isPotionActive(MobEffects.RESISTANCE)) {
			int resistanceAmplifier = (getPlayer().getActivePotionEffect(MobEffects.RESISTANCE).getAmplifier() + 1) * 5;
			int i = 25 - resistanceAmplifier;
			float d = referenceDamage * (float) i;
			referenceDamage = d / 25.0F;
		}

		if (referenceDamage <= 0.0F) {
			return 100;
		} else {
			int enchantmentModifierDamage = EnchantmentHelper.getEnchantmentModifierDamage(getPlayer().inventory.armorInventory,
					DamageSource.GENERIC);
			if (enchantmentModifierDamage > 20) {
				enchantmentModifierDamage = 20;
			}
			if (enchantmentModifierDamage > 0) {
				int k = 25 - enchantmentModifierDamage;
				float f = referenceDamage * (float) k;
				referenceDamage = f / 25.0F;
			}
			return 100 - referenceDamage;
		}
	}

	@Override
	public PotionEffectImpl getPotionForVignette() {
		for (PotionEffect potionEffect : getActivePlayerPotionEffects()) {
			Potion potion = getPotionByEffect(potionEffect);
			if (potion != null && potion.isBadEffect()) {
				return wrapPotionEffect(potionEffect);
			}
		}
		for (PotionEffect potionEffect : getActivePlayerPotionEffects()) {
			Potion potion = getPotionByEffect(potionEffect);
			if (potion != null && !potion.isBadEffect()) {
				return wrapPotionEffect(potionEffect);
			}
		}

		return null;
	}

	@Override
	public List<eu.the5zig.mod.gui.ingame.PotionEffect> getActivePotionEffects() {
		List<eu.the5zig.mod.gui.ingame.PotionEffect> result = new ArrayList<>(getActivePlayerPotionEffects().size());
		for (PotionEffect potionEffect : getActivePlayerPotionEffects()) {
			result.add(wrapPotionEffect(potionEffect));
		}
		Collections.sort(result);
		return result;
	}

	@Override
	public List<? extends eu.the5zig.mod.gui.ingame.PotionEffect> getDummyPotionEffects() {
		return DUMMY_POTIONS;
	}

	private PotionEffectImpl wrapPotionEffect(PotionEffect potionEffect) {
		Potion potion = getPotionByEffect(potionEffect);
		return new PotionEffectImpl(potion == null ? "" : potionEffect.getEffectName(), potionEffect.getDuration(),
				Potion.getPotionDurationString(potionEffect, 1), potionEffect.getAmplifier() + 1, potion == null ? -1 : potion.getStatusIconIndex(),
				potion == null || !potion.isBadEffect(), true, potion == null ? 0 : potion.getLiquidColor());
	}

	@Override
	public int getPotionEffectIndicatorHeight() {
		return 0;
	}

	@Override
	public boolean isHungerPotionActive() {
		return getPlayer().isPotionActive(MobEffects.HUNGER);
	}

	@Override
	public ItemStack getItemInMainHand() {
		return getPlayerItemInHand() == null ? null : new WrappedItemStack(getPlayerItemInHand());
	}

	@Override
	public ItemStack getItemInOffHand() {
		return null;
	}

	@Override
	public ItemStack getItemInArmorSlot(int slot) {
		return getArmorItemBySlot(slot) == null ? null : new WrappedItemStack(getArmorItemBySlot(slot));
	}

	public Collection<PotionEffect> getActivePlayerPotionEffects() {
		return getPlayer().getActivePotionEffects();
	}

	public Potion getPotionByEffect(PotionEffect potionEffect) {
		return potionEffect.getPotion();
	}

	public net.minecraft.item.ItemStack getPlayerItemInHand() {
		return getPlayer().getHeldItemMainhand();
	}

	public net.minecraft.item.ItemStack getArmorItemBySlot(int slot) {
		return getPlayer().inventory.armorItemInSlot(slot);
	}

	@Override
	public ItemStack getItemByName(String resourceName) {
		return new WrappedItemStack(new net.minecraft.item.ItemStack(Item.getByNameOrId(resourceName)));
	}

	@Override
	public ItemStack getItemByName(String resourceName, int amount) {
		return new WrappedItemStack(new net.minecraft.item.ItemStack(Item.getByNameOrId(resourceName), amount));
	}

	@Override
	public int getItemCount(String key) {
		int count = 0;
		for (net.minecraft.item.ItemStack itemStack : getPlayer().inventory.mainInventory) {
			if (itemStack == null)
				continue;
			if (key.equals(WrappedItemStack.getResourceKey(itemStack))) {
				count += itemStack.getCount();
			}
		}
		return count;
	}

	@Override
	public int getSelectedHotbarSlot() {
		return getPlayer().inventory.currentItem;
	}

	@Override
	public void setSelectedHotbarSlot(int slot) {
		getPlayer().inventory.currentItem = slot;
		getNetworkManager().sendPacket(new CPacketHeldItemChange(slot));
	}

	@Override
	public void onRightClickMouse() {
		try {
			rightClickMouse.invoke(getMinecraft());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void renderItem(net.minecraft.item.ItemStack itemStack, int x, int y) {
		RenderHelper.enableGUIStandardItemLighting();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		RenderItem itemRenderer = getMinecraft().getRenderItem();
		itemRenderer.renderItemAndEffectIntoGUI(itemStack, x, y);
		itemRenderer.renderItemOverlays(getFontrenderer(), itemStack, x, y);
		GlStateManager.disableBlend();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableAlpha();
	}

	@Override
	public void updateScaledResolution() {
		this.scaledResolution = new ScaledResolution(getMinecraft());
	}

	@Override
	public int getWidth() {
		return getMinecraft().displayWidth;
	}

	@Override
	public int getHeight() {
		return getMinecraft().displayHeight;
	}

	@Override
	public int getScaledWidth() {
		return scaledResolution.getScaledWidth();
	}

	@Override
	public int getScaledHeight() {
		return scaledResolution.getScaledHeight();
	}

	@Override
	public int getScaleFactor() {
		return scaledResolution.getScaleFactor();
	}

	@Override
	public void drawIngameTexturedModalRect(int x, int y, int u, int v, int width, int height) {
		if (getGuiIngame() != null)
			getGuiIngame().drawTexturedModalRect(x, y, u, v, width, height);
	}

	@Override
	public boolean showDebugScreen() {
		return getGameSettings().showDebugInfo;
	}

	@Override
	public boolean isPlayerSpectating() {
		return !isSpectatingSelf() || getPlayerController().isSpectator();
	}

	@Override
	public boolean shouldDrawHUD() {
		return getPlayerController().shouldDrawHUD();
	}

	@Override
	public String[] getHotbarKeys() {
		String[] result = new String[9];
		KeyBinding[] hotbarBindings = getGameSettings().keyBindsHotbar;
		for (int i = 0; i < Math.min(result.length, hotbarBindings.length); i++) {
			result[i] = getKeyDisplayStringShort(hotbarBindings[i].getKeyCode());
		}
		return result;
	}

	@Override
	public String getKeyDisplayStringShort(int key) {
		return key < 0 ? "M" + (key + 101) : (key < 256 ? org.lwjgl.input.Keyboard.getKeyName(key) : String.format("%c", (char) (key - 256)).toUpperCase());
	}

	private PlayerControllerMP getPlayerController() {
		return getMinecraft().playerController;
	}

	@Override
	public Gui getCurrentScreen() {
		if (!(getMinecraft().currentScreen instanceof GuiHandle))
			return null;
		return ((GuiHandle) getMinecraft().currentScreen).getChild();
	}

	@Override
	public Object getMinecraftScreen() {
		return getMinecraft().currentScreen;
	}

	@Override
	public void messagePlayer(String message) {
		messagePlayer(ChatComponentBuilder.fromLegacyText(message));
	}

	public void messagePlayer(ITextComponent chatComponent) {
		boolean cancel = MinecraftFactory.getClassProxyCallback().shouldCancelChatMessage(chatComponent.getFormattedText().replace(ChatColor.RESET.toString(), ""), chatComponent);
		if (!cancel) {
			getPlayer().sendMessage(chatComponent);
		}
	}

	@Override
	public void sendMessage(String message) {
		getPlayer().connection.sendPacket(new CPacketChatMessage(message));
	}

	@Override
	public boolean hasNetworkManager() {
		return getNetworkManager() != null;
	}

	@Override
	public void sendCustomPayload(String channel, ByteBuf payload) {
		if (getNetworkManager() != null) {
			getNetworkManager().sendPacket(new CPacketCustomPayload(channel, new PacketBuffer(payload)));
		}
	}

	@Override
	public boolean isLocalWorld() {
		return hasNetworkManager() && getNetworkManager().isLocalChannel();
	}

	private NetworkManager getNetworkManager() {
		return getMinecraft().getConnection() != null ? getMinecraft().getConnection().getNetworkManager() : null;
	}

	@Override
	public IResourceLocation createResourceLocation(String resourcePath) {
		return new ResourceLocation(resourcePath);
	}

	@Override
	public IResourceLocation createResourceLocation(String resourceDomain, String resourcePath) {
		return new ResourceLocation(resourceDomain, resourcePath);
	}

	@Override
	public Object loadDynamicImage(String name, BufferedImage image) {
		return getTextureManager().getDynamicTextureLocation(name, new DynamicTexture(image));
	}

	@Override
	public void bindTexture(Object resourceLocation) {
		if (resourceLocation instanceof DynamicTexture) {
			GlStateManager.bindTexture(((DynamicTexture)resourceLocation).getGlTextureId());
		} else {
			getTextureManager().bindTexture((net.minecraft.util.ResourceLocation) resourceLocation);
		}
	}

	@Override
	public void deleteTexture(Object resourceLocation) {
		if (resourceLocation instanceof net.minecraft.util.ResourceLocation) {
			getTextureManager().deleteTexture((net.minecraft.util.ResourceLocation) resourceLocation);
		}
	}

	@Override
	public Object createDynamicImage(Object resourceLocation, int width, int height) {
		DynamicTexture dynamicImage = new DynamicTexture(width, height);
		getTextureManager().loadTexture((net.minecraft.util.ResourceLocation) resourceLocation, dynamicImage);
		return dynamicImage;
	}

	@Override
	public Object getTexture(Object resourceLocation) {
		return getTextureManager().getTexture((net.minecraft.util.ResourceLocation) resourceLocation);
	}

	@Override
	public void fillDynamicImage(Object dynamicImage, BufferedImage image) {
		image.getRGB(0, 0, image.getWidth(), image.getHeight(), ((DynamicTexture) dynamicImage).getTextureData(), 0, image.getWidth());
		((DynamicTexture) dynamicImage).updateDynamicTexture();
	}

	@Override
	public void renderPotionIcon(int index) {
		getGuiIngame().drawTexturedModalRect(0, 0, index % 8 * 18, 198 + index / 8 * 18, 18, 18);
	}

	public TextureManager getTextureManager() {
		return getMinecraft().getTextureManager();
	}

	@Override
	public void renderTextureOverlay(int x1, int x2, int y1, int y2) {
		Tessellator var4 = Tessellator.getInstance();
		BufferBuilder var5 = var4.getBuffer();
		bindTexture(net.minecraft.client.gui.Gui.OPTIONS_BACKGROUND);
		GLUtil.color(1.0F, 1.0F, 1.0F, 1.0F);
		var5.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		var5.pos((double) x1, (double) y2, 0.0D).tex(0.0D, (double) ((float) y2 / 32.0F)).color(64, 64, 64, 255).endVertex();
		var5.pos((double) (x1 + x2), (double) y2, 0.0D).tex((double) ((float) x2 / 32.0F), (double) ((float) y2 / 32.0F)).color(64, 64, 64, 255).endVertex();
		var5.pos((double) (x1 + x2), (double) y1, 0.0D).tex((double) ((float) x2 / 32.0F), (double) ((float) y1 / 32.0F)).color(64, 64, 64, 255).endVertex();
		var5.pos((double) x1, (double) y1, 0.0D).tex(0.0D, (double) ((float) y1 / 32.0F)).color(64, 64, 64, 255).endVertex();
		var4.draw();
	}

	@Override
	public void setIngameFocus() {
		getMinecraft().setIngameFocus();
	}

	@Override
	public MouseOverObject calculateMouseOverDistance(double maxDistance) {
		if (getSpectatingEntity() == null || getWorld() == null)
			return null;
		RayTraceResult objectMouseOver = getSpectatingEntity().rayTrace(maxDistance, 1f);
		double var3 = maxDistance;
		Vec3d entityPosition = getSpectatingEntity().getPositionEyes(1f);

		if (objectMouseOver != null) {
			var3 = objectMouseOver.hitVec.distanceTo(entityPosition);
		}

		Vec3d look = getSpectatingEntity().getLook(1f);
		Vec3d mostFarPoint = entityPosition.addVector(look.x * maxDistance, look.y * maxDistance, look.z * maxDistance);
		Entity pointedEntity = null;
		Vec3d hitVector = null;
		List<Entity> entitiesWithinAABBExcludingEntity = getWorld().getEntitiesInAABBexcluding(getSpectatingEntity(),
				getSpectatingEntity().getCollisionBoundingBox().grow(look.x * maxDistance, look.y * maxDistance, look.z * maxDistance).expand(1.0, 1.0, 1.0), Predicates.and(EntitySelectors.NOT_SPECTATING, new Predicate<Entity>() {
					public boolean apply(Entity entity) {
						return entity.canBeCollidedWith();
					}
				}));
		double distance = var3;

		for (Entity entity : entitiesWithinAABBExcludingEntity) {
			float collisionBorderSize = entity.getCollisionBorderSize();
			AxisAlignedBB axisAlignedBB = entity.getCollisionBoundingBox().grow((double) collisionBorderSize, (double) collisionBorderSize, (double) collisionBorderSize);
			RayTraceResult intercept = axisAlignedBB.calculateIntercept(entityPosition, mostFarPoint);
			if (axisAlignedBB.contains(entityPosition)) {
				if (distance >= 0.0D) {
					pointedEntity = entity;
					hitVector = intercept == null ? entityPosition : intercept.hitVec;
					distance = 0.0D;
				}
			} else if (intercept != null) {
				double distanceToHitVec = entityPosition.distanceTo(intercept.hitVec);
				if (distanceToHitVec < distance || distance == 0.0D) {
					if (entity == getSpectatingEntity().getRidingEntity()) {
						if (distance == 0.0D) {
							pointedEntity = entity;
							hitVector = intercept.hitVec;
						}
					} else {
						pointedEntity = entity;
						hitVector = intercept.hitVec;
						distance = distanceToHitVec;
					}
				}
			}
		}

		if (pointedEntity != null && (distance < var3 || objectMouseOver == null)) {
			objectMouseOver = new RayTraceResult(pointedEntity, hitVector);
		}

		if (objectMouseOver == null)
			return null;
		ObjectType type;
		switch (objectMouseOver.typeOfHit) {
			case MISS:
				return null;
			case BLOCK:
				type = ObjectType.BLOCK;
				break;
			case ENTITY:
				type = ObjectType.ENTITY;
				break;
			default:
				return null;
		}

		return new MouseOverObject(type, type == ObjectType.ENTITY ? pointedEntity : null, distance);
	}

	@Override
	public ScoreboardImpl getScoreboard() {
		if (getWorld() == null) {
			return null;
		}
		Scoreboard scoreboard = getWorld().getScoreboard();
		if (scoreboard == null) {
			return null;
		}
		ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
		if (objective == null) {
			return null;
		}
		String displayName = objective.getDisplayName();
		Collection<Score> scores = scoreboard.getSortedScores(objective);
		HashMap<String, Integer> lines = Maps.newHashMap();
		for (Score score : scores) {
			lines.put(score.getPlayerName(), score.getScorePoints());
		}
		return new ScoreboardImpl(displayName, lines);
	}

	@Override
	public ResourceManager getResourceManager() {
		return resourceManager;
	}

	@Override
	public Kernel32.SYSTEM_POWER_STATUS getBatteryStatus() {
		return batteryStatus;
	}

	@Override
	public InputStream getMinecraftIcon() throws Exception {
		Minecraft mc = Minecraft.getMinecraft();
		return mc.mcDefaultResourcePack.getInputStream(new ResourceLocation("icons/icon_16x16.png"));
	}

	@Override
	public boolean isMainThread() {
		return getMinecraft().isCallingFromMinecraftThread();
	}

	@Override
	public File getMinecraftDataDirectory() {
		return getMinecraft().mcDataDir;
	}

	@Override
	public void dispatchKeypresses() {
		int eventKey = org.lwjgl.input.Keyboard.getEventKey();
		int currentcode = eventKey == 0 ? org.lwjgl.input.Keyboard.getEventCharacter() : eventKey;
		if ((currentcode == 0) || (org.lwjgl.input.Keyboard.isRepeatEvent()))
			return;

		if (org.lwjgl.input.Keyboard.getEventKeyState()) {
			int keyCode = currentcode + (eventKey == 0 ? 256 : 0);
			MinecraftFactory.getClassProxyCallback().fireKeyPressEvent(keyCode);
		}
	}

	@Override
	public void shutdown() {
		getMinecraft().shutdown();
	}

	@Override
	public void setSession(String name, String uuid, String token, String userType) {
		Session session = new Session(name, uuid, token, userType);
		try {
			sessionField.set(Minecraft.getMinecraft(), session);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
