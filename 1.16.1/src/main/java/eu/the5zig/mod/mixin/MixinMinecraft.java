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

import eu.the5zig.mod.ScreenOpenCallback;
import eu.the5zig.mod.The5zigMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraft {
    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo _ci) {
        The5zigMod.getListener().onTick();
    }

    @Inject(method = "openScreen", at = @At("HEAD"), cancellable = true)
    public void openScreen(Screen toOpen, CallbackInfo ci) {
        Screen current = MinecraftClient.getInstance().currentScreen;
        ActionResult result = ScreenOpenCallback.EVENT.invoker().open(current, toOpen);
        if(result == ActionResult.FAIL) ci.cancel();
    }

    @Inject(method = "doAttack", at = @At("HEAD"))
    public void leftClickCPS(CallbackInfo _ci) {
        The5zigMod.getDataManager().getCpsManager().getLeftClickCounter().incrementCount();
    }

    @Inject(method = "doItemUse", at = @At("HEAD"))
    public void rightClickCPS(CallbackInfo _ci) {
        The5zigMod.getDataManager().getCpsManager().getRightClickCounter().incrementCount();
    }
}
