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
import eu.the5zig.mod.Version;
import eu.the5zig.mod.util.ButtonFactory;
import eu.the5zig.mod.util.MatrixStacks;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class MixinGuiMainMenu extends Screen {
    protected MixinGuiMainMenu(Text title) {
        super(title);
    }

    @Inject(method = "initWidgetsNormal", at = @At("TAIL"))
    public void addSingleplayerMultiplayerButtons(int y1, int rowHeight, CallbackInfo _ci) {
        if (!MinecraftFactory.getClassProxyCallback().isShowLastServer())
            return;
        String lastServer;
        ButtonWidget button;
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
            button = ButtonFactory.lastServer(x, y, lastServer);
        } else {
            button = (ButtonWidget)
                    MinecraftFactory.getVars().createButton(98, x, y, 98, 20,
                            MinecraftFactory.getClassProxyCallback().translate("menu.no_last_server"));
        }
        addButton(button);
        for(AbstractButtonWidget btn : buttons) {
            if(!(btn.getMessage() instanceof TranslatableText)) return;
            if("menu.multiplayer".equals(((TranslatableText) btn.getMessage()).getKey())) {
                btn.setWidth(98);
            }
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void drawScreen(MatrixStack stack, int _mouseX, int _mouseY, float _pTicks, CallbackInfo _ci) {
        MatrixStacks.hudMatrixStack = stack;
        The5zigMod.getVars().drawString(Version.getVersionDisplay(), 2, 2);
    }
}
