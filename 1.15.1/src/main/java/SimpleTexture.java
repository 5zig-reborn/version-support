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

import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.TextureUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class SimpleTexture extends net.minecraft.client.renderer.texture.SimpleTexture {

	private boolean textureUploaded;
	private BufferedImage bufferedImage;

	public SimpleTexture() {
		super(null);
	}

	void checkTextureUploaded() {
		if (!this.textureUploaded) {
			if (this.bufferedImage != null) {
				try {
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					ImageIO.write(bufferedImage, "png", os);
					InputStream in = new ByteArrayInputStream(os.toByteArray());
					NativeImage var1 = NativeImage.read(in);
					TextureUtil.func_225680_a_(super.getGlTextureId(), var1.getWidth(), var1.getHeight());
					var1.uploadTextureSub(0, 0, 0, false);
					this.textureUploaded = true;
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	public void setBufferedImage(BufferedImage bufferedImage) {
		this.textureUploaded = false;
		this.bufferedImage = bufferedImage;
	}

	public BufferedImage getBufferedImage() {
		return bufferedImage;
	}

	@Override
	public int getGlTextureId() {
		checkTextureUploaded();
		return super.getGlTextureId();
	}

	@Override
	public void loadTexture(net.minecraft.resources.IResourceManager resourceManager) throws IOException {
	}
}
