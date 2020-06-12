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

import eu.the5zig.mod.The5zigMod;
import net.minecraft.client.gui.GuiChat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiChat.class)
public abstract class MixinGuiChat {

    @Shadow
    public abstract void onAutocompleteResponse(String[] completions);

    @Shadow private boolean waitingOnAutocomplete;

    @Inject(method = "onGuiClosed", at = @At("HEAD"))
    public void onGuiClosed(CallbackInfo _ci) {
        The5zigMod.getVars().get2ndChat().resetScroll();
    }

    @Inject(method = "drawScreen", at = @At("HEAD"))
    public void drawScreen(int mouseX, int mouseY, float pTicks, CallbackInfo _ci) {
        The5zigMod.getVars().get2ndChat().drawComponentHover(mouseX, mouseY);
    }

    @Inject(method = "keyTyped", at = @At("HEAD"))
    public void keyTyped(char c, int keyCode, CallbackInfo _ci) {
        The5zigMod.getVars().get2ndChat().keyTyped(keyCode);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void mouseClicked(int mouseX, int mouseY, int btn, CallbackInfo ci) {
        boolean b = The5zigMod.getRenderer().getChatSymbolsRenderer().mouseClicked(mouseX, mouseY) ||
                The5zigMod.getVars().get2ndChat().mouseClicked(mouseX, mouseY, btn);

        if(b)
            ci.cancel();
    }
}
