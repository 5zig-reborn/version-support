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
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisconnectedScreen.class)
public abstract class MixinGuiDisconnected extends Screen {
    @Shadow
    private Screen parent;

    protected MixinGuiDisconnected(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("HEAD"))
    public void initGui(CallbackInfo _ci) {
        The5zigMod.getDataManager().getAutoReconnectManager().startCountdown(this.parent);
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void draw(MatrixStack _stack, int _mouseX, int _mouseY, float _partialTicks, CallbackInfo _ci) {
        MinecraftFactory.getClassProxyCallback().checkAutoreconnectCountdown(width, height);
    }
}
