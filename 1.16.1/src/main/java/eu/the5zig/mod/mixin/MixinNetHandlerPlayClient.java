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
import eu.the5zig.mod.util.ChatComponentBuilder;
import eu.the5zig.mod.util.TabList;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.MessageType;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinNetHandlerPlayClient {
    @Inject(method = "onCustomPayload", at = @At(value = "RETURN", ordinal = 0))
    public void handleCustomPayload(CustomPayloadS2CPacket packet, CallbackInfo _ci) {
        The5zigMod.getListener().handlePluginMessage(packet.getChannel().toString(),
                packet.getData().asReadOnly());
    }

    @Inject(method = "onPlayerListHeader", at = @At("TAIL"))
    public void handlePlayerListHeaderFooter(PlayerListHeaderS2CPacket packet, CallbackInfo _ci) {
        TabList tabList = new TabList(packet.getHeader().getString(), packet.getFooter().getString());
        The5zigMod.getListener().onPlayerListHeaderFooter(tabList);
    }

    @Inject(method = "onGameMessage", at = @At(value = "INVOKE",
            target = "net/minecraft/client/gui/hud/InGameHud.addChatMessage(Lnet/minecraft/network/MessageType;Lnet/minecraft/text/Text;Ljava/util/UUID;)V",
            shift = At.Shift.BEFORE), cancellable = true)
    public void handleChat(GameMessageS2CPacket packet, CallbackInfo ci) {
        MessageType type = packet.getLocation();
        Text comp = packet.getMessage();
        String formatted = comp.getString().replace("§r", "");
        boolean b;
        if(type == MessageType.GAME_INFO) {
            b = The5zigMod.getListener().onActionBar(formatted);
        }
        else {
            b = The5zigMod.getListener().onServerChat(formatted, ChatComponentBuilder.toInterface(comp), comp);
        }
        if(b) ci.cancel();
    }

    @Inject(method = "onHeldItemChange", at = @At("HEAD"))
    public void handleSetSlot(HeldItemChangeS2CPacket packet, CallbackInfo _ci) {
        try {
            //ZIG116 ItemStack stack = (ItemStack) Class.forName("WrappedItemStack").getConstructor(net.minecraft.item.ItemStack.class)
            //        .newInstance(packet.());
            The5zigMod.getListener().onInventorySetSlot(packet.getSlot(), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Inject(method = "onTitle", at = @At(value = "RETURN", ordinal = 0))
    public void titleBar(TitleS2CPacket packet, CallbackInfo _ci) {
        The5zigMod.getListener().onActionBar(packet.getText().getString());
    }

    @Inject(method = "onTitle", at = @At(value = "RETURN", ordinal = 1))
    public void titleClear(TitleS2CPacket _packet, CallbackInfo _ci) {
        The5zigMod.getListener().onTitle(null, null);
    }

    @Inject(method = "onTitle", at = @At(value = "RETURN", ordinal = 2))
    public void title(TitleS2CPacket packet, CallbackInfo _ci) {
        String text = packet.getText() == null ? "" : packet.getText().getString();
        switch(packet.getAction()) {
            case TITLE:
                The5zigMod.getListener().onTitle(text, null);
                break;
            case SUBTITLE:
                The5zigMod.getListener().onTitle(null, text);
                break;
        }
    }
}
