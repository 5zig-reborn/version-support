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
import eu.the5zig.mod.util.CombatRangeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    @Inject(method = "displayGuiScreen", at = @At("HEAD"))
    public void displayScreen(Screen guiIn, CallbackInfo _ci) {
        if(!The5zigMod.hasBeenInitialized()) {
            The5zigMod.init();
        }
        The5zigMod.getKeybindingManager().flushTextfields();
    }

    @Inject(method = "runTick", at = @At("TAIL"))
    public void tick(CallbackInfo _ci) {
        The5zigMod.getListener().onTick();
    }

    @Inject(method = "clickMouse", at = @At(value = "INVOKE", target =
            "net/minecraft/client/multiplayer/PlayerController.attackEntity(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/entity/Entity;)V"))
    public void clickMouse(CallbackInfo _ci) {
        CombatRangeUtil.lastRange = CombatRangeUtil.lastPoint;
        CombatRangeUtil.lastAttack = System.currentTimeMillis();
    }

    @Inject(method = "processKeyBinds", at = @At(value = "INVOKE", target = "net/minecraft/client/Minecraft.rightClickMouse()V", ordinal = 0))
    public void onRightClick(CallbackInfo _ci) {
        The5zigMod.getDataManager().getCpsManager().getRightClickCounter().incrementCount();
    }
}


