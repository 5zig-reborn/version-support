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

package eu.the5zig.mod.util;

import eu.the5zig.mod.The5zigMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.math.Matrix4f;

public class RewardTagUtils {

    private static final float SCALE_FACTOR = 0.5F;

    public static boolean shouldRender(PlayerEntity player) {
        if(player == MinecraftClient.getInstance().player && !The5zigMod.getConfig().getBool("showOwnNameTag")) return false;
        if(player.isInvisibleTo(MinecraftClient.getInstance().player)) return false;
        if(player.hasPlayerRider()) return false;
        int renderDist = 4096; // 64^2
        if(player.squaredDistanceTo(MinecraftClient.getInstance().player) > renderDist) return false;
        return shouldRenderTeam(player);
    }

    public static void render(MatrixStack stack, PlayerEntityRenderer renderer, String str, PlayerEntity pl, VertexConsumerProvider vertexConsumers, int light) {
        float scale = 0.016666668F * 1.6F * SCALE_FACTOR;
        stack.push();
        stack.translate(0.0, pl.getHeight() + 0.65, 0.0);
        stack.multiply(renderer.getRenderManager().getRotation());
        stack.scale(-scale, -scale, scale);
        Matrix4f matrix4f = stack.peek().getModel();
        float opacity = MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25f);
        int color = (int)(opacity * 255.0f) << 24;
        TextRenderer textRenderer = renderer.getFontRenderer();
        float h = -textRenderer.getWidth(str) / 2;
        boolean sneaky = pl.isSneaky();
        textRenderer.draw(str, h, 0f, 0x20FFFFFF, false, matrix4f, vertexConsumers, !sneaky, color, light);
        if (!sneaky) {
            textRenderer.draw(str, h, 0f, -1, false, matrix4f, vertexConsumers, false, 0, light);
        }
        stack.pop();
    }

    private static boolean shouldRenderTeam(PlayerEntity player) {
        AbstractTeam team = player.getScoreboardTeam();
        AbstractTeam team1 = MinecraftClient.getInstance().player.getScoreboardTeam();

        if (team != null) {
            Team.VisibilityRule enumVisible = team.getNameTagVisibilityRule();
            switch (enumVisible) {
                case NEVER:
                    return false;
                case HIDE_FOR_OTHER_TEAMS:
                    return team1 == null || team.isEqual(team1);
                case HIDE_FOR_OWN_TEAM:
                    return team1 == null || !team.isEqual(team1);
                default:
                    return true;
            }
        }
        return true;
    }
}
