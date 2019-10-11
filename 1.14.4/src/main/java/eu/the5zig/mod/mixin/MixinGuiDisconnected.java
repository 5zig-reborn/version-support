package eu.the5zig.mod.mixin;

import eu.the5zig.mod.MinecraftFactory;
import eu.the5zig.mod.The5zigMod;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisconnectedScreen.class)
public abstract class MixinGuiDisconnected extends Screen {

    @Shadow Screen field_146307_h; // Parent screen

    protected MixinGuiDisconnected(ITextComponent p_i51108_1_) {
        super(p_i51108_1_);
    }

    @Inject(method = "init", at = @At("HEAD"))
    public void initGui(CallbackInfo _ci) {
        The5zigMod.getDataManager().getAutoReconnectManager().startCountdown(this.field_146307_h);
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void draw(int mouseX, int mouseY, float partialTicks, CallbackInfo _ci) {
        MinecraftFactory.getClassProxyCallback().checkAutoreconnectCountdown(width, height);
    }
}
