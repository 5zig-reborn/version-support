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

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import eu.the5zig.mod.MinecraftFactory;
import eu.the5zig.mod.gui.ingame.IGui2ndChat;
import eu.the5zig.mod.util.ChatComponentBuilder;
import eu.the5zig.mod.util.ChatUtils;
import eu.the5zig.mod.util.MatrixStacks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.LogManager;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

/**
 * Decompiled from avt.class
 */
public class Gui2ndChat implements IGui2ndChat {

	private static final Splitter NEWLINE_SPLITTER = Splitter.on('\n');
	private static final Joiner NEWLINE_STRING_JOINER = Joiner.on("\\n");

	private final List<String> sentMessages = Lists.newArrayList();
	private final List<GuiChatLine> chatLines = Lists.newArrayList();
	private final List<ChatHudLine<OrderedText>> singleChatLines = Lists.newArrayList();
	private int scrollPos;
	private boolean isScrolled;

	private static Method clickChatComponent;
	private static Method hoverChatComponent;

	public Gui2ndChat() {
	}

	static {
		/* ZIG116
		try {
			clickChatComponent = Screen.class.getDeclaredMethod(Transformer.REFLECTION.ChatComponentClick().get(), Text.class);
			clickChatComponent.setAccessible(true);

			hoverChatComponent = Screen.class.getDeclaredMethod(Transformer.REFLECTION.ChatComponentHover().get(),
					Text.class, int.class, int.class);
			hoverChatComponent.setAccessible(true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} */
	}

	@Override
	public void draw(int updateCounter) {
		if(!MinecraftFactory.getClassProxyCallback().is2ndChatVisible()) return;
		MatrixStack stack = MatrixStacks.hudMatrixStack;
		MinecraftClient mc = MinecraftClient.getInstance();
		this.refreshChat();
		int lineCount = this.getLineCount();
		int visibleLineCount = this.singleChatLines.size();
		if (visibleLineCount <= 0) {
			return;
		}
		boolean chatOpened = this.isChatOpened();
		float scale = this.getChatScale();
		int width = MathHelper.ceil((double)this.getChatWidth() / this.getChatScale());
		RenderSystem.pushMatrix();
		RenderSystem.translatef(MinecraftFactory.getVars().getScaledWidth() - getChatWidth() - 6.0f * scale, 20.0f, 0.0f);
		RenderSystem.scaled(scale, scale, 1.0);
		double opacity = MinecraftFactory.getClassProxyCallback().get2ndChatOpacity() * (double)0.9f + (double)0.1f;
		double backgroundOpacity = mc.options.textBackgroundOpacity;
		double spacing = 9.0 * (mc.options.chatLineSpacing + 1.0);
		double spacingPost = -8.0 * (mc.options.chatLineSpacing + 1.0) + 4.0 * mc.options.chatLineSpacing;
		int totalChatLines = 0;
		for (int lineIndex = 0; lineIndex + this.scrollPos < this.singleChatLines.size() && lineIndex < lineCount; ++lineIndex) {
			int ticksPassed;
			ChatHudLine chatLine = this.singleChatLines.get(lineIndex + this.scrollPos);
			if (chatLine == null || (ticksPassed = updateCounter - chatLine.getCreationTick()) >= 200 && !chatOpened) continue;
			double tempOpacity;
			if(chatOpened) tempOpacity = 1.0;
			else {
				tempOpacity = (double) ticksPassed / 200.0;
				tempOpacity = 1.0 - tempOpacity;
				tempOpacity *= 10.0;
				tempOpacity = MathHelper.clamp(tempOpacity, 0.0, 1.0);
				tempOpacity *= tempOpacity;
			}
			int textOpacity = (int)(255.0 * tempOpacity * opacity);
			int bgOpacity = (int)(255.0 * tempOpacity * backgroundOpacity);
			++totalChatLines;
			if (textOpacity <= 3) continue;
			double y = (double)(-lineIndex) * spacing;
			stack.push();
			stack.translate(0.0, 0.0, 50.0);
			if(!MinecraftFactory.getClassProxyCallback().isChatBackgroundTransparent())
				ChatHud.fill(stack, -2, (int)(y - spacing), width + 4, (int)y, bgOpacity << 24);
            ChatUtils.highlightChatLine((OrderedText) chatLine.getText(),
                    MinecraftFactory.getClassProxyCallback().is2ndChatTextLeftbound() ? -2 :
                            (int) ((getChatWidth() - mc.textRenderer.getWidth((OrderedText) chatLine.getText())) * scale / scale),
                    (int) y - 9, textOpacity);
			RenderSystem.enableBlend();
			stack.translate(0.0, 0.0, 50.0);
			mc.textRenderer.drawWithShadow(stack, (OrderedText) chatLine.getText(), 0.0f, (float)((int)(y + spacingPost)), 0xFFFFFF + (textOpacity << 24));
			RenderSystem.disableAlphaTest();
			RenderSystem.disableBlend();
			stack.pop();
		}
		if (chatOpened) {
			int fontHeight = MinecraftFactory.getVars().getFontHeight();
			RenderSystem.translatef(-3.0f, 0.0f, 0.0f);
			int visibleLineHeight = visibleLineCount * fontHeight + visibleLineCount;
			int totalLineHeight = totalChatLines * fontHeight + totalChatLines;
			int relativeScroll = this.scrollPos * totalLineHeight / visibleLineCount;
			int relativeHeight = totalLineHeight * totalLineHeight / visibleLineHeight;
			if (visibleLineHeight != totalLineHeight) {
				int alpha = relativeScroll > 0 ? 170 : 96;
				int color = 0x3333AA;
				ChatHud.fill(stack, 0, -relativeScroll, 2, -relativeScroll - relativeHeight, color + (alpha << 24));
				ChatHud.fill(stack, 2, -relativeScroll, 1, -relativeScroll - relativeHeight, 0xCCCCCC + (alpha << 24));
			}
		}
		RenderSystem.popMatrix();
	}

	public void clearChatMessages() {
		this.singleChatLines.clear();
		this.chatLines.clear();
		this.sentMessages.clear();
	}

	@Override
	public void printChatMessage(String message) {
		this.printChatMessage(ChatComponentBuilder.fromLegacyText(message));
	}

	@Override
	public void printChatMessage(Object chatComponent) {
		if (!(chatComponent instanceof Text))
			throw new IllegalArgumentException(chatComponent.getClass().getName() + " != " + Text.class.getName());
		printChatMessage((Text) chatComponent);
	}

	public void printChatMessage(Text chatComponent) {
		this.printChatMessage(chatComponent, 0);
	}

	public void printChatMessage(Text chatComponent, int id) {
		LogManager.getLogger().info("[CHAT2] {}", chatComponent.getString());
		this.setChatLine(chatComponent, id, ((Variables) MinecraftFactory.getVars()).getGuiIngame().getTicks(), false);
	}

	private void setChatLine(Text chatComponent, int id, int currentUpdateCounter, boolean refresh) {
		if (!refresh && MinecraftFactory.getClassProxyCallback().isShowTimeBeforeChatMessage()) {
			chatComponent = (Text) MinecraftFactory.getClassProxyCallback().getChatComponentWithTime(chatComponent);
		}
		if (id != 0) {
			this.deleteChatLine(id);
		}
		int lineWidth = MathHelper.floor((double)this.getChatWidth() / this.getChatScale());
		List<OrderedText> list = ChatMessages.breakRenderedChatMessageLines(chatComponent, lineWidth, MinecraftClient.getInstance().textRenderer);
		boolean chatOpened = this.isChatOpened();
		for (OrderedText orderedText : list) {
			if (chatOpened && this.scrollPos > 0) {
				this.scroll(1);
			}
			this.singleChatLines.add(0, new ChatHudLine<OrderedText>(currentUpdateCounter, orderedText, id));
		}
		if (!refresh) {
			this.chatLines.add(0, new GuiChatLine(currentUpdateCounter, chatComponent, id));
			while (this.chatLines.size() > MinecraftFactory.getClassProxyCallback().getMaxChatLines()) {
				this.chatLines.remove(this.chatLines.size() - 1);
			}
		}

	}

	@Override
	public void clear() {
		sentMessages.clear();
		singleChatLines.clear();
		chatLines.clear();
		resetScroll();
	}

	@Override
	public void refreshChat() {
		this.singleChatLines.clear();
		this.resetScroll();

		for (int i = this.chatLines.size() - 1; i >= 0; --i) {
			GuiChatLine chatLine = this.chatLines.get(i);
			this.setChatLine(chatLine.getChatComponent(), chatLine.getChatLineID(), chatLine.getUpdatedCounter(), true);
		}

	}

	public List<String> getSentMessages() {
		return this.sentMessages;
	}

	public void addToSentMessages(String message) {
		if (this.sentMessages.isEmpty() || !this.sentMessages.get(this.sentMessages.size() - 1).equals(message)) {
			this.sentMessages.add(message);
		}

	}

	@Override
	public void resetScroll() {
		this.scrollPos = 0;
		this.isScrolled = false;
	}

	@Override
	public void scroll(int scroll) {
		this.scrollPos += scroll;
		int chatLines = this.singleChatLines.size();
		if (this.scrollPos > chatLines - this.getLineCount()) {
			this.scrollPos = chatLines - this.getLineCount();
		}

		if (this.scrollPos <= 0) {
			this.scrollPos = 0;
			this.isScrolled = false;
		}

	}

	@Override
	public void drawComponentHover(int mouseX, int mouseY) {
		if(hoverChatComponent == null) return;
		Style chatComponent = getChatComponent(mouseX, mouseY);
		try {
			hoverChatComponent.invoke(MinecraftFactory.getVars().getMinecraftScreen(),
					chatComponent, mouseX, mouseY);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int button) {
		if(clickChatComponent == null) return button == 0;
		try {
			return button == 0 && (Boolean) clickChatComponent.invoke(MinecraftFactory.getVars().getMinecraftScreen(),
					getChatComponent(mouseX, mouseY));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void keyTyped(int code) {
		if (code == 201) {
			scroll(((Variables) MinecraftFactory.getVars()).getGuiIngame().getChatHud().getVisibleLineCount() - 1);
		} else if (code == 209) {
			scroll(-((Variables) MinecraftFactory.getVars()).getGuiIngame().getChatHud().getVisibleLineCount() + 1);
		}
	}

	private Style getChatComponent(int mouseX, int mouseY) {
		if (!this.isChatOpened()) return null;
		MinecraftClient mc = MinecraftClient.getInstance();
		double x = mouseX - 2.0;
		double y = mc.getWindow().getScaledHeight() - mouseY - 40.0;
		x = MathHelper.floor(x / this.getChatScale());
		y = MathHelper.floor(y / (this.getChatScale() * (mc.options.chatLineSpacing + 1.0)));
		if (x < 0.0 || y < 0.0) {
			return null;
		}
		int lineId = Math.min(this.getLineCount(), this.singleChatLines.size());
		if (x <= (double)MathHelper.floor((double)this.getChatWidth() / this.getChatScale())) {
			if (y < (double)(9 * lineId + lineId)) {
				int scroll = (int)(y / 9.0 + (double)this.scrollPos);
				if (scroll >= 0 && scroll < this.singleChatLines.size()) {
					ChatHudLine chatHudLine = this.singleChatLines.get(scroll);
					return mc.textRenderer.getTextHandler().getStyleAt((OrderedText)chatHudLine.getText(), (int)x);
				}
			}
		}
		return null;
	}

	private String optStripColor(String text) {
		return text;
	}

	public boolean isChatOpened() {
		return MinecraftFactory.getVars().getMinecraftScreen() instanceof ChatScreen;
	}

	/**
	 * finds and deletes a Chat line by ID
	 */
	public void deleteChatLine(int id) {
		Iterator<ChatHudLine<OrderedText>> iterator = this.singleChatLines.iterator();
		ChatHudLine chatLine;

		while (iterator.hasNext()) {
			chatLine = iterator.next();

			if (chatLine.getId() == id) {
				iterator.remove();
			}
		}

		Iterator<GuiChatLine> iterator2 = this.chatLines.iterator();
		GuiChatLine chatLine2;
		while (iterator2.hasNext()) {
			chatLine2 = iterator2.next();

			if (chatLine2.getChatLineID() == id) {
				iterator.remove();
				break;
			}
		}
	}

	public int getChatWidth() {
		return MathHelper.floor(MinecraftFactory.getClassProxyCallback().get2ndChatWidth());
	}

	public int getChatHeight() {
		return MathHelper.floor(this.isChatOpened() ? MinecraftFactory.getClassProxyCallback().get2ndChatHeightFocused() : MinecraftFactory.getClassProxyCallback().get2ndChatHeightUnfocused());
	}

	public float getChatScale() {
		return MinecraftFactory.getClassProxyCallback().get2ndChatScale();
	}

	@Override
	public int getLineCount() {
		return this.getChatHeight() / 9;
	}
}
