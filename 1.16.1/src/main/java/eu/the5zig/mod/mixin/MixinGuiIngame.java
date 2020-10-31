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
import eu.the5zig.mod.util.MatrixStacks;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class MixinGuiIngame {
    @Inject(method = "renderHotbar", at = @At("TAIL"))
    protected void renderHotbarPost(float _pTicks, MatrixStack stack, CallbackInfo _ci) {
        MatrixStacks.hudMatrixStack = stack;
        The5zigMod.getGuiIngame().onRenderHotbar();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void updateTick(CallbackInfo _ci) {
        The5zigMod.getGuiIngame().tick();
    }

    @Inject(method = "renderStatusBars", at = @At(value = "INVOKE", target = "net/minecraft/util/profiler/Profiler.swap(Ljava/lang/String;)V",
            ordinal = 1))
    public void patchFood(MatrixStack stack, CallbackInfo _ci) {
        MatrixStacks.hudMatrixStack = stack;
        The5zigMod.getGuiIngame().onRenderFood();
    }

    @Inject(method = "renderStatusEffectOverlay", at = @At(value = "HEAD"), cancellable = true)
    protected void renderVignette(MatrixStack _stack, CallbackInfo ci) {
        if(!The5zigMod.getConfig().getBool("showVanillaPotionIndicator"))
            ci.cancel();
    }
}
