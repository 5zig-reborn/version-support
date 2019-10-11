package eu.the5zig.mod.mixin;

import eu.the5zig.mod.MinecraftFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiConnecting;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.ServerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiConnecting.class)
public abstract class MixinGuiConnecting {

    @Inject(method = "<init>", at = @At("RETURN"))
    public void init(GuiScreen p_i1181_1_, Minecraft mcIn, ServerData p_i1181_3_, CallbackInfo _ci) {
        MinecraftFactory.getClassProxyCallback().setAutoreconnectServerData(p_i1181_3_);
    }
}
