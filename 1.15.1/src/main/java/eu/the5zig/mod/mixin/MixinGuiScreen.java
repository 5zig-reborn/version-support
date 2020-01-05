package eu.the5zig.mod.mixin;

import eu.the5zig.mod.The5zigMod;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class MixinGuiScreen {
    @Inject(method = "renderBackground(I)V", at = @At("HEAD"), cancellable = true)
    public void drawWorldBackground(int tint, CallbackInfo ci) {
        if(!The5zigMod.shouldDrawWorldBackground()) ci.cancel();
    }
}
