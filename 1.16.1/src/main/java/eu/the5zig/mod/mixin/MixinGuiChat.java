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
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class MixinGuiChat {
    @Inject(method = "render", at = @At("HEAD"))
    public void drawScreen(MatrixStack _stack, int mouseX, int mouseY, float pTicks, CallbackInfo _ci) {
        The5zigMod.getVars().get2ndChat().drawComponentHover(mouseX, mouseY);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"))
    public void keyTyped(int c, int keyCode, int state, CallbackInfoReturnable<Boolean> _ci) {
        The5zigMod.getVars().get2ndChat().keyTyped(keyCode);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void mouseClicked(double mouseX, double mouseY, int btn, CallbackInfoReturnable<Boolean> ci) {
        boolean b = The5zigMod.getRenderer().getChatSymbolsRenderer().mouseClicked((int)mouseX, (int)mouseY) ||
                The5zigMod.getVars().get2ndChat().mouseClicked((int)mouseX, (int)mouseY, btn);
        if(b) ci.setReturnValue(false);
    }
}
