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

package eu.the5zig.mod.mixin;

import eu.the5zig.mod.MinecraftFactory;
import eu.the5zig.mod.The5zigMod;
import eu.the5zig.mod.util.ChatHighlighting;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(GuiNewChat.class)
public abstract class MixinGuiChatNew {

    private ITextComponent lastComponent;

    @Inject(method = "render", at = @At(value = "INVOKE", ordinal = 0, target = "net/minecraft/client/gui/GuiNewChat.drawRect(IIIII)V"),
        locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    public void drawChatHighlight(int var1, CallbackInfo ci, int var2, int var3, double var4, boolean var5, double var6, int var7, int var8, int var9,
                                  ChatLine var10, int var11, double var12, int var14, int var15, int var16) {
        lastComponent = var10.getChatComponent();
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", ordinal = 0, target = "net/minecraft/client/gui/GuiNewChat.drawRect(IIIII)V"),
        index = 4)
    public int customAlpha(int previous) {
        if(ChatHighlighting.shouldHighlight(lastComponent.getString())) {
            return MinecraftFactory.getClassProxyCallback().getHighlightWordsColor() + (Math.min(0x80, previous) << 24);
        }
        return previous * (The5zigMod.getConfig().getBool("transparentChatBackground") ? 0 : 1);
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
}
