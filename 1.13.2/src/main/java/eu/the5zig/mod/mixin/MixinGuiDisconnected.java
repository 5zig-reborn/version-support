package eu.the5zig.mod.mixin;

import eu.the5zig.mod.MinecraftFactory;
import eu.the5zig.mod.The5zigMod;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiDisconnected.class)
public abstract class MixinGuiDisconnected extends GuiScreen {

    @Shadow GuiScreen parentScreen;

    @Inject(method = "initGui", at = @At("HEAD"))
    public void initGui(CallbackInfo _ci) {
        The5zigMod.getDataManager().getAutoReconnectManager().startCountdown(this.parentScreen);
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void draw(int mouseX, int mouseY, float partialTicks, CallbackInfo _ci) {
        MinecraftFactory.getClassProxyCallback().checkAutoreconnectCountdown(width, height);
    }
}
