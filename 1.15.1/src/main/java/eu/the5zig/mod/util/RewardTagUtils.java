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

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import eu.the5zig.mod.The5zigMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Team;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class RewardTagUtils {

    private static final float SCALE_FACTOR = 0.5F;

	public static boolean shouldRender(PlayerEntity player) {
        if(player == Minecraft.getInstance().player && !The5zigMod.getConfig().getBool("showOwnNameTag")) return false;
        if(player.isInvisibleToPlayer(Minecraft.getInstance().player)) return false;
        if(player.func_226273_bm_()) return false; // Sneaking
        if(player.isBeingRidden()) return false;

        int renderDist = 4096; // 64^2
        if(player.getDistanceSq(Minecraft.getInstance().player) > renderDist) return false;

        return shouldRenderTeam(player);

    }

	public static void render(PlayerRenderer renderer, String str, PlayerEntity pl, double x, double y, double z) {
        FontRenderer fontRenderer = renderer.getFontRendererFromRenderManager();
        float f = 1.6F;
        float f1 = 0.016666668F * f * SCALE_FACTOR;
        GlStateManager.func_227626_N_();
        GlStateManager.func_227688_c_((float) x + 0.0F, (float) y + pl.getHeight() + 0.35F, (float) z);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.func_227689_c_((float) -renderer.getRenderManager().info.getProjectedView().y, 0.0F, 1.0F, 0.0F);
        GlStateManager.func_227689_c_((float) renderer.getRenderManager().info.getProjectedView().x, 1.0F, 0.0F, 0.0F);
        GlStateManager.func_227672_b_(-f1, -f1, f1);
        GlStateManager.func_227722_g_();
        GlStateManager.func_227667_a_(false);
        GlStateManager.func_227731_j_();
        GlStateManager.func_227740_m_();
        GlStateManager.func_227706_d_(770, 771, 1, 0);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder worldrenderer = tessellator.getBuffer();
        int i = 0;

        int j = fontRenderer.getStringWidth(str) / 2;
        GlStateManager.func_227621_I_();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        color(worldrenderer.func_225582_a_((double) (-j - 1), (double) (-1 + i), 0.0D),0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        color(worldrenderer.func_225582_a_((double) (-j - 1), (double) (8 + i), 0.0D), 0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        color(worldrenderer.func_225582_a_((double) (j + 1), (double) (8 + i), 0.0D), 0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        color(worldrenderer.func_225582_a_((double) (j + 1), (double) (-1 + i), 0.0D),0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        tessellator.draw();
        GlStateManager.func_227619_H_();

        makeStr(fontRenderer, str);

        GlStateManager.func_227716_f_();
        GlStateManager.func_227737_l_();
        GlStateManager.func_227702_d_(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.func_227627_O_();
    }

    private static void makeStr(FontRenderer renderer, String str) {
        int x = -renderer.getStringWidth(str) / 2;
        renderTxt(renderer, str, x);
    }

	private static void renderTxt(FontRenderer renderer, String toRender, int x) {
        GlStateManager.func_227731_j_();
        GlStateManager.func_227667_a_(true);
        GlStateManager.func_227731_j_();
        GlStateManager.func_227667_a_(false);

        int y = 0;
        GlStateManager.func_227702_d_(255, 255, 255, .5F);
        renderer.drawString(toRender, x, y, Color.WHITE.darker().darker().darker().darker().darker().getRGB() * 255);


        GlStateManager.func_227734_k_();
        GlStateManager.func_227667_a_(true);

        GlStateManager.func_227702_d_(1.0F, 1.0F, 1.0F, 1.0F);
        renderer.drawString(toRender, x, y, Color.WHITE.darker().getRGB());

    }

    private static boolean shouldRenderTeam(PlayerEntity player) {
        Team team = player.getTeam();
        Team team1 = Minecraft.getInstance().player.getTeam();

        if (team != null) {
            Team.Visible enumVisible = team.getNameTagVisibility();
            switch (enumVisible) {
                case ALWAYS:
                    return true;
                case NEVER:
                    return false;
                case HIDE_FOR_OTHER_TEAMS:
                    return team1 == null || team.isSameTeam(team1);
                case HIDE_FOR_OWN_TEAM:
                    return team1 == null || !team.isSameTeam(team1);
                default:
                    return true;
            }
        }
        return true;
    }

    private static IVertexBuilder color(IVertexBuilder in, float red, float green, float blue, float alpha)
    {
        return in.func_225586_a_((int)(red * 255.0F), (int)(green * 255.0F), (int)(blue * 255.0F), (int)(alpha * 255.0F));
    }
}
