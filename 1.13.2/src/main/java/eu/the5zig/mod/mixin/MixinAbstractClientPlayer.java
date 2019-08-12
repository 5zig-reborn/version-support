/*
 * Copyright (c) 2019 5zig Reborn
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
import eu.the5zig.mod.The5zigMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(AbstractClientPlayer.class)
public abstract class MixinAbstractClientPlayer {

    @Shadow
    private NetworkPlayerInfo playerInfo;

    private static final UUID SPRINT_MODIFIER_UUID = UUID.fromString("662A6B8D-DA3E-4C1C-8813-96EA6097278D");

    @Inject(method = "<init>", at = @At("RETURN"))
    public void init(World w, GameProfile profile, CallbackInfo ci) {
        MinecraftFactory.getVars().getResourceManager().loadPlayerTextures(profile);
    }

    @Inject(method = "getLocationCape", at = @At("HEAD"), cancellable = true)
    public void getCapeLocation(CallbackInfoReturnable<ResourceLocation> ci) {
        Object loc = MinecraftFactory.getVars().getResourceManager().getCapeLocation(playerInfo);
        if(loc != null) {
            ci.setReturnValue((ResourceLocation)loc);
        }
    }

    @Inject(method = "getFovModifier", at = @At("HEAD"), cancellable = true)
    public void getFovModifier(CallbackInfoReturnable<Float> ci) {
        if(The5zigMod.getConfig().getBool("staticFov")) {
            ci.setReturnValue(getCustomFOVModifier(Minecraft.getInstance().player));
        }
    }

    private static float getCustomFOVModifier(Object object) {
        EntityPlayer playerInstance = (EntityPlayer) object;

        float modifier = 1.0F;
        if (playerInstance.abilities.isFlying) {
            // is Flying
            modifier *= 1.1F;
        }
        double staticSpeed = 0.10000000149011612D;
        if(playerInstance.isSprinting())
            staticSpeed = 0.13000000312924387D;

        modifier *= (staticSpeed / playerInstance.abilities.getWalkSpeed() + 1.0F) / 2.0F;
        if (playerInstance.abilities.getWalkSpeed() == 0.0F || Float.isNaN(modifier) || Float.isInfinite(modifier)) {
            modifier = 1.0F;
        }

        if (playerInstance.getActiveItemStack().getItem() == Items.BOW) {
            // is using bow
            int itemInUseDuration = playerInstance.getItemInUseCount();
            float itemInUseDurationSeconds = (float) itemInUseDuration / 20.0F;
            if (itemInUseDurationSeconds > 1.0F) {
                itemInUseDurationSeconds = 1.0F;
            } else {
                itemInUseDurationSeconds *= itemInUseDurationSeconds;
            }

            modifier *= 1.0F - itemInUseDurationSeconds * 0.15F;
        }

        return modifier;
    }

}
