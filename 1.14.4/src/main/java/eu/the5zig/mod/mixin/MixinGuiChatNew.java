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
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.NewChatGui;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NewChatGui.class)
public abstract class MixinGuiChatNew {
    private static final String fillTarget = "net/minecraft/client/gui/NewChatGui.fill(IIIII)V";
    private ITextComponent lastComponent;
    private int lastY, lastAlpha;

    @Inject(method = "func_194813_a", at = @At("HEAD"))
    public void scroll(double amount, CallbackInfo _ci) {
        The5zigMod.getVars().get2ndChat().scroll((int)amount);
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void drawChat(int upd, CallbackInfo _ci) {
        The5zigMod.getVars().get2ndChat().draw(upd);
    }


    @ModifyVariable(method = "render", at = @At(value = "INVOKE", ordinal = 0, target = fillTarget))
    public ChatLine getComponent(ChatLine previous) {
        lastComponent = previous.getChatComponent();
        return previous;
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", ordinal = 0, target = fillTarget),
            index = 1)
    public int getLastY(int previous) {
        lastY = previous;
        return previous;
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", ordinal = 0, target = fillTarget),
            index = 4)
    public int customAlpha(int previous) {
        lastAlpha = (previous >> 24) * 2;
        if(lastComponent == null) return previous;
        return previous * (The5zigMod.getConfig().getBool("transparentChatBackground") ? 0 : 1);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", ordinal = 0, target = fillTarget, shift = At.Shift.AFTER))
    public void callHighlight(int upd, CallbackInfo _ci) {
        if(lastComponent == null) return;
        ChatUtils.highlightChatLine(lastComponent, 0, lastY, lastAlpha);
    }

    @Inject(method = "clearChatMessages", at = @At("HEAD"))
    public void clearChatMessages(CallbackInfo _ci) {
        The5zigMod.getVars().get2ndChat().clear();
    }

    @ModifyVariable(method = "setChatLine", at = @At("HEAD"), argsOnly = true, index = 1)
    public ITextComponent setChatLine(ITextComponent chatComponent) {
        if(The5zigMod.getConfig().getBool("chatTimePrefixEnabled")) {
            return (ITextComponent) The5zigMod.getDataManager().getChatComponentWithTime(chatComponent);
        }
        return chatComponent;
    }

    @ModifyConstant(method = "setChatLine")
    public int maxLines(int maxIn) {
        if(maxIn == 100) {
            return MinecraftFactory.getClassProxyCallback().getMaxChatLines();
        }
        return maxIn;
    }
}
