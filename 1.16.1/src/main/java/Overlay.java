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

import com.mojang.blaze3d.platform.GlStateManager;
import eu.the5zig.mod.MinecraftFactory;
import eu.the5zig.mod.gui.IOverlay;
import eu.the5zig.util.minecraft.ChatColor;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;

import java.util.List;

public class Overlay implements IOverlay {

	Overlay() {
	}

	public static void updateOverlayCount(int count) {
	}

	public static void renderAll() {
	}

	@Override
	public void displayMessage(String title, String subtitle) {
		Toast.create(ChatColor.YELLOW + title, subtitle, null);
	}

	@Override
	public void displayMessage(String title, String subtitle, Object uniqueReference) {
		Toast.create(ChatColor.YELLOW + title, subtitle, uniqueReference);
	}

	@Override
	public void displayMessage(String message) {
		displayMessage("The 5zig Mod", message);
	}

	@Override
	public void displayMessage(String message, Object uniqueReference) {
		displayMessage("The 5zig Mod", message, uniqueReference);
	}

	@Override
	public void displayMessageAndSplit(String message) {
		displayMessageAndSplit(message, null);
	}

	@Override
	public void displayMessageAndSplit(String message, Object uniqueReference) {
		List<String> split = MinecraftFactory.getVars().splitStringToWidth(message, MinecraftFactory.getClassProxyCallback().getOverlayTexture() == 2 ? 142 : 150);
		String title = null;
		String subTitle = null;
		for (int i = 0; i < split.size(); i++) {
			if (i == 0)
				title = split.get(0);
			if (i == 1)
				subTitle = split.get(1);
		}
		displayMessage(title, subTitle, uniqueReference);
	}

	private static class Toast implements net.minecraft.client.toast.Toast {

		private boolean updateTime = true;
		private long startedTime;
		private final Object uniqueReference;
		private String title;
		private String subTitle;

		private Toast(Object uniqueReference, String title, String subTitle) {
			this.uniqueReference = uniqueReference;
			this.title = title;
			this.subTitle = subTitle;
		}

		private void update(String title, String subTitle) {
			this.title = title;
			this.subTitle = subTitle;
			this.startedTime = System.currentTimeMillis();
			this.updateTime = true;
		}

		@Override
		public Visibility draw(MatrixStack matrixStack, ToastManager toastManager, long l) {
			if (updateTime) {
				startedTime = l;
				updateTime = false;
			}

			MinecraftFactory.getVars().bindTexture(TOASTS_TEX);
			GlStateManager.scalef(1.0F, 1.0F, 1.0F); // scaleF
			int overlayTexture = MinecraftFactory.getClassProxyCallback().getOverlayTexture();
			//toastManager.?(0, 0, 0, overlayTexture * 32, 160, 32);
			//ZIG116 toastManager.draw(matrixStack);
			MinecraftFactory.getVars().drawString(title, overlayTexture == 2 ? 16 : 6, 7);
			MinecraftFactory.getVars().drawString(subTitle, overlayTexture == 2 ? 16 : 6, 18);

			return l - startedTime < 5000L ? Visibility.SHOW : Visibility.HIDE;
		}

		@Override
		public Object getType() {
			return uniqueReference == null ? TYPE : uniqueReference;
		}

		public static void create(String title, String subTitle, Object uniqueReference) {
			ToastManager toastFactory = ((Variables) MinecraftFactory.getVars()).getMinecraft().getToastManager();

			if (uniqueReference == null) {
				toastFactory.add(new Toast(null, title, subTitle));
			} else {
				Toast toast = toastFactory.getToast(Toast.class, uniqueReference);
				if (toast == null) {
					toastFactory.add(new Toast(uniqueReference, title, subTitle));
				} else {
					toast.update(title, subTitle);
				}
			}
		}
	}

}