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

import eu.the5zig.mod.MinecraftFactory;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.TextureTickListener;
import net.minecraft.resource.ResourceManager;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SimpleTickingTexture extends AbstractTexture implements TextureTickListener {

	private final ResourceLocation resourceLocation;

	private boolean textureUploaded;
	private List<BufferedImage> bufferedImages;
	private List<ResourceLocation> frames;
	private int index;

	public SimpleTickingTexture(ResourceLocation resourceLocation) {
		this.resourceLocation = resourceLocation;
	}

	private void checkTextureUploaded() {
		if (!this.textureUploaded) {
			if (this.frames != null) {
				List<BufferedImage> bufferedImages1 = this.bufferedImages;
				for (int i = 0; i < bufferedImages1.size(); i++) {
					BufferedImage bufferedImage = bufferedImages1.get(i);
					ResourceLocation resourceLocation = new ResourceLocation(this.resourceLocation.callGetResourceDomain(), this.resourceLocation.callGetResourcePath() + i);
					AbstractTexture texture = ((Variables) MinecraftFactory.getVars()).getTextureManager().getTexture(resourceLocation);
					SimpleTexture simpleTexture;
					if (texture instanceof SimpleTexture) {
						simpleTexture = (SimpleTexture) texture;
					} else {
						simpleTexture = new SimpleTexture();
						((Variables) MinecraftFactory.getVars()).getTextureManager().registerTexture(resourceLocation, simpleTexture);
					}
					simpleTexture.setBufferedImage(bufferedImage);
					simpleTexture.checkTextureUploaded();
					frames.add(resourceLocation);
				}
				this.textureUploaded = true;
			}
		}
	}

	public void setBufferedImage(BufferedImage bufferedImage) {
		if (bufferedImage == null) {
			frames = null;
			index = 0;
			textureUploaded = false;
		} else {
			int parts = bufferedImage.getHeight() / (bufferedImage.getWidth() / 2);
			int partHeight = bufferedImage.getHeight() / parts;
			bufferedImages = new ArrayList<>(parts);
			frames = new ArrayList<>(parts);
			for (int part = 0; part < parts; part++) {
				bufferedImages.add(bufferedImage.getSubimage(0, part * partHeight, bufferedImage.getWidth(), partHeight));
			}
		}
	}

	@Override
	public void tick() {
		checkTextureUploaded();
		if (textureUploaded && frames.size() > 1) {
			index = (index + 1) % (frames.size());
		}
	}

	public ResourceLocation getCurrentResource() {
		if (!textureUploaded || index < 0 || index >= frames.size()) {
			return null;
		}
		return frames.get(index);
	}

	@Override
	public void load(ResourceManager resourceManager) throws IOException {

	}
}
