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
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiIngame.class)
public abstract class MixinGuiIngame {

    @Shadow
    protected int updateCounter;

    @Inject(method = "renderTooltip", at = @At("HEAD"))
    protected void renderOverlay(ScaledResolution res, float pTicks, CallbackInfo _ci) {
        The5zigMod.getGuiIngame().renderGameOverlay();
        The5zigMod.getGuiIngame().onRenderHotbar();
    }

    @Inject(method = "updateTick", at = @At("TAIL"))
    public void updateTick(CallbackInfo _ci) {
        The5zigMod.getGuiIngame().tick();
    }

    @Inject(method = "renderGameOverlay", at = @At(value = "INVOKE",
            target = "net/minecraft/profiler/Profiler.startSection(Ljava/lang/String;)V", ordinal = 4))
    public void patchChat(float pTicks, CallbackInfo _ci) {
        The5zigMod.getVars().get2ndChat().draw(updateCounter);
    }

    @Inject(method = "renderPlayerStats", at = @At(value = "INVOKE", target = "net/minecraft/profiler/Profiler.endStartSection(Ljava/lang/String;)V",
            ordinal = 1))
    public void patchFood(ScaledResolution res, CallbackInfo _ci) {
        The5zigMod.getGuiIngame().onRenderFood();
    }
}
