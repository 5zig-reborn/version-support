package eu.the5zig.mod.mixin;

import net.minecraft.client.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameSettings.class)
public interface MixinGameSettings {
    @Accessor
    void setKeyBindings(KeyBinding[] keys);
}
