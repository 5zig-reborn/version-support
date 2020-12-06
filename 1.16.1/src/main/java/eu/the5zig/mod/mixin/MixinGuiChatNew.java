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

package eu.the5zig.mod.mixin;

import eu.the5zig.mod.MinecraftFactory;
import eu.the5zig.mod.The5zigMod;
import eu.the5zig.mod.util.ChatUtils;
import eu.the5zig.mod.util.MatrixStacks;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public abstract class MixinGuiChatNew {
    private static final String fillTarget = "net/minecraft/client/gui/hud/ChatHud.fill(Lnet/minecraft/client/util/math/MatrixStack;IIIII)V";
    private static final Class<?> secondChatClass;

    private OrderedText lastComponent;
    private int lastY, lastAlpha;

    static {
        try {
            secondChatClass = Class.forName("Gui2ndChat");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Inject(method = "scroll", at = @At("HEAD"))
    public void scroll(double amount, CallbackInfo _ci) {
        if(!secondChatClass.isAssignableFrom(this.getClass())) {
            The5zigMod.getVars().get2ndChat().scroll((int) amount);
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void drawChat(MatrixStack stack, int upd, CallbackInfo _ci) {
        if(!secondChatClass.isAssignableFrom(this.getClass())) {
            MatrixStacks.chatMatrixStack = stack;
            The5zigMod.getVars().get2ndChat().draw(upd);
        }
    }

    @ModifyVariable(method = "render", at = @At(value = "INVOKE", ordinal = 0, target = fillTarget))
    public ChatHudLine getComponent(ChatHudLine previous) {
        if(previous.getText() instanceof OrderedText) lastComponent = (OrderedText) previous.getText();
        return previous;
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", ordinal = 0, target = fillTarget),
            index = 2)
    public int getLastY(int previous) {
        lastY = previous;
        return previous;
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = fillTarget),
            index = 5)
    public int customAlpha(int previous) {
        lastAlpha = (previous >> 24) * 2;
        return previous * (The5zigMod.getConfig().getBool("transparentChatBackground") ? 0 : 1);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = fillTarget, shift = At.Shift.AFTER))
    public void callHighlight(MatrixStack stack, int upd, CallbackInfo _ci) {
        if(lastComponent == null) return;
        ChatUtils.highlightChatLine(stack, lastComponent, 0, lastY, lastAlpha);
    }

    @Inject(method = "clear", at = @At("HEAD"))
    public void clearChatMessages(boolean _history, CallbackInfo _ci) {
        if(!secondChatClass.isAssignableFrom(this.getClass())) {
            The5zigMod.getVars().get2ndChat().clear();
        }
    }

    @ModifyVariable(method = "queueMessage", at = @At("HEAD"), argsOnly = true, index = 1)
    public Text setChatLine(Text chatComponent) {
        if(The5zigMod.getConfig().getBool("chatTimePrefixEnabled")) {
            return (Text) The5zigMod.getDataManager().getChatComponentWithTime(chatComponent);
        }
        return chatComponent;
    }

    @ModifyConstant(method = "queueMessage")
    public int maxLines(int maxIn) {
        if(maxIn == 100) {
            return MinecraftFactory.getClassProxyCallback().getMaxChatLines();
        }
        return maxIn;
    }
}
