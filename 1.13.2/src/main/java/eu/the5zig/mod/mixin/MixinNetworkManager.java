package eu.the5zig.mod.mixin;

import eu.the5zig.mod.The5zigMod;
import eu.the5zig.mod.event.PacketEvent;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetworkManager.class)
public abstract class MixinNetworkManager {
    @Inject(method = "sendPacket", at = @At("HEAD"))
    public void sendPacket(Packet packetIn, CallbackInfo _ci) {
        The5zigMod.getListener().fireEvent(new PacketEvent(packetIn, false));
    }

    @Inject(method = "channelRead0", at = @At("HEAD"))
    public void receivePacket(ChannelHandlerContext ctx, Packet packet, CallbackInfo _ci) {
        The5zigMod.getListener().fireEvent(new PacketEvent(packet, true));
    }
}
