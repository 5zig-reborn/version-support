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

import eu.the5zig.mod.MinecraftFactory;
import eu.the5zig.mod.The5zigMod;
import eu.the5zig.mod.gui.ingame.IGui2ndChat;
import eu.the5zig.mod.util.ChatComponentBuilder;
import eu.the5zig.mod.util.MatrixStacks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.Nullable;

public class Gui2ndChat extends ChatHud implements IGui2ndChat {
    private final Gui2ndChatScreen handle;

    public Gui2ndChat() {
        super(MinecraftClient.getInstance());
        handle = new Gui2ndChatScreen();
    }


    @Override
    public void draw(int updateCounter) {
        if(!MinecraftFactory.getClassProxyCallback().is2ndChatVisible()) return;
        MatrixStacks.chatMatrixStack.push();
        MatrixStacks.chatMatrixStack.translate(getLeft(), 0, 1);
        this.render(MatrixStacks.chatMatrixStack, updateCounter);
        MatrixStacks.chatMatrixStack.pop();
    }

    private double getLeft() {
        return MinecraftFactory.getVars().getScaledWidth() - getWidth();
    }

    @Override
    public void printChatMessage(String message) {
        printChatMessage(ChatComponentBuilder.fromLegacyText(message));
    }

    @Override
    public void printChatMessage(Object chatComponent) {
        if (!(chatComponent instanceof Text)) throw new IllegalArgumentException("Chat component must be a Text");
        Text comp = (Text) chatComponent;
        LogManager.getLogger().info("[CHAT2] {}", comp.getString());
        super.queueMessage(comp);
    }

    @Override
    public void clear() {
        super.clear(false);
    }

    @Override
    public void refreshChat() {
        this.resetScroll();
    }

    @Override
    public int getLineCount() {
        return super.getVisibleLineCount();
    }

    @Override
    public void scroll(int scroll) {
        super.scroll(scroll);
    }

    @Override
    public void drawComponentHover(int mouseX, int mouseY) {
        Style style = getText(mouseX, mouseY);
        if(style == null) return;
        handle.renderTextHoverEffect(MatrixStacks.chatMatrixStack, style, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        Style text = getText(mouseX, mouseY);
        if (text == null) return false;
        return handle.handleTextClick(text);
    }

    @Override
    public @Nullable Style getText(double x, double y) {
        return super.getText(x - getLeft(), y);
    }

    @Override
    public void keyTyped(int code) {

    }

    private static class Gui2ndChatScreen extends Screen {
        private Screen chatScreen;

        protected Gui2ndChatScreen() {
            super(new LiteralText(""));
            init(MinecraftClient.getInstance(), Integer.MAX_VALUE, Integer.MAX_VALUE);
        }

        @Override
        protected void init() {
            // MC opens this "screen" after clicking on a link, reset focus instead
            if(MinecraftClient.getInstance().currentScreen == this) MinecraftClient.getInstance().openScreen(chatScreen);
        }

        @Override
        protected void renderTextHoverEffect(MatrixStack matrices, @Nullable Style style, int x, int y) {
            super.renderTextHoverEffect(matrices, style, x, y);
        }

        @Override
        public boolean handleTextClick(@Nullable Style style) {
            chatScreen = MinecraftClient.getInstance().currentScreen;
            return super.handleTextClick(style);
        }

        @Override
        protected void insertText(String text, boolean override) {
            The5zigMod.getVars().typeInChatGUI(text);
        }
    }
}
