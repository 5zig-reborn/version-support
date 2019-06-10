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

import eu.the5zig.mod.MinecraftFactory;
import eu.the5zig.mod.The5zigMod;
import eu.the5zig.mod.Version;
import eu.the5zig.mod.gui.elements.IButton;
import eu.the5zig.util.minecraft.ChatColor;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GuiMainMenu.class)
public abstract class MixinGuiMainMenu extends GuiScreen {

    @Inject(method = "addSingleplayerMultiplayerButtons", at = @At("TAIL"))
    public void addSingleplayerMultiplayerButtons(int y1, int rowHeight, CallbackInfo _ci) {
        if (!MinecraftFactory.getClassProxyCallback().isShowLastServer())
            return;
        String lastServer;
        GuiButton button;
        String server = MinecraftFactory.getClassProxyCallback().getLastServer();
        int x = width / 2 + 2;
        int y = y1 + rowHeight;
        if (server != null) {
            String[] parts = server.split(":");
            String host = parts[parts.length - 2];
            int port = Integer.parseInt(parts[parts.length - 1]);
            if (port == 25565) {
                lastServer = host;
            } else {
                lastServer = host + ":" + port;
            }
            lastServer = MinecraftFactory.getVars().shortenToWidth(lastServer, 88);
            button = new GuiButton(99, x, y, 98, 20, lastServer) {
                @Override
                public void onClick(double mouseX, double mouseY) {
                    String server = MinecraftFactory.getClassProxyCallback().getLastServer();
                    if (server == null)
                        return;
                    String[] parts = server.split(":");
                    String host = parts[parts.length - 2];
                    int port = Integer.parseInt(parts[parts.length - 1]);
                    MinecraftFactory.getVars().joinServer(host, port);
                }
            };
        } else {
            button = new GuiButton(98, x, y, 98, 20, MinecraftFactory.getClassProxyCallback().translate("menu.no_last_server")) {
            };
        }

        buttons.add(button);

        for (GuiButton b : buttons) {
            int id = b.id;
            if (id == 2) {
                b.width = 98;
            }
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void drawScreen(int mouseX, int mouseY, float pTicks, CallbackInfo _ci) {
        The5zigMod.getVars().drawString(ChatColor.GOLD + "The 5zig Mod v" + Version.VERSION, 2, 2);
    }
}
