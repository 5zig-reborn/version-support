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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import eu.the5zig.mod.MinecraftFactory;
import eu.the5zig.mod.asm.Transformer;
import eu.the5zig.mod.gui.ingame.resource.CapeResource;
import eu.the5zig.mod.gui.ingame.resource.IResourceManager;
import eu.the5zig.mod.gui.ingame.resource.ItemModelResource;
import eu.the5zig.mod.gui.ingame.resource.PlayerResource;
import eu.the5zig.mod.util.GLUtil;
import eu.the5zig.util.Utils;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.*;

public class ResourceManager implements IResourceManager {

	private static final int PLAYER_RESOURCE_VERSION = 1;

	private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("5zig Texture Downloader #%d").setDaemon(true)
			.build());
	private static final String BASE_URL = "http://textures.5zig.net/";
	private static final Gson gson = new Gson();

	private final Object guiCameraTransform;
	private final FaceBakery faceBakery = new FaceBakery();

	private final GameProfile playerProfile;
	private PlayerResource ownPlayerResource;
	private final Cache<UUID, PlayerResource> playerResources = CacheBuilder.newBuilder().expireAfterAccess(3, TimeUnit.MINUTES).build();
	private final Cache<Integer, String> moduleIds = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build();

	public ResourceManager(GameProfile playerProfile) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
		guiCameraTransform = Thread.currentThread().getContextClassLoader().loadClass(ItemCameraTransforms.class.getName() + (Transformer.FORGE ? "$TransformType" : "$b")).getDeclaredField(
				Transformer.FORGE ? "GUI" : "e").get(null);
		this.playerProfile = playerProfile;
	}

	public void loadPlayerTextures(final GameProfile gameProfile) {
		if (gameProfile == null || gameProfile.getId() == null) {
			return;
		}
		PlayerResource playerResource;
		if (playerProfile.getName().equals(gameProfile.getName())) {
			if (ownPlayerResource != null) {
				playerResource = ownPlayerResource;
			} else {
				playerResource = ownPlayerResource = loadPlayerResource(playerProfile);
			}
		} else {
			playerResource = playerResources.getIfPresent(gameProfile.getId());
			if (playerResource != null) {
				MinecraftFactory.getClassProxyCallback().getLogger().debug("Loaded player resource textures from cache for player " + gameProfile.getName());
			} else {
				playerResources.put(gameProfile.getId(), loadPlayerResource(gameProfile));
			}
		}
		if (playerResource == null) {
			return;
		}

		if (playerResource.getItemModelResources() != null) {
			for (ItemModelResource itemModelResource : playerResource.getItemModelResources()) {
				ResourceLocation resourceLocation = (ResourceLocation) itemModelResource.getResourceLocation();
				if (((Variables) MinecraftFactory.getVars()).getTextureManager().getTexture(resourceLocation) == null) {
					((Variables) MinecraftFactory.getVars()).getTextureManager().loadTexture(resourceLocation,
							(SimpleTexture) itemModelResource.getSimpleTexture());
				}
			}
		}
	}

	private PlayerResource loadPlayerResource(final GameProfile gameProfile) {
		final MinecraftProfileTexture minecraftProfileTexture = new MinecraftProfileTexture(
				BASE_URL + "textures/" + PLAYER_RESOURCE_VERSION + "/" + Utils.getUUIDWithoutDashes(gameProfile.getId()), new HashMap<String, String>());
		final PlayerResource playerResource = new PlayerResource();

		final ResourceLocation capeLocation = new ResourceLocation("the5zigmod", "capes/" + gameProfile.getId());
		final SimpleTickingTexture capeTexture;
		ITextureObject texture = ((Variables) MinecraftFactory.getVars()).getTextureManager().getTexture(capeLocation);
		if (texture instanceof SimpleTickingTexture) {
			capeTexture = (SimpleTickingTexture) texture;
			capeTexture.setBufferedImage(null);
		} else {
			capeTexture = new SimpleTickingTexture(capeLocation);
			((Variables) MinecraftFactory.getVars()).getTextureManager().loadTexture(capeLocation, capeTexture);
		}

		EXECUTOR_SERVICE.execute(new Runnable() {
			@Override
			public void run() {
				MinecraftFactory.getClassProxyCallback().getLogger().debug("Loading player resource textures from {} for player {}", minecraftProfileTexture.getUrl(), gameProfile.getName());

				HttpURLConnection connection = null;
				BufferedReader reader = null;
				try {
					connection = (HttpURLConnection) (new URL(minecraftProfileTexture.getUrl())).openConnection(MinecraftFactory.getVars().getProxy());
					connection.setDoInput(true);
					connection.setDoOutput(false);
					connection.connect();

					if (connection.getResponseCode() != 200) {
						return;
					}
					reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					String line = reader.readLine();

					TextureData data = gson.fromJson(line, TextureData.class);
					if (data.animatedCape != null && !data.animatedCape.isEmpty()) {
						try {
							BufferedImage cape = ImageIO.read(new ByteArrayInputStream(Base64.decodeBase64(data.animatedCape)));
							capeTexture.setBufferedImage(cape);

							playerResource.setCapeResource(new CapeResource(capeLocation, capeTexture));
						} catch (Exception e) {
							MinecraftFactory.getClassProxyCallback().getLogger().error("Could not parse cape for " + gameProfile.getId(), e);
						}
					} else if (data.cape != null && !data.cape.isEmpty()) {
						try {
							BufferedImage cape = ImageIO.read(new ByteArrayInputStream(Base64.decodeBase64(data.cape)));
							capeTexture.setBufferedImage(cape);

							playerResource.setCapeResource(new CapeResource(capeLocation, capeTexture));
						} catch (Exception e) {
							MinecraftFactory.getClassProxyCallback().getLogger().error("Could not parse cape for " + gameProfile.getId(), e);
						}
					}
					if (data.models != null) {
						playerResource.setItemModelResources(Lists.<ItemModelResource>newArrayList());
						List<TextureData.Model> models = data.models;
						for (TextureData.Model model : models) {
							if (StringUtils.isEmpty(model.itemName) || StringUtils.isEmpty(model.texture)) {
								continue;
							}
							try {
								Item item = Item.REGISTRY.getObject(new ResourceLocation(model.itemName));
								if (item != null) {
									final ResourceLocation modelLocation = new ResourceLocation(
											"item-models/" + gameProfile.getId() + "/" + model.itemName + (StringUtils.isNotEmpty(model.render) ? "/" + model.render.toLowerCase(
													Locale.ROOT) : "") + ".png");
									final SimpleTexture modelTexture = new SimpleTexture();
									if (((Variables) MinecraftFactory.getVars()).getTextureManager().getTexture(modelLocation) == null) {
										((Variables) MinecraftFactory.getVars()).getTextureManager().loadTexture(modelLocation, modelTexture);
									}

									BufferedImage modelImage = ImageIO.read(new ByteArrayInputStream(Base64.decodeBase64(model.texture)));
									modelTexture.setBufferedImage(modelImage);

									String modelData = model.modelId == null || model.modelId == 0 ? model.model : getModelIdData(model.modelId);
									ItemModelResource.Render render = null;
									if (StringUtils.isNotEmpty(model.render)) {
										try {
											render = ItemModelResource.Render.valueOf(model.render);
										} catch (IllegalArgumentException e) {
											MinecraftFactory.getClassProxyCallback().getLogger().warn("Could not parse render type for model with item " + model.itemName + "!", e);
										}
									}
									if (StringUtils.isNotEmpty(modelData)) {
										String decoded = new String(Base64.decodeBase64(modelData), Charsets.UTF_8);
										playerResource.getItemModelResources().add(new ItemModelResource(modelLocation, item, render, createModel(decoded), modelTexture));
									}
								}
							} catch (Exception e) {
								MinecraftFactory.getClassProxyCallback().getLogger().error("Could not parse item model for " + gameProfile.getId(), e);
							}
						}
					}
				} catch (Exception e) {
					MinecraftFactory.getClassProxyCallback().getLogger().error("Couldn\'t download http texture", e);
				} finally {
					IOUtils.closeQuietly(reader);
					if (connection != null) {
						connection.disconnect();
					}
				}
			}
		});

		return playerResource;
	}

	private String getModelIdData(final Integer modelId) {
		synchronized (moduleIds) {
			try {
				return moduleIds.get(modelId, new Callable<String>() {
					@Override
					public String call() throws Exception {
						HttpURLConnection connection = null;
						BufferedReader reader = null;
						try {
							connection = (HttpURLConnection) new URL(BASE_URL + "models/" + PLAYER_RESOURCE_VERSION + "/" + modelId).openConnection();
							if (connection.getResponseCode() != 200) {
								return null;
							}
							reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
							return reader.readLine();
						} finally {
							IOUtils.closeQuietly(reader);
							if (connection != null) {
								connection.disconnect();
							}
						}
					}
				});
			} catch (ExecutionException e) {
				MinecraftFactory.getClassProxyCallback().getLogger().warn("Could not load model data with id " + modelId, e);
				return null;
			}
		}
	}

	@Override
	public void updateOwnPlayerTextures() {
		ownPlayerResource = null;
		loadPlayerTextures(MinecraftFactory.getVars().getGameProfile());
	}

	@Override
	public Object getOwnCapeLocation() {
		return getCapeLocation(ownPlayerResource);
	}

	@Override
	public void cleanupTextures() {
		MinecraftFactory.getClassProxyCallback().getLogger().debug("Cleaning up player textures...");
		for (PlayerResource playerResource : playerResources.asMap().values()) {
			if (playerResource.getCapeResource() != null) {
				((SimpleTickingTexture) playerResource.getCapeResource().getSimpleTexture()).setBufferedImage(null);
			}
		}
		playerResources.invalidateAll();
		ownPlayerResource = null;
	}

	@Override
	public Object getCapeLocation(Object player) {
		GameProfile profile = ((AbstractClientPlayer) player).getGameProfile();
		if (playerProfile.getName().equals(profile.getName())) {
			return getCapeLocation(ownPlayerResource);
		} else {
			return getCapeLocation(playerResources.getIfPresent(profile.getId()));
		}
	}

	private Object getCapeLocation(PlayerResource playerResource) {
		if (playerResource != null && playerResource.getCapeResource() != null) {
			CapeResource capeResource = playerResource.getCapeResource();
			return ((SimpleTickingTexture) capeResource.getSimpleTexture()).getCurrentResource();
		}
		return null;
	}

	private IBakedModel createModel(String model) {
		ModelBlock modelBlock = ModelBlock.deserialize(model);

		return bakeModel(modelBlock);
	}

	private IBakedModel bakeModel(ModelBlock modelBlock) {
		SimpleBakedModel.Builder bakedModelBuilder = (new SimpleBakedModel.Builder(modelBlock, ItemOverrideList.NONE)).setTexture(TextureAtlasSprite.MISSING_NO);

		for (BlockPart blockPart : modelBlock.getElements()) {
			for (EnumFacing face : blockPart.mapFaces.keySet()) {
				BlockPartFace blockPartFace = blockPart.mapFaces.get(face);
				if (blockPartFace.blockFaceUV == null) {
					bakedModelBuilder.addGeneralQuad(faceBakery.a(blockPart.positionFrom, blockPart.positionTo,
							blockPartFace, face, ModelRotation.X0_Y0, blockPart.partRotation, false, blockPart.shade));
				} else {
					bakedModelBuilder.addFaceQuad(ModelRotation.X0_Y0.rotateFace(blockPartFace.cullFace),
							faceBakery.a(blockPart.positionFrom, blockPart.positionTo, blockPartFace, face,
							ModelRotation.X0_Y0, blockPart.partRotation, false, blockPart.shade));
				}
			}
		}

		return bakedModelBuilder.makeBakedModel();
	}

	private boolean shouldRender(ItemModelResource itemModelResource, EntityPlayer entityPlayer, ItemStack itemStack) {
		if (itemStack.getItem() == itemModelResource.getItem()) {
			if (itemStack.getItem() == Items.BOW) {
				// bow
				int useCount = itemStack.getMaxItemUseDuration() - entityPlayer.getItemInUseCount();
				if (useCount >= 18) {
					if (itemModelResource.getRender() == ItemModelResource.Render.BOW_PULLING_2) {
						return true;
					}
				} else if (useCount > 13) {
					if (itemModelResource.getRender() == ItemModelResource.Render.BOW_PULLING_1) {
						return true;
					}
				} else if (useCount > 0) {
					if (itemModelResource.getRender() == ItemModelResource.Render.BOW_PULLING_0) {
						return true;
					}
				}
			} else if (itemStack.getItem() == Items.FISHING_ROD && entityPlayer.fishEntity != null) {
				// fishing rod
				if (itemModelResource.getRender() == ItemModelResource.Render.FISHING_ROD_CAST) {
					return true;
				}
			} else {
				if (itemModelResource.getRender() == null) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean renderInPersonMode(Object instance, Object itemStackObject, Object entityPlayerObject, Object cameraTransformTypeObject) {
		if (!MinecraftFactory.getClassProxyCallback().isRenderCustomModels()) {
			return false;
		}
		ItemStack itemStack = (ItemStack) itemStackObject;
		if (!(entityPlayerObject instanceof EntityPlayer)) {
			return false;
		}
		EntityPlayer entityPlayer = (EntityPlayer) entityPlayerObject;
		ItemCameraTransforms.TransformType cameraTransformType = (ItemCameraTransforms.TransformType) cameraTransformTypeObject;
		GameProfile profile = entityPlayer.getGameProfile();
		PlayerResource playerResource = playerProfile.getName().equals(profile.getName()) ? ownPlayerResource : playerResources.getIfPresent(profile.getId());
		if (playerResource == null || playerResource.getItemModelResources() == null) {
			return false;
		}
		for (ItemModelResource itemModelResource : playerResource.getItemModelResources()) {
			if (shouldRender(itemModelResource, entityPlayer, itemStack)) {
				render(instance, itemStack, itemModelResource, cameraTransformType);
				return true;
			}
		}

		return false;
	}

	private void render(Object instance, ItemStack itemStack, ItemModelResource itemModel, ItemCameraTransforms.TransformType transformType) {
		MinecraftFactory.getVars().bindTexture(itemModel.getResourceLocation());
		((Variables) MinecraftFactory.getVars()).getTextureManager().getTexture((ResourceLocation) itemModel.getResourceLocation())
			.setBlurMipmap(false, false);
		GLUtil.scale(2, 2, 2);
		GLUtil.color(1, 1, 1, 1);
		GlStateManager.enableRescaleNormal();
		GlStateManager.alphaFunc(516, 0.1F);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.pushMatrix();
		ItemCameraTransforms itemCameraTransforms = ((IBakedModel) itemModel.getBakedModel()).getItemCameraTransforms();
		itemCameraTransforms.applyTransform(transformType);
		if (a(itemCameraTransforms.getTransform(transformType))) {
			GlStateManager.cullFace(GlStateManager.CullFace.FRONT);
		}

		((RenderItem) instance).renderItem(itemStack, (IBakedModel) itemModel.getBakedModel());
		GlStateManager.cullFace(GlStateManager.CullFace.BACK);
		GlStateManager.popMatrix();
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableBlend();
		MinecraftFactory.getVars().bindTexture(itemModel.getResourceLocation());
		((Variables) MinecraftFactory.getVars()).getTextureManager().getTexture((ResourceLocation) itemModel.getResourceLocation()).restoreLastBlurMipmap();
	}

	private boolean a(ItemTransformVec3f var) {
		return var.scale.x < 0.0F ^ var.scale.y < 0.0F ^ var.scale.z < 0.0F;
	}

	public boolean renderInInventory(Object instance, Object itemStackObject, int x, int y) {
		if (!MinecraftFactory.getClassProxyCallback().isRenderCustomModels()) {
			return false;
		}
		ItemStack itemStack = (ItemStack) itemStackObject;
		if (ownPlayerResource == null || ownPlayerResource.getItemModelResources() == null) {
			return false;
		}
		for (ItemModelResource itemModel : ownPlayerResource.getItemModelResources()) {
			EntityPlayerSP player = ((Variables) MinecraftFactory.getVars()).getPlayer();
			if (shouldRender(itemModel, player, itemStack) && (itemModel.getRender() == null || player.inventory.getItemStack() == itemStack)) {
				GlStateManager.pushMatrix();
				MinecraftFactory.getVars().bindTexture(itemModel.getResourceLocation());
				((Variables) MinecraftFactory.getVars()).getTextureManager().getTexture((ResourceLocation) itemModel.getResourceLocation())
						.setBlurMipmap(false, false);
				GlStateManager.enableRescaleNormal();
				GlStateManager.enableAlpha();
				GlStateManager.alphaFunc(516, 0.1F);
				GlStateManager.enableBlend();
				GlStateManager.blendFunc(770, 771);
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				a((RenderItem) instance, x, y, ((IBakedModel) itemModel.getBakedModel()).isGui3d());
				((IBakedModel) itemModel.getBakedModel()).getItemCameraTransforms().applyTransform((ItemCameraTransforms.TransformType) guiCameraTransform);
				((RenderItem) instance).renderItem(itemStack, (IBakedModel) itemModel.getBakedModel());
				GlStateManager.disableAlpha();
				GlStateManager.disableRescaleNormal();
				GlStateManager.disableLighting();
				GlStateManager.popMatrix();
				MinecraftFactory.getVars().bindTexture(itemModel.getResourceLocation());
				((Variables) MinecraftFactory.getVars()).getTextureManager().getTexture((ResourceLocation) itemModel.getResourceLocation()).restoreLastBlurMipmap();

				return true;
			}
		}

		return false;
	}

	private void a(RenderItem instance, int var, int var1, boolean var2) {
		GlStateManager.translate((float) var, (float) var1, 100.0F + instance.zLevel);
		GlStateManager.translate(8.0F, 8.0F, 0.0F);
		GlStateManager.scale(1.0F, 1.0F, -1.0F);
		GlStateManager.scale(0.5F, 0.5F, 0.5F);
		if (var2) {
			GlStateManager.scale(40.0F, 40.0F, 40.0F);
			GlStateManager.rotate(210.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
			GlStateManager.enableLighting();
		} else {
			GlStateManager.scale(64.0F, 64.0F, 64.0F);
			GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.disableLighting();
		}
	}
}
