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

import com.mojang.authlib.GameProfile;
import eu.the5zig.mod.MinecraftFactory;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class MixinAbstractClientPlayer {

    @Shadow
    protected abstract PlayerListEntry getPlayerListEntry();

    @Inject(method = "<init>", at = @At("RETURN"))
    public void init(ClientWorld _w, GameProfile profile, CallbackInfo _ci) {
        MinecraftFactory.getVars().getResourceManager().loadPlayerTextures(profile);
    }

    @Inject(method = "getCapeTexture", at = @At("HEAD"), cancellable = true)
    public void getCapeLocation(CallbackInfoReturnable<Identifier> ci) {
        PlayerListEntry entry = getPlayerListEntry();
        if (entry == null) return;
        Object loc = MinecraftFactory.getVars().getResourceManager().getCapeLocation(entry);
        if(loc != null) {
            ci.setReturnValue((Identifier) loc);
        }
    }
}
