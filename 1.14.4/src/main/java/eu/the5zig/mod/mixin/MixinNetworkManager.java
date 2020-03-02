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
import eu.the5zig.mod.event.PacketEvent;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetworkManager.class)
public abstract class MixinNetworkManager {
    @Inject(method = "sendPacket", at = @At("HEAD"))
    public void sendPacket(IPacket packetIn, CallbackInfo _ci) {
        The5zigMod.getListener().fireEvent(new PacketEvent(packetIn, false));
    }

    @Inject(method = "channelRead0", at = @At("HEAD"))
    public void receivePacket(ChannelHandlerContext ctx, IPacket packet, CallbackInfo _ci) {
        The5zigMod.getListener().fireEvent(new PacketEvent(packet, true));
    }
}
