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

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.GlStateManager;
import eu.the5zig.mod.MinecraftFactory;
import eu.the5zig.mod.The5zigMod;
import eu.the5zig.mod.asm.Transformer;
import eu.the5zig.mod.gui.Gui;
import eu.the5zig.mod.gui.IOverlay;
import eu.the5zig.mod.gui.IWrappedGui;
import eu.the5zig.mod.gui.ingame.IGui2ndChat;
import eu.the5zig.mod.gui.ingame.ItemStack;
import eu.the5zig.mod.gui.ingame.PotionEffectImpl;
import eu.the5zig.mod.gui.ingame.ScoreboardImpl;
import eu.the5zig.util.Callback;
import eu.the5zig.util.Utils;
import eu.the5zig.util.minecraft.ChatColor;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.GameSettings;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CChatMessagePacket;
import net.minecraft.network.play.client.CCustomPayloadPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectUtils;
import net.minecraft.potion.Effects;
import net.minecraft.realms.RealmsBridge;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Session;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunk;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWKeyCallbackI;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Proxy;
import java.util.*;

public class Variables implements IVariables, GLFWKeyCallbackI {

	private static Field forgeChatField;
	private static Method rightClickMouse;
	private static Field sessionField;

	static {
		try {
			The5zigMod.logger.info("Field: ", Transformer.REFLECTION
					.GuiChatInput()
					.get());
			forgeChatField = ChatScreen.class.getDeclaredField(Transformer.REFLECTION.GuiChatInput().get());
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

	private MainWindow scaledResolution;
	private IGui2ndChat gui2ndChat = new Gui2ndChat();

	private final GLFWKeyCallback previousCallback;

	private final ResourceManager resourceManager;
	private Kernel32.SYSTEM_POWER_STATUS batteryStatus;

	private static final List<PotionEffectImpl> DUMMY_POTIONS = Arrays.asList(new PotionEffectImpl("potion.jump", 20, "0:01", 1, 10, true, true, 0x22ff4c),
			new PotionEffectImpl("potion.moveSpeed", 20 * 50, "0:50", 1, 0, true, true, 0x7cafc6));

	public Variables() {
		Keyboard.init(new Keyboard.KeyboardHandler() {
			@Override
			public boolean isKeyDown(int key) {
				return GLFW.glfwGetKey(scaledResolution.getHandle(), key) == GLFW.GLFW_PRESS;
			}

			@Override
			public void enableRepeatEvents(boolean repeat) {
				getMinecraft().keyboardListener.enableRepeatEvents(repeat);
			}
		});
		Mouse.init(new Mouse.MouseHandler() {
			@Override
			public boolean isButtonDown(int button) {
				return GLFW.glfwGetMouseButton(scaledResolution.getHandle(), button)
						== GLFW.GLFW_PRESS;
			}

			@Override
			public int getX() {
				return (int)getMinecraft().mouseHelper.getMouseX();
			}

			@Override
			public int getY() {
				return (int) (getMinecraft().mainWindow.getHeight() - getMinecraft().mouseHelper.getMouseY());
			}
		});
		Display.init(new Display.DisplayHandler() {
			@Override
			public boolean isActive() {
				return GLFW.glfwGetWindowAttrib(scaledResolution.getHandle(), GLFW.GLFW_FOCUSED)
						!= 0;
			}
		});
		previousCallback = GLFW.glfwSetKeyCallback(getMinecraft().mainWindow.getHandle(), this);
		updateScaledResolution();
		try {
			this.resourceManager = new ResourceManager(getGameProfile());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		try {
			Class<?> enumGameSettings = GameSettings.class;
			Method setMaxValue = enumGameSettings.getDeclaredMethod(Transformer.REFLECTION.SetMaxValue().get(), float.class);
			Field gamma = enumGameSettings.getDeclaredField(Transformer.REFLECTION.GAMMA().get());
			setMaxValue.invoke(gamma.get(null), 10.0f);
			Field fov = enumGameSettings.getDeclaredField(Transformer.REFLECTION.FOV().get());
			setMaxValue.invoke(fov.get(null), 130f);
		} catch (Exception e) {
			//MinecraftFactory.getClassProxyCallback().getLogger().warn("Could not patch game settings", e);
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
		if(withShadow) {
			getFontrenderer().drawStringWithShadow(string, x, y, color);
		}
		else {
			getFontrenderer().drawString(string, x, y, color);
		}

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
		return new WrappedTextfield((TextFieldWidget) handle);
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
		return new WrappedGui((Screen) lastScreen);
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
		return getMinecraftScreen() instanceof ChatScreen;
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
			displayScreen(new ChatScreen(text));
		}
		TextFieldWidget chatField = getChatField();
		chatField.setText(chatField.getText() + text);
	}

	private TextFieldWidget getChatField() {
		ChatScreen chatGUI = (ChatScreen) getMinecraftScreen();
		TextFieldWidget chatField;
		try {
			chatField = (TextFieldWidget) forgeChatField.get(chatGUI);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return chatField;
	}

	@Override
	public Object getChatComponentWithPrefix(String prefix, Object originalChatComponent) {
		return new StringTextComponent(prefix).appendSibling((ITextComponent) originalChatComponent);
	}

	@Override
	public boolean isSignGUIOpened() {
		return getMinecraftScreen() instanceof EditSignScreen;
	}

	@Override
	public void registerKeybindings(List<IKeybinding> keybindings) {
		KeyBinding[] currentKeybindings = getGameSettings().keyBindings;
		KeyBinding[] customKeybindings = new KeyBinding[keybindings.size()];
		for (int i = 0; i < keybindings.size(); i++) {
			customKeybindings[i] = (KeyBinding) keybindings.get(i);
		}
		//getGameSettings().keyBindings = Utils.concat(currentKeybindings, customKeybindings);

		getGameSettings().loadOptions();
	}

	@Override
	public void playSound(String sound, float pitch) {
		playSound("minecraft", sound, pitch);
	}

	@Override
	public void playSound(String domain, String sound, float pitch) {
		getMinecraft().getSoundHandler().play(SimpleSound.master(new SoundEvent(new ResourceLocation(domain, sound)), pitch));
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
		for (net.minecraft.client.network.play.NetworkPlayerInfo wrapped : getPlayer().connection.getPlayerInfoMap()) {
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
		getGameSettings().fov = fov;
	}

	@Override
	public float getFOV() {
		return (float)getGameSettings().fov;
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
		getMinecraft().displayGuiScreen((Screen) gui);
	}

	@Override
	public void joinServer(String host, int port) {
		if (getWorld() != null) {
			getWorld().setInitialSpawnLocation();
		}

		MinecraftFactory.getClassProxyCallback().resetServer();
		displayScreen(new ConnectingScreen((Screen) getMinecraftScreen(), getMinecraft(), new ServerData(host, host + ":" + port, false)));
	}

	@Override
	public void joinServer(Object parentScreen, Object serverData) {
		if (serverData == null) {
			return;
		}
		if (getWorld() != null) {
			getWorld().setInitialSpawnLocation();
		}

		MinecraftFactory.getClassProxyCallback().resetServer();
		displayScreen(new ConnectingScreen((ConnectingScreen) parentScreen, getMinecraft(), (ServerData) serverData));
	}

	@Override
	public void disconnectFromWorld() {
		boolean isOnIntegratedServer = getMinecraft().isIntegratedServerRunning();
		boolean isConnectedToRealms = getMinecraft().isConnectedToRealms();
		if (getWorld() != null) {
			getWorld().setInitialSpawnLocation();
		}
		getMinecraft().loadWorld(null);
		if (isOnIntegratedServer) {
			displayScreen(new MainMenuScreen());
		} else if (isConnectedToRealms) {
			RealmsBridge realmsBridge = new RealmsBridge();
			realmsBridge.switchToRealms(new MainMenuScreen());
		} else {
			displayScreen(new MultiplayerScreen(new MainMenuScreen()));
		}
	}

	@Override
	public IServerPinger getServerPinger() {
		return new ServerPinger();
	}

	@Override
	public long getSystemTime() {
		return System.currentTimeMillis();
	}

	public Minecraft getMinecraft() {
		return Minecraft.getInstance();
	}

	public FontRenderer getFontrenderer() {
		return getMinecraft().fontRenderer;
	}

	public GameSettings getGameSettings() {
		return getMinecraft().gameSettings;
	}

	public ClientPlayerEntity getPlayer() {
		return getMinecraft().player;
	}

	public World getWorld() {
		return getMinecraft().world;
	}

	public IngameGui getGuiIngame() {
		return getMinecraft().ingameGUI;
	}

	@Override
	public boolean isSpectatingSelf() {
		return getSpectatingEntity() instanceof PlayerEntity;
	}

	@Override
	public PlayerGameMode getGameMode() {
		return PlayerGameMode.values()[getMinecraft().playerController.getCurrentGameType().getID()];
	}

	public Entity getSpectatingEntity() {
		return getMinecraft().getRenderViewEntity();
	}

	public Container getOpenContainer() {
		return getPlayer() == null ? null : getPlayer().openContainer;
	}

	@Override
	public String getOpenContainerTitle() {
		if (!(getOpenContainer() instanceof ChestContainer))
			return null;
		// TODO return ((ChestContainer) getOpenContainer()).getLowerChestInventory().getDisplayName().getFormattedText();
		return "";
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
		return getMinecraftScreen() instanceof DownloadTerrainScreen;
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
		return getMinecraft().objectMouseOver != null && getMinecraft().objectMouseOver.getType().ordinal() == 1;
	}

	@Override
	public int getTargetBlockX() {
		return (int) getMinecraft().objectMouseOver.getHitVec().getX();
	}

	@Override
	public int getTargetBlockY() {
		return (int) getMinecraft().objectMouseOver.getHitVec().getY();
	}

	@Override
	public int getTargetBlockZ() {
		return (int) getMinecraft().objectMouseOver.getHitVec().getZ();
	}

	@Override
	public ResourceLocation getTargetBlockName() {
		// TODO
		return new ResourceLocation("minecraft", "stone");
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
		IChunk localObject = getMinecraft().world.getChunk(localdt);
		return localObject.getBiome(localdt).getDisplayName().getFormattedText();
	}

	@Override
	public int getLightLevel() {
		BlockPos localdt = new BlockPos(getPlayerPosX(), getPlayerPosY(), getPlayerPosZ());
		if (!getWorld().isBlockLoaded(localdt)) {
			return 0;
		}
		IChunk localObject = getMinecraft().world.getChunk(localdt);
		return localObject.getLightValue(localdt);
	}

	@Override
	public String getEntityCount() {
		if(getMinecraft().world == null) return "No world";
		return Integer.toString(Iterables.size(getMinecraft().world.getAllEntities()));
	}

	@Override
	public boolean isRidingEntity() {
		return getSpectatingEntity().getRidingEntity() != null;
	}

	@Override
	public int getFoodLevel() {
		return isSpectatingSelf() ? ((PlayerEntity) getSpectatingEntity()).getFoodStats().getFoodLevel() : getPlayer().getFoodStats().getFoodLevel();
	}

	@Override
	public float getSaturation() {
		return isSpectatingSelf() ? ((PlayerEntity) getSpectatingEntity()).getFoodStats().getSaturationLevel() : 0;
	}

	@Override
	public float getHealth(Object entity) {
		if (!(entity instanceof LivingEntity))
			return -1;
		return ((LivingEntity) entity).getHealth();
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
		return getSpectatingEntity().isInWater();
	}

	@Override
	public float getResistanceFactor() {
		float referenceDamage = 100;
		int i1 = 25 - getPlayer().getTotalArmorValue();
		float d1 = referenceDamage * (float) i1;
		referenceDamage = d1 / 25.0F;

		if (getPlayer().isPotionActive(Effects.RESISTANCE)) {
			int resistanceAmplifier = (getPlayer().getActivePotionEffect(Effects.RESISTANCE).getAmplifier() + 1) * 5;
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
	public MouseOverObject calculateMouseOverDistance(double maxDistance) {
		return null;
	}

	@Override
	public PotionEffectImpl getPotionForVignette() {
		for (EffectInstance potionEffect : getActivePlayerPotionEffects()) {
			Effect potion = getPotionByEffect(potionEffect);
			if (potion != null) {
				return wrapPotionEffect(potionEffect);
			}
		}
		return null;
	}

	@Override
	public List<eu.the5zig.mod.gui.ingame.PotionEffect> getActivePotionEffects() {
		List<eu.the5zig.mod.gui.ingame.PotionEffect> result = new ArrayList<>(getActivePlayerPotionEffects().size());
		for (EffectInstance potionEffect : getActivePlayerPotionEffects()) {
			result.add(wrapPotionEffect(potionEffect));
		}
		Collections.sort(result);
		return result;
	}

	@Override
	public List<? extends eu.the5zig.mod.gui.ingame.PotionEffect> getDummyPotionEffects() {
		return DUMMY_POTIONS;
	}

	private PotionEffectImpl wrapPotionEffect(EffectInstance potionEffect) {
		Effect potion = getPotionByEffect(potionEffect);
		return new PotionEffectImpl(potion == null ? "" : potionEffect.getEffectName(), potionEffect.getDuration(),
				EffectUtils.getPotionDurationString(potionEffect, 1), potionEffect.getAmplifier() + 1, potion == null ? -1 : potion.getEffectType().ordinal(),
				potion == null || potionEffect.getPotion().isBeneficial(), true, potion == null ? 0 : potionEffect.getPotion().getLiquidColor());
	}

	@Override
	public int getPotionEffectIndicatorHeight() {
		return 0;
	}

	@Override
	public boolean isHungerPotionActive() {
		return getPlayer().isPotionActive(Effects.HUNGER);
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

	public Collection<EffectInstance> getActivePlayerPotionEffects() {
		return getPlayer().getActivePotionEffects();
	}

	public Effect getPotionByEffect(EffectInstance potionEffect) {
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
	//	return new WrappedItemStack(new net.minecraft.item.ItemStack TODO Find impl
		return null;
	}

	@Override
	public ItemStack getItemByName(String resourceName, int amount) {
		//return new WrappedItemStack(new net.minecraft.item.ItemStack(Item.func_111206_d(resourceName), amount));
		return null;
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
		getNetworkManager().sendPacket(new CHeldItemChangePacket(slot));
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
		GlStateManager.blendFuncSeparate(770, 771, 1, 0);
		ItemRenderer itemRenderer = getMinecraft().getItemRenderer();
		itemRenderer.renderItemAndEffectIntoGUI(itemStack, x, y);
		itemRenderer.renderItemOverlays(getFontrenderer(), itemStack, x, y);
		GlStateManager.disableBlend();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableAlphaTest();
	}

	@Override
	public void updateScaledResolution() {
		this.scaledResolution = getMinecraft().mainWindow;
	}

	@Override
	public int getWidth() {
		return scaledResolution.getWidth();
	}

	@Override
	public int getHeight() {
		return scaledResolution.getHeight();
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
		return (int) scaledResolution.getGuiScaleFactor();
	}

	@Override
	public void drawIngameTexturedModalRect(int x, int y, int u, int v, int width, int height) {
		if (getGuiIngame() != null)
			getGuiIngame().blit(x, y, u, v, width, height);
	}

	@Override
	public boolean showDebugScreen() {
		return getGameSettings().showDebugInfo;
	}

	@Override
	public boolean isPlayerSpectating() {
		return !isSpectatingSelf() || getPlayerController().isSpectatorMode();
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
			result[i] = getKeyDisplayStringShort(hotbarBindings[i].getKey().getKeyCode());
		}
		return result;
	}

	@Override
	public String getKeyDisplayStringShort(int key) {
		return key < 0 ? "M" + (key + 101) : (key < 256 ? org.lwjgl.input.Keyboard.getKeyName(key) : String.format("%c", (char) (key - 256)).toUpperCase());
	}

	private PlayerController getPlayerController() {
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
		getPlayer().connection.sendPacket(new CChatMessagePacket(message));
	}

	@Override
	public boolean hasNetworkManager() {
		return getNetworkManager() != null;
	}

	@Override
	public void sendCustomPayload(String channel, ByteBuf payload) {
		if (getNetworkManager() != null) {
			getNetworkManager().sendPacket(new CCustomPayloadPacket(ResourceLocation.create(channel, ':'),
					new PacketBuffer(payload)));
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
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ImageIO.write(image, "png", os);
			InputStream in = new ByteArrayInputStream(os.toByteArray());
			return getTextureManager().getDynamicTextureLocation(name, new DynamicTexture(NativeImage.read(in)));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
		DynamicTexture dynamicImage = new DynamicTexture(width, height, false);
		getTextureManager().loadTexture((net.minecraft.util.ResourceLocation) resourceLocation, dynamicImage);
		return dynamicImage;
	}

	@Override
	public Object getTexture(Object resourceLocation) {
		return getTextureManager().getTexture((net.minecraft.util.ResourceLocation) resourceLocation);
	}

	@Override
	public void fillDynamicImage(Object dynamicImage, BufferedImage image) {
		image.getRGB(0, 0, image.getWidth(), image.getHeight(), ((DynamicTexture) dynamicImage).getTextureData().makePixelArray(), 0, image.getWidth());
		((DynamicTexture) dynamicImage).updateDynamicTexture();
	}

	@Override
	public void renderPotionIcon(int index) {
		getGuiIngame().blit(0, 0, index % 8 * 18, 198 + index / 8 * 18, 18, 18);
	}

	public TextureManager getTextureManager() {
		return getMinecraft().getTextureManager();
	}

	@Override
	public void renderTextureOverlay(int x1, int x2, int y1, int y2) {
		Tessellator var4 = Tessellator.getInstance();
		BufferBuilder var5 = var4.getBuffer();
		bindTexture(AbstractGui.BACKGROUND_LOCATION);
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
		getMinecraft().setGameFocused(true);
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
		String displayName = objective.getDisplayName().getUnformattedComponentText();
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
		return null;
	}

	@Override
	public boolean isMainThread() {
		return getMinecraft().isOnExecutionThread();
	}

	@Override
	public File getMinecraftDataDirectory() {
		return getMinecraft().gameDir;
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
	public void invoke(long window, int key, int scancode, int action, int mods) {
		previousCallback.invoke(window, key, scancode, action, mods);
		if(action == GLFW.GLFW_PRESS)
			MinecraftFactory.getClassProxyCallback().fireKeyPressEvent(key);
	}

	@Override
	public void setSession(String name, String uuid, String token, String userType) {
		Session session = new Session(name, uuid, token, userType);
		try {
			sessionField.set(Minecraft.getInstance(), session);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
