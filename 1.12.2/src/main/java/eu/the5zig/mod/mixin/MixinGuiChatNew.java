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

import eu.the5zig.mod.The5zigMod;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiNewChat.class)
public abstract class MixinGuiChatNew {

    @ModifyArg(method = "setChatLine", at = @At("HEAD"), index = 0)
    public ITextComponent setChatLine(ITextComponent chatComponent, int chatLineId, int updateCounter, boolean displayOnly, CallbackInfo _ci) {
        if(The5zigMod.getConfig().getBool("chatTimePrefixEnabled")) {
            return (ITextComponent) The5zigMod.getDataManager().getChatComponentWithTime(chatComponent);
        }
        return chatComponent;
    }

    @Inject(method = "drawChat", at = @At("HEAD"))
    public void drawChat(int upd, CallbackInfo _ci) {

    }

    @Inject(method = "clearChatMessages", at = @At("HEAD"))
    public void clearChatMessages(boolean b, CallbackInfo _ci) {
        The5zigMod.getVars().get2ndChat().clear();
    }
}
