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
import eu.the5zig.mod.gui.ingame.ItemStack;
import eu.the5zig.mod.util.TabList;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.*;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public abstract class MixinNetHandlerPlayClient {

    @Inject(method = "handleCustomPayload", at = @At(value = "RETURN", ordinal = 0))
    public void handleCustomPayload(S3FPacketCustomPayload packet, CallbackInfo _ci) {
        The5zigMod.getListener().handlePluginMessage(packet.getChannelName(),
                packet.getBufferData());
    }

    @Inject(method = "handlePlayerListHeaderFooter", at = @At("TAIL"))
    public void handlePlayerListHeaderFooter(S47PacketPlayerListHeaderFooter packet, CallbackInfo _ci) {
        TabList tabList = new TabList(packet.getHeader().getFormattedText(), packet.getFooter().getFormattedText());
        The5zigMod.getListener().onPlayerListHeaderFooter(tabList);
    }

    @Inject(method = "handleChat", at = @At(value = "CONSTANT", args = "intValue=2", shift = At.Shift.BEFORE), cancellable = true)
    public void handleChat(S02PacketChat packet, CallbackInfo ci) {
        byte type = packet.getType();
        IChatComponent comp = packet.getChatComponent();
        String formatted = comp.getFormattedText().replace("§r", "");
        boolean b;
        if(type == 2) {
            b = The5zigMod.getListener().onActionBar(formatted);
        }
        else {
            b = The5zigMod.getListener().onServerChat(formatted, comp);
        }

        if(b)
            ci.cancel();
    }

    @Inject(method = "handleSetSlot", at = @At("HEAD"))
    public void handleSetSlot(S2FPacketSetSlot packet, CallbackInfo _ci) {
        try {
            ItemStack stack = (ItemStack) Class.forName("WrappedItemStack").getConstructor(net.minecraft.item.ItemStack.class)
                    .newInstance(packet.func_149174_e());
            The5zigMod.getListener().onInventorySetSlot(packet.func_149173_d(), stack);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Inject(method = "handleTitle", at = @At(value = "RETURN", ordinal = 0))
    public void titleBar(S45PacketTitle packet, CallbackInfo _ci) {
        The5zigMod.getListener().onActionBar(packet.getMessage().getFormattedText());
    }


    @Inject(method = "handleTitle", at = @At(value = "RETURN", ordinal = 1))
    public void titleClear(S45PacketTitle packet, CallbackInfo _ci) {
        The5zigMod.getListener().onTitle(null, null);
    }

    @Inject(method = "handleTitle", at = @At(value = "RETURN", ordinal = 2))
    public void title(S45PacketTitle packet, CallbackInfo _ci) {
        String text = packet.getMessage().getFormattedText();

        switch(packet.getType()) {
            case TITLE:
                The5zigMod.getListener().onTitle(text, null);
                break;
            case SUBTITLE:
                The5zigMod.getListener().onTitle(null, text);
                break;
        }
    }
}
