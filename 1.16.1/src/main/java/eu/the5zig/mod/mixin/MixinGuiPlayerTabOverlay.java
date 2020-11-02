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

import com.mojang.blaze3d.systems.RenderSystem;
import eu.the5zig.mod.The5zigMod;
import eu.the5zig.util.minecraft.ChatColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerListHud.class)
public abstract class MixinGuiPlayerTabOverlay extends DrawableHelper {
    @Inject(method = "renderLatencyIcon", at = @At("HEAD"), cancellable = true)
    public void drawPing(MatrixStack stack, int offset, int x, int y, PlayerListEntry info, CallbackInfo ci) {
        if(The5zigMod.getConfig().getBool("pingOnTab")) {
            int ping = info.getLatency();
            String display = ping <= 0 ? "?" : Integer.toString(ping);
            int color = ChatColor.getPingColor(ping);
            stack.push();
            RenderSystem.color3f(1f, 1f, 1f);
            if(MinecraftClient.getInstance().options.forceUnicodeFont) {
                stack.scale(1f, 1f, 1f); // Unicode fonts are much smaller
                int rescale = 1;
                drawCenteredString(stack, MinecraftClient.getInstance().textRenderer, display, (x + offset) * rescale - 5,
                        y * rescale, color);
            }
            else {
                stack.scale(0.5f, 0.5f, 0.5f); // 0.5 gives the best result in my opinion
                int rescale = 2; // 1/0.5
                drawCenteredString(stack, MinecraftClient.getInstance().textRenderer, display, (x + offset) * rescale - 11,
                        y * rescale + 3, color);
            }
            stack.pop();
            ci.cancel();
        }
    }
}
