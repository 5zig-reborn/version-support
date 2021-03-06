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
import eu.the5zig.mod.util.ingamemenu.MenuButtonCallback;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IngameMenuScreen.class)
public abstract class MixinGuiIngameMenu extends Screen {

    protected MixinGuiIngameMenu(ITextComponent p_i51108_1_) {
        super(p_i51108_1_);
    }

    @Inject(method = "addButtons", at = @At("TAIL"))
    public void onInit(CallbackInfo _ci) {
        if (The5zigMod.getConfig().getBool("confirmDisconnect")) {
            Button dcBtn = (Button) buttons.remove(buttons.size() - 1);
            children.remove(dcBtn);
            Button newBtn;
            addButton(newBtn = new Button(dcBtn.x, dcBtn.y, 204, 20, dcBtn.getMessage(), new MenuButtonCallback(this, dcBtn)));
            if (!this.minecraft.isIntegratedServerRunning()) {
                newBtn.setMessage(I18n.format("menu.disconnect"));
            }
        }
    }
}
