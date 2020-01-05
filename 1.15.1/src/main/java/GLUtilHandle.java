/*
 * Original: Copyright (c) 2015-2019 5zig [MIT]
 * Current: Copyright (c) 2019 5zig Reborn [GPLv3+]
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

import com.mojang.blaze3d.platform.GlStateManager;
import eu.the5zig.mod.util.IGLUtil;

public class GLUtilHandle implements IGLUtil {

	@Override
	public void enableBlend() {
		GlStateManager.func_227740_m_();
	}

	@Override
	public void disableBlend() {
		GlStateManager.func_227737_l_();
	}

	@Override
	public void scale(float x, float y, float z) {
		GlStateManager.func_227672_b_(x, y, z);
	}

	@Override
	public void translate(float x, float y, float z) {
		GlStateManager.func_227688_c_(x, y, z);
	}

	@Override
	public void color(float r, float g, float b, float a) {
		GlStateManager.func_227702_d_(r, g, b, a);
	}

	@Override
	public void color(float r, float g, float b) {
		color(r, g, b, 1.0f);
	}

	@Override
	public void pushMatrix() {
		GlStateManager.func_227626_N_();
	}

	@Override
	public void popMatrix() {
		GlStateManager.func_227627_O_();
	}

	@Override
	public void matrixMode(int mode) {
		GlStateManager.func_227768_x_(mode);
	}

	@Override
	public void loadIdentity() {
		GlStateManager.func_227625_M_();
	}

	@Override
	public void clear(int i) {
		GlStateManager.func_227658_a_(i, false);
	}

	@Override
	public void disableDepth() {
		GlStateManager.func_227731_j_();
	}

	@Override
	public void enableDepth() {
		GlStateManager.func_227734_k_();
	}

	@Override
	public void depthMask(boolean b) {
		GlStateManager.func_227667_a_(b);
	}

	@Override
	public void disableLighting() {
		GlStateManager.func_227722_g_();
	}

	@Override
	public void enableLighting() {
		GlStateManager.func_227716_f_();
	}

	@Override
	public void disableFog() {
		GlStateManager.func_227769_y_();
	}

	@Override
	public void tryBlendFuncSeparate(int i, int i1, int i2, int i3) {
		GlStateManager.func_227706_d_(i, i1, i2, i3);
	}

	@Override
	public void disableAlpha() {
		GlStateManager.func_227700_d_();
	}
}
