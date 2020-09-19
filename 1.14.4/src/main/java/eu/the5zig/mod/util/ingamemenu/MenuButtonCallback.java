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

package eu.the5zig.mod.util.ingamemenu;

import eu.the5zig.mod.The5zigMod;
import eu.the5zig.mod.gui.GuiYesNo;
import eu.the5zig.mod.gui.YesNoCallback;
import net.minecraft.client.gui.widget.button.Button;

public class MenuButtonCallback implements Button.IPressable {
    private Object lastScreen;
    private Button oldButton;

    public MenuButtonCallback(Object lastScreen, Button oldButton) {
        this.lastScreen = lastScreen;
        this.oldButton = oldButton;
    }

    @Override
    public void onPress(Button button) {
        The5zigMod.getVars().displayScreen(new GuiYesNo(The5zigMod.getVars().createWrappedGui(lastScreen), new YesNoCallback() {
            @Override
            public void onDone(boolean yes) {
                if (yes) {
                    oldButton.onPress();
                }
            }
            @Override
            public String title() {
                return eu.the5zig.mod.I18n.translate("confirm_disconnect.title");
            }
        }));
    }
}
