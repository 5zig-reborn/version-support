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
import eu.the5zig.util.minecraft.ChatColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerSelectionList.NormalEntry.class)
public abstract class MixinServerListEntry {

    @Shadow
    private ServerData server;
    private int textWidth;

    /**
     * Redirects the texture draw, so that no texture (bars) is displayed and the text is shown instead.
     */
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "net/minecraft/client/gui/AbstractGui.blit(IIFFIIII)V", ordinal = 0))
    private void redirectCall(int x, int y, float p_blit_2_, float p_blit_3_, int p_blit_4_, int p_blit_5_, int p_blit_6_, int p_blit_7_) {
        if(The5zigMod.getConfig().getBool("ping_on_serverlist")) {
            if (server.pingToServer < 0) Minecraft.getInstance().fontRenderer.drawString("?", x, y + 1, 0x808079);
            else {
                String pingFormat = String.format("%dms", server.pingToServer);
                x -= (textWidth = Minecraft.getInstance().fontRenderer.getStringWidth(pingFormat) / 2);
                Minecraft.getInstance().fontRenderer.drawString(pingFormat, x, y + 1, ChatColor.getPingColor(server.pingToServer));
            }
            GLUtil.color(1f, 1f, 1f, 1f);
        }
        else AbstractGui.blit(x, y, p_blit_2_, p_blit_3_, p_blit_4_, p_blit_5_, p_blit_6_, p_blit_7_);
    }

    /**
     * Redirects the normal player info string draw (min/max), so that it's shifted if the setting is toggled on.
     */
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "net/minecraft/client/gui/FontRenderer.drawString(Ljava/lang/String;FFI)I", ordinal = 2))
    private int redirectString(FontRenderer fr, String text, float x, float y, int color) {
        if(The5zigMod.getConfig().getBool("ping_on_serverlist"))
            x -= textWidth;
        return fr.drawString(text, x, y, color);
    }

    /**
     * Makes the tooltips follow their parent when shifted.
     */
    @ModifyVariable(method = "render", at = @At(value = "HEAD"), argsOnly = true, index = 6)
    private int editMouseX(int old) {
        return The5zigMod.getConfig().getBool("ping_on_serverlist")
                ? old + textWidth
                : old;
    }
}
