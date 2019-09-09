import com.mojang.blaze3d.platform.GlStateManager;
import eu.the5zig.mod.MinecraftFactory;
import eu.the5zig.mod.gui.IOverlay;
import eu.the5zig.util.minecraft.ChatColor;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.gui.toasts.ToastGui;

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

	private static class Toast implements IToast {

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
		public IToast.Visibility draw(ToastGui var1, long l) {
			if (updateTime) {
				startedTime = l;
				updateTime = false;
			}

			MinecraftFactory.getVars().bindTexture(TEXTURE_TOASTS);
			GlStateManager.scalef(1.0F, 1.0F, 1.0F);
			int overlayTexture = MinecraftFactory.getClassProxyCallback().getOverlayTexture();
			var1.blit(0, 0, 0, overlayTexture * 32, 160, 32);
			MinecraftFactory.getVars().drawString(title, overlayTexture == 2 ? 16 : 6, 7);
			MinecraftFactory.getVars().drawString(subTitle, overlayTexture == 2 ? 16 : 6, 18);

			return l - startedTime < 5000L ? Visibility.SHOW : Visibility.HIDE;
		}

		@Override
		public Object getType() {
			return uniqueReference == null ? NO_TOKEN : uniqueReference;
		}

		public static void create(String title, String subTitle, Object uniqueReference) {
			ToastGui toastFactory = ((Variables) MinecraftFactory.getVars()).getMinecraft().getToastGui();

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