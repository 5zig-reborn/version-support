package eu.the5zig.mod.mixin;

import eu.the5zig.mod.The5zigMod;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinCommonMinecraft {
    @Inject(method = "clickMouse", at = @At("HEAD"))
    public void leftClickCPS(CallbackInfo _ci) {
        The5zigMod.getDataManager().getCpsManager().getLeftClickCounter().incrementCount();
    }

    @Inject(method = "rightClickMouse", at = @At("HEAD"))
    public void rightClickCPS(CallbackInfo _ci) {
        The5zigMod.getDataManager().getCpsManager().getRightClickCounter().incrementCount();
    }
}
