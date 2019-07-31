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

import eu.the5zig.mod.The5zigMod;
import eu.the5zig.mod.api.rewards.RewardsCache;
import eu.the5zig.mod.util.RewardTagUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderPlayer.class)
public abstract class MixinRenderPlayer {

    @Inject(method = "doRender", at = @At("TAIL"))
    public void doRender(AbstractClientPlayer entity, double x, double y, double z, float yaw, float pTicks, CallbackInfo _ci) {
        if(!RewardTagUtils.shouldRender(entity)) return;
        String rewardString = RewardsCache.getRewardString(entity.getUniqueID().toString());
        if(rewardString == null) return;

        // Transform
        rewardString = rewardString.replace("§s", The5zigMod.getRenderer().getPrefix());

        double offset = 0.3;
        Scoreboard scoreboard = entity.getWorldScoreboard();
        ScoreObjective scoreObjective = scoreboard.getObjectiveInDisplaySlot(2);

        if (scoreObjective != null && entity.getDistanceSqToEntity(Minecraft.getMinecraft().thePlayer) < 10 * 10) {
            offset *= 2;
        }

        RenderPlayer renderer = (RenderPlayer)
                Minecraft.getMinecraft().getRenderManager().<AbstractClientPlayer>getEntityRenderObject(entity);

        RewardTagUtils.render(renderer, rewardString, entity, x, y + offset, z);
    }
}
