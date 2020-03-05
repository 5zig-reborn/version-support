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
import eu.the5zig.mod.util.GLUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetworkPlayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiPlayerTabOverlay.class)
public abstract class MixinGuiPlayerTabOverlay extends Gui {

    @Inject(method = "drawPing", at = @At("HEAD"), cancellable = true)
    public void drawPing(int offset, int x, int y, NetworkPlayerInfo info, CallbackInfo ci) {
        if(The5zigMod.getConfig().getBool("pingOnTab")) {
            GLUtil.color(1f, 1f, 1f);
            int ping = info.getResponseTime();
            String display = ping <= 0 ? "?" : Integer.toString(ping);

            int color;
            if(ping <= 0) color = 0x949191;
            else if(ping < 100) color = 0x0ce813;
            else if(ping < 300) color = 0x068a0a;
            else if(ping < 600) color = 0xd6c360;
            else if(ping < 1000) color = 0xe33d2d;
            else color = 0x75140b;
            GLUtil.pushMatrix();
            if(Minecraft.getMinecraft().gameSettings.forceUnicodeFont) {
                GLUtil.scale(1f, 1f, 1f); // Unicode fonts are much smaller
                int rescale = 1;
                drawCenteredString(Minecraft.getMinecraft().fontRenderer, display, (x + offset) * rescale - 5,
                        y * rescale, color);
            }
            else {
                GLUtil.scale(0.5f, 0.5f, 0.5f); // 0.5 gives the best result in my opinion
                int rescale = 2; // 1/0.5
                drawCenteredString(Minecraft.getMinecraft().fontRenderer, display, (x + offset) * rescale - 11,
                        y * rescale + 3, color);
            }
            GLUtil.popMatrix();
            ci.cancel();
        }
    }
}
