package eu.the5zig.mod.mixin;

import eu.the5zig.mod.The5zigMod;

import net.minecraft.server.integrated.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IntegratedServer.class)
public abstract class MixinIntegratedServer {

    @Inject(method = "initiateShutdown", at = @At("RETURN"))
    public void initiateShutdown(CallbackInfo _ci) {
        The5zigMod.getListener().onSingleplayerLeave();
    }
}
