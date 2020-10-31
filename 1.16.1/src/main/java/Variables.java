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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import eu.the5zig.mod.MinecraftFactory;
import eu.the5zig.mod.asm.ReflectionNames;
import eu.the5zig.mod.asm.Transformer;
import eu.the5zig.mod.gui.Gui;
import eu.the5zig.mod.gui.IOverlay;
import eu.the5zig.mod.gui.IWrappedGui;
import eu.the5zig.mod.gui.elements.*;
import eu.the5zig.mod.gui.ingame.IGui2ndChat;
import eu.the5zig.mod.gui.ingame.ItemStack;
import eu.the5zig.mod.gui.ingame.PotionEffectImpl;
import eu.the5zig.mod.gui.ingame.ScoreboardImpl;
import eu.the5zig.mod.gui.list.GuiArrayList;
import eu.the5zig.mod.util.*;
import eu.the5zig.mod.util.component.MessageComponent;
import eu.the5zig.util.Callback;
import eu.the5zig.util.Utils;
import eu.the5zig.util.minecraft.ChatColor;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.options.Option;
import net.minecraft.client.realms.gui.screen.RealmsBridgeScreen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.texture.*;
import net.minecraft.client.util.Session;
import net.minecraft.client.util.Window;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.HeldItemChangeS2CPacket;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
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
import java.util.stream.Collectors;

public class Variables implements IVariables, GLFWKeyCallbackI {

	private static Field forgeChatField;
	private static Method rightClickMouse;
	private static Field sessionField;

	static {
		try {
			if(Transformer.REFLECTION == null) {
				Transformer.FABRIC = true;
				Field vf;
				String reflName = null;
				String version;
				try {
					vf = null; /*RealmsSharedConstants.class.getField("VERSION_STRING"); ZIG116*/

					version = (String) vf.get(null);

					switch (version) {
						case "1.8.9":
							reflName = "ReflectionNames189";
							break;
						case "1.12.2":
							reflName = "ReflectionNames1122";
							break;
						case "1.13.2":
							reflName = "ReflectionNames1132";
							break;
					}
				} catch (Exception e) {
					version = "1.14.4";
					reflName = "ReflectionNames1144";
				}
				System.out.println("F Minecraft Version: " + version);

				try {
					Transformer.REFLECTION = (ReflectionNames) Class.forName("eu.the5zig.mod.asm." + reflName).newInstance();
				} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
			forgeChatField = ChatScreen.class.getDeclaredField(Transformer.REFLECTION.GuiChatInput().get());
			forgeChatField.setAccessible(true);
			rightClickMouse = MinecraftClient.class.getDeclaredMethod(Transformer.REFLECTION.RightClickMouse().get());
			rightClickMouse.setAccessible(true);
			for(Field f : MinecraftClient.class.getDeclaredFields()) {
				if(f.getType().isAssignableFrom(Session.class)) {
					sessionField = f;
					break;
				}
			}
			sessionField.setAccessible(true);
		} catch(Exception e) {
			//ZIG116 throw new RuntimeException(e);
			e.printStackTrace();
		}
	}

	private Window scaledResolution;
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
				getMinecraft().keyboard.setRepeatEvents(repeat);
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
				return (int)getMinecraft().mouse.getX();
			}

			@Override
			public int getY() {
				return (int) (getMinecraft().getWindow().getHeight() - getMinecraft().mouse.getY());
			}
		});
		Display.init(new Display.DisplayHandler() {
			@Override
			public boolean isActive() {
				return GLFW.glfwGetWindowAttrib(scaledResolution.getHandle(), GLFW.GLFW_FOCUSED)
						!= 0;
			}
		});
		previousCallback = GLFW.glfwSetKeyCallback(getMinecraft().getWindow().getHandle(), this);
		updateScaledResolution();
		try {
			this.resourceManager = new ResourceManager(getGameProfile());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		// Set custom max values
		Option.GAMMA.setMax(10f);
		Option.FOV.setMax(130f);

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
			getFontrenderer().drawWithShadow(MatrixStacks.hudMatrixStack, string, x, y, color);
		}
		else {
			getFontrenderer().draw(MatrixStacks.hudMatrixStack, string, x, y, color);
		}

	}

	@Override
	public List<String> splitStringToWidth(String string, int width) {
		Validate.isTrue(width > 0);
		if (string == null)
			return Collections.emptyList();
		if (string.isEmpty())
			return Collections.singletonList("");
		return getFontrenderer().wrapLines(ChatComponentBuilder.fromLegacyText(string), width).stream().map(ChatUtils::getText)
				.collect(Collectors.toList());
	}

	@Override
	public int getStringWidth(String string) {
		return getFontrenderer().getWidth(string);
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
	public <E extends Row> IGuiList<E> createGuiList(Clickable<E> clickable, int width, int height, int top, int bottom, int left, int right, GuiArrayList<E> rows) {
		GuiList<E> list = new GuiList<>(clickable, width, height, top, bottom, left, right, rows);
		if(rows != null) rows.setParentList(list);
		return list;
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
		if(forgeChatField == null) return null;
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
		return new LiteralText(prefix).getSiblings().add((Text) originalChatComponent);
	}

	@Override
	public boolean isSignGUIOpened() {
		return getMinecraftScreen() instanceof SignEditScreen;
	}

	@Override
	public void registerKeybindings(List<IKeybinding> keybindings) {
		KeyBinding[] currentKeybindings = getGameSettings().keysAll;
		KeyBinding[] customKeybindings = new KeyBinding[keybindings.size()];
		for (int i = 0; i < keybindings.size(); i++) {
			customKeybindings[i] = (KeyBinding) keybindings.get(i);
		}
		//getGameSettings().keysAll = Utils.concat(currentKeybindings, customKeybindings);
		//ZIG116 ((MixinGameSettings)getGameSettings()).setKeyBindings(Utils.concat(currentKeybindings, customKeybindings));

		getGameSettings().load();
	}

	@Override
	public void playSound(String sound, float pitch) {
		playSound("minecraft", sound, pitch);
	}

	@Override
	public void playSound(String domain, String sound, float pitch) {
		getMinecraft().getSoundManager().play(PositionedSoundInstance.master(new SoundEvent(new ResourceLocation(domain, sound)), pitch));
	}

	@Override
	public int getFontHeight() {
		return getFontrenderer().fontHeight;
	}

	public ServerInfo getServerData() {
		return getMinecraft().getCurrentServerEntry();
	}

	@Override
	public void resetServer() {
		getMinecraft().setCurrentServerEntry(null);
	}

	@Override
	public String getServer() {
		ServerInfo serverData = getServerData();
		if (serverData == null)
			return null;
		return serverData.address;
	}

	@Override
	public List<NetworkPlayerInfo> getServerPlayers() {
		List<NetworkPlayerInfo> result = Lists.newArrayList();
		for (PlayerListEntry wrapped : getPlayer().networkHandler.getPlayerList()) {
			result.add(new WrappedNetworkPlayerInfo(wrapped));
		}
		return result;
	}

	@Override
	public boolean isPlayerListShown() {
		return (getGameSettings().keyPlayerList.isPressed()) && ((!getMinecraft().isIntegratedServerRunning()) ||
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
		getGameSettings().smoothCameraEnabled = smoothCamera;
	}

	@Override
	public boolean isSmoothCamera() {
		return getGameSettings().smoothCameraEnabled;
	}

	@Override
	public String translate(String location, Object... values) {
		return I18n.translate(location, values);
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
		getMinecraft().openScreen((Screen) gui);
	}

	@Override
	public void joinServer(String host, int port) {
		MinecraftFactory.getClassProxyCallback().resetServer();
		displayScreen(new ConnectScreen((Screen) getMinecraftScreen(), getMinecraft(), new ServerInfo(host, host + ":" + port, false)));
	}

	@Override
	public void joinServer(Object parentScreen, Object serverData) {
		if (serverData == null) {
			return;
		}
		MinecraftFactory.getClassProxyCallback().resetServer();
		displayScreen(new ConnectScreen((Screen) parentScreen, getMinecraft(), (ServerInfo) serverData));
	}

	@Override
	public void disconnectFromWorld() {
		boolean isOnIntegratedServer = getMinecraft().isIntegratedServerRunning();
		boolean isConnectedToRealms = getMinecraft().isConnectedToRealms();
		getMinecraft().joinWorld(null);
		if (isOnIntegratedServer) {
			displayScreen(new TitleScreen());
		} else if (isConnectedToRealms) {
			RealmsBridgeScreen realmsBridge = new RealmsBridgeScreen();
			realmsBridge.switchToRealms(new TitleScreen());
		} else {
			displayScreen(new MultiplayerScreen(new TitleScreen()));
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

	public MinecraftClient getMinecraft() {
		return MinecraftClient.getInstance();
	}

	public TextRenderer getFontrenderer() {
		return getMinecraft().textRenderer;
	}

	public GameOptions getGameSettings() {
		return getMinecraft().options;
	}

	public ClientPlayerEntity getPlayer() {
		return getMinecraft().player;
	}

	public World getWorld() {
		return getMinecraft().world;
	}

	public InGameHud getGuiIngame() {
		return getMinecraft().inGameHud;
	}

	@Override
	public boolean isSpectatingSelf() {
		return getSpectatingEntity() instanceof PlayerEntity;
	}

	@Override
	public PlayerGameMode getGameMode() {
		return PlayerGameMode.values()[getMinecraft().interactionManager.getCurrentGameMode().getId()];
	}

	public Entity getSpectatingEntity() {
		return getMinecraft().getCameraEntity();
	}

	@Override
	public String getOpenContainerTitle() {
		return "";
	}

	@Override
	public void closeContainer() {
		getPlayer().closeScreen();
	}

	@Override
	public String getSession() {
		return getMinecraft().getSession().getAccessToken();
	}

	@Override
	public String getUsername() {
		return getMinecraft().getSession().getUsername();
	}

	@Override
	public Proxy getProxy() {
		return getMinecraft().getNetworkProxy();
	}

	@Override
	public GameProfile getGameProfile() {
		return getMinecraft().getSession().getProfile();
	}

	@Override
	public String getFPS() {
		return MinecraftClient.getInstance().fpsDebugString;
	}

	@Override
	public boolean isPlayerNull() {
		return getPlayer() == null;
	}

	@Override
	public boolean isTerrainLoading() {
		return getMinecraftScreen() instanceof DownloadingTerrainScreen;
	}

	@Override
	public double getPlayerPosX() {
		return getSpectatingEntity().lastRenderX;
	}

	@Override
	public double getPlayerPosY() {
		return getSpectatingEntity().lastRenderY;
	}

	@Override
	public double getPlayerPosZ() {
		return getSpectatingEntity().lastRenderZ;
	}

	@Override
	public float getPlayerRotationYaw() {
		return getSpectatingEntity().yaw;
	}

	@Override
	public float getPlayerRotationPitch() {
		return getSpectatingEntity().pitch;
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
		return getMinecraft().crosshairTarget != null && getMinecraft().crosshairTarget.getType().ordinal() == 1;
	}

	@Override
	public int getTargetBlockX() {
		return (int) getMinecraft().crosshairTarget.getPos().getX();
	}

	@Override
	public int getTargetBlockY() {
		return (int) getMinecraft().crosshairTarget.getPos().getY();
	}

	@Override
	public int getTargetBlockZ() {
		return (int) getMinecraft().crosshairTarget.getPos().getZ();
	}

	@Override
	public ResourceLocation getTargetBlockName() {
		if(!hasTargetBlock()) return null;
		HitResult mouseOver = getMinecraft().crosshairTarget;
		BlockPos pos = ((BlockHitResult)mouseOver).getBlockPos();
		if(getWorld() == null) return null;
		return ResourceLocation.fromObfuscated(Registry.BLOCK.getId(getWorld().getBlockState(pos).getBlock()));
	}

	@Override
	public boolean isFancyGraphicsEnabled() {
		return MinecraftClient.isFancyGraphicsOrBetter();
	}

	@Override
	public String getBiome() {
		BlockPos localdt = new BlockPos(getPlayerPosX(), getPlayerPosY(), getPlayerPosZ());
		if (!getWorld().isChunkLoaded(localdt)) {
			return null;
		}
		Chunk localObject = getMinecraft().world.getChunk(localdt);
		return localObject.getBiomeArray().getBiomeForNoiseGen((int) getPlayerPosX(), (int) getPlayerPosY(), (int) getPlayerPosZ()).getCategory().getName();
	}

	@Override
	public int getLightLevel() {
		if (getWorld() == null) return 0;
		BlockPos localdt = new BlockPos(getPlayerPosX(), getPlayerPosY(), getPlayerPosZ());
		return getWorld().getLightLevel(localdt, 0);
	}

	@Override
	public String getEntityCount() {
		if(getMinecraft().worldRenderer == null || getMinecraft().world == null) return "No world";
		return getMinecraft().worldRenderer.getEntitiesDebugString().split("[ ,]")[1];
	}

	@Override
	public boolean isRidingEntity() {
		return false;
	}

	@Override
	public int getFoodLevel() {
		return isSpectatingSelf() ? ((PlayerEntity) getSpectatingEntity()).getHungerManager().getFoodLevel() : getPlayer().getHungerManager().getFoodLevel();
	}

	@Override
	public float getSaturation() {
		return isSpectatingSelf() ? ((PlayerEntity) getSpectatingEntity()).getHungerManager().getSaturationLevel() : 0;
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
		return getPlayer().getArmor();
	}

	@Override
	public int getAir() {
		return getPlayer().getAir();
	}

	@Override
	public boolean isPlayerInsideWater() {
		return getSpectatingEntity().isTouchingWater();
	}

	@Override
	public float getResistanceFactor() {
		float referenceDamage = 100;
		int i1 = 25 - getPlayer().getArmor();
		float d1 = referenceDamage * (float) i1;
		referenceDamage = d1 / 25.0F;

		if (getPlayer().hasStatusEffect(StatusEffects.RESISTANCE)) {
			int resistanceAmplifier = (getPlayer().getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1) * 5;
			int i = 25 - resistanceAmplifier;
			float d = referenceDamage * (float) i;
			referenceDamage = d / 25.0F;
		}

		if (referenceDamage <= 0.0F) {
			return 100;
		} else {
			int enchantmentModifierDamage = 0; //ZIG116 EnchantmentHelper.getAttackDamage(getPlayer().inventory.armor, EntityGroup.DEFAULT);
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
		for (StatusEffectInstance potionEffect : getActivePlayerPotionEffects()) {
			StatusEffect potion = getPotionByEffect(potionEffect);
			if (potion != null) {
				return wrapPotionEffect(potionEffect);
			}
		}
		return null;
	}

	@Override
	public List<eu.the5zig.mod.gui.ingame.PotionEffect> getActivePotionEffects() {
		List<eu.the5zig.mod.gui.ingame.PotionEffect> result = new ArrayList<>(getActivePlayerPotionEffects().size());
		for (StatusEffectInstance potionEffect : getActivePlayerPotionEffects()) {
			result.add(wrapPotionEffect(potionEffect));
		}
		Collections.sort(result);
		return result;
	}

	@Override
	public List<? extends eu.the5zig.mod.gui.ingame.PotionEffect> getDummyPotionEffects() {
		return DUMMY_POTIONS;
	}

	private PotionEffectImpl wrapPotionEffect(StatusEffectInstance potionEffect) {
		StatusEffect potion = getPotionByEffect(potionEffect);
		return new PotionEffectImpl(potion == null ? "" : potionEffect.getTranslationKey(), potionEffect.getDuration(),
				StatusEffectUtil.durationToString(potionEffect, 1), potionEffect.getAmplifier() + 1, potion == null ? -1 : StatusEffect.getRawId(potion),
				potion == null || potionEffect.getEffectType().isBeneficial(), true, potion == null ? 0 : potionEffect.getEffectType().getColor());
	}

	@Override
	public int getPotionEffectIndicatorHeight() {
		return 0;
	}

	@Override
	public boolean isHungerPotionActive() {
		return getPlayer().hasStatusEffect(StatusEffects.HUNGER);
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

	public Collection<StatusEffectInstance> getActivePlayerPotionEffects() {
		return getPlayer().getActiveStatusEffects().values();
	}

	public StatusEffect getPotionByEffect(StatusEffectInstance potionEffect) {
		return potionEffect.getEffectType();
	}

	public net.minecraft.item.ItemStack getPlayerItemInHand() {
		return getPlayer().getMainHandStack();
	}

	public net.minecraft.item.ItemStack getArmorItemBySlot(int slot) {
		return getPlayer().inventory.getArmorStack(slot);
	}

	@Override
	public ItemStack getItemByName(String resourceName) {
		return getItemByName(resourceName, 1);
	}

	@Override
	public ItemStack getItemByName(String resourceName, int amount) {
		String[] data = resourceName.split(":");
		ResourceLocation loc = new ResourceLocation(data[0], data[1]);
		return new WrappedItemStack(new net.minecraft.item.ItemStack(Registry.ITEM.get(loc), amount));
	}

	@Override
	public int getItemCount(String key) {
		int count = 0;
		for (net.minecraft.item.ItemStack itemStack : getPlayer().inventory.main) {
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
		return getPlayer().inventory.selectedSlot;
	}

	@Override
	public void setSelectedHotbarSlot(int slot) {
		getPlayer().inventory.selectedSlot = slot;
		getNetworkManager().send(new HeldItemChangeS2CPacket(slot));
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
		RenderSystem.enableRescaleNormal();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		float cooldown = (float)itemStack.getCooldown();
		if (cooldown > 0.0f) {
			RenderSystem.pushMatrix();
			float h = 1.0f + cooldown / 5.0f;
			RenderSystem.translatef(x + 8, y + 12, 0.0f);
			RenderSystem.scalef(1.0f / h, (h + 1.0f) / 2.0f, 1.0f);
			RenderSystem.translatef(-(x + 8), -(y + 12), 0.0f);
		}
		ItemRenderer itemRenderer = getMinecraft().getItemRenderer();
		itemRenderer.renderInGuiWithOverrides(getPlayer(), itemStack, x, y);
		if (cooldown > 0.0f) {
			RenderSystem.popMatrix();
		}
		itemRenderer.renderGuiItemOverlay(getFontrenderer(), itemStack, x, y);
		RenderSystem.disableRescaleNormal();
		RenderSystem.disableBlend();
	}

	@Override
	public void updateScaledResolution() {
		this.scaledResolution = getMinecraft().getWindow();
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
		return (int) scaledResolution.getScaleFactor();
	}

	@Override
	public void drawIngameTexturedModalRect(int x, int y, int u, int v, int width, int height) {
		if (getGuiIngame() != null) {
			getGuiIngame().drawTexture(MatrixStacks.hudMatrixStack, x, y, u, v, width, height);
		}
	}

	@Override
	public boolean showDebugScreen() {
		return getGameSettings().debugEnabled;
	}

	@Override
	public boolean isPlayerSpectating() {
		return !isSpectatingSelf() || getPlayerController().getCurrentGameMode() == GameMode.SPECTATOR;
	}

	@Override
	public boolean shouldDrawHUD() {
		return getPlayerController().hasStatusBars();
	}

	@Override
	public String[] getHotbarKeys() {
		String[] result = new String[9];
		KeyBinding[] hotbarBindings = getGameSettings().keysHotbar;
		for (int i = 0; i < Math.min(result.length, hotbarBindings.length); i++) {
			result[i] = KeyBinding.getLocalizedName(hotbarBindings[i].getTranslationKey()).get().getString();
		}
		return result;
	}

	@Override
	public String getKeyDisplayStringShort(int key) {
		String tempName;
		return key < 0 ? "M" + (key + 101) : (key < 256 ? ((tempName = GLFW.glfwGetKeyName(key, -1)) == null ?
				GLFW.glfwGetKeyName(-1, key) : tempName) : String.format("%c", (char) (key - 256)).toUpperCase());
	}

	private ClientPlayerInteractionManager getPlayerController() {
		return getMinecraft().interactionManager;
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

	public void messagePlayer(Text chatComponent) {
		boolean cancel = MinecraftFactory.getClassProxyCallback().shouldCancelChatMessage(chatComponent.getString().replace(ChatColor.RESET.toString(), ""), chatComponent);
		if (!cancel) {
			getPlayer().sendMessage(chatComponent, false);
		}
	}

	@Override
	public void sendMessage(String message) {
		getPlayer().networkHandler.sendPacket(new ChatMessageC2SPacket(message));
	}

	@Override
	public boolean hasNetworkManager() {
		return getNetworkManager() != null;
	}

	@Override
	public void sendCustomPayload(String channel, ByteBuf payload) {
		if (getNetworkManager() != null) {
			getNetworkManager().send(new CustomPayloadC2SPacket(Identifier.splitOn(channel, ':'),
					new PacketByteBuf(payload)));
		}
	}

	@Override
	public boolean isLocalWorld() {
		return hasNetworkManager() && getNetworkManager().isLocal();
	}

	private ClientConnection getNetworkManager() {
		return getMinecraft().getNetworkHandler() != null ? getMinecraft().getNetworkHandler().getConnection() : null;
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
			return getTextureManager().registerDynamicTexture(name, new NativeImageBackedTexture(NativeImage.read(in)));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void bindTexture(Object resourceLocation) {
		if (resourceLocation instanceof NativeImageBackedTexture) {
			GlStateManager.bindTexture(((NativeImageBackedTexture)resourceLocation).getGlId());
		} else {
			getTextureManager().bindTexture((Identifier) resourceLocation);
		}
	}

	@Override
	public void deleteTexture(Object resourceLocation) {
		if (resourceLocation instanceof Identifier) {
			getTextureManager().destroyTexture((Identifier) resourceLocation);
		}
	}

	@Override
	public Object createDynamicImage(Object resourceLocation, int width, int height) {
		NativeImageBackedTexture dynamicImage = new NativeImageBackedTexture(width, height, false);
		getTextureManager().registerDynamicTexture(resourceLocation.toString(), dynamicImage);
		return dynamicImage;
	}

	@Override
	public Object getTexture(Object resourceLocation) {
		return getTextureManager().getTexture((Identifier) resourceLocation);
	}

	@Override
	public void fillDynamicImage(Object dynamicImage, BufferedImage image) {
		image.getRGB(0, 0, image.getWidth(), image.getHeight(), ((NativeImageBackedTexture) dynamicImage).getImage().makePixelArray(), 0, image.getWidth());
		((NativeImageBackedTexture) dynamicImage).upload();
	}

	@Override
	public void renderPotionIcon(int index) {
		StatusEffect effect = StatusEffect.byRawId(index);
		if(effect == null) return;
		StatusEffectSpriteManager potionspriteuploader = this.getMinecraft().getStatusEffectSpriteManager();
		Sprite sprite = potionspriteuploader.getSprite(effect);
		this.getMinecraft().getTextureManager().bindTexture(sprite.getAtlas().getId());
		DrawableHelper.drawSprite(MatrixStacks.hudMatrixStack, 0, 0, 0, 18, 18, sprite);
	}

	public TextureManager getTextureManager() {
		return getMinecraft().getTextureManager();
	}

	@Override
	public void renderTextureOverlay(int x1, int x2, int y1, int y2) {
		Tessellator var4 = Tessellator.getInstance();
		BufferBuilder var5 = var4.getBuffer();
		bindTexture(Screen.OPTIONS_BACKGROUND_TEXTURE);
		GLUtil.color(1.0F, 1.0F, 1.0F, 1.0F);
		var5.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
		var5.vertex(x1, y2, 0.0D).texture(0.0F, ((float) y2 / 32.0F)).color(64, 64, 64, 255).next();
		var5.vertex(x1 + x2, y2, 0.0D).texture(((float) x2 / 32.0F), ((float) y2 / 32.0F)).color(64, 64, 64, 255).next();
		var5.vertex(x1 + x2, y1, 0.0D).texture(((float) x2 / 32.0F), ((float) y1 / 32.0F)).color(64, 64, 64, 255).next();
		var5.vertex(x1, y1, 0.0D).texture(0.0F, ((float) y1 / 32.0F)).color(64, 64, 64, 255).next();
		var4.draw();
	}

	@Override
	public void setIngameFocus() {
		getMinecraft().onWindowFocusChanged(true);
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
		ScoreboardObjective objective = scoreboard.getObjectiveForSlot(1);
		if (objective == null) {
			return null;
		}
		String displayName = objective.getDisplayName().getString();
		Collection<ScoreboardPlayerScore> scores = scoreboard.getAllPlayerScores(objective);
		HashMap<String, Integer> lines = Maps.newHashMap();
		for (ScoreboardPlayerScore score : scores) {
			lines.put(score.getPlayerName(), score.getScore());
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
		return getMinecraft().isOnThread();
	}

	@Override
	public File getMinecraftDataDirectory() {
		return getMinecraft().runDirectory;
	}

	@Override
	public void dispatchKeypresses() {
		/* ZIG116
		int eventKey = org.lwjgl.input.Keyboard.getEventKey();
		int currentcode = eventKey == 0 ? org.lwjgl.input.Keyboard.getEventCharacter() : eventKey;
		if ((currentcode == 0) || (org.lwjgl.input.Keyboard.isRepeatEvent()))
			return;

		if (org.lwjgl.input.Keyboard.getEventKeyState()) {
			int keyCode = currentcode + (eventKey == 0 ? 256 : 0);
			MinecraftFactory.getClassProxyCallback().fireKeyPressEvent(keyCode);
		}*/
	}

	@Override
	public void shutdown() {
		getMinecraft().close();
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
			sessionField.set(MinecraftClient.getInstance(), session);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void sendChatComponent(MessageComponent component, boolean secondChat) {
		Text result = ChatComponentBuilder.fromInterface(component);
		if(secondChat) gui2ndChat.printChatMessage(result);
		else getMinecraft().inGameHud.getChatHud().addMessage(result);
	}
}
