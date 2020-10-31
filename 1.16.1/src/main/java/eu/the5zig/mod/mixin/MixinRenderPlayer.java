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

import eu.the5zig.mod.The5zigMod;
import eu.the5zig.mod.api.rewards.RewardsCache;
import eu.the5zig.mod.util.RewardTagUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class MixinRenderPlayer {
    @Inject(method = "renderLabelIfPresent", at = @At(value = "INVOKE", target = "net/minecraft/client/util/math/MatrixStack.pop()V", shift = At.Shift.BEFORE))
    public void onRenderLabel(AbstractClientPlayerEntity entity, Text _text, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light,
                              CallbackInfo _ci) {
        if(!RewardTagUtils.shouldRender(entity)) return;
        String rewardString = RewardsCache.getRewardString(entity.getUuid().toString());
        if(rewardString == null) return;
        rewardString = rewardString.replace("§s", The5zigMod.getRenderer().getPrefix());
        PlayerEntityRenderer renderer = (PlayerEntityRenderer) MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(entity);
        RewardTagUtils.render(matrixStack, renderer, rewardString, entity, vertexConsumerProvider, light);
    }
}
