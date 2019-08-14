package eu.the5zig.mod.mixin;

import eu.the5zig.mod.The5zigMod;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiScreen.class)
public abstract class MixinCommonGuiScreen {
    @Inject(method = "drawWorldBackground", at = @At("HEAD"), cancellable = true)
    public void drawWorldBackground(int tint, CallbackInfo ci) {
        if(!The5zigMod.shouldDrawWorldBackground()) ci.cancel();
    }
}
