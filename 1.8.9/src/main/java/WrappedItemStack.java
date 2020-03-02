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

import com.google.common.collect.Multimap;
import eu.the5zig.mod.MinecraftFactory;
import eu.the5zig.mod.gui.ingame.ItemStack;
import eu.the5zig.mod.util.GLUtil;
import eu.the5zig.util.minecraft.ChatColor;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by 5zig.
 * All rights reserved © 2015
 */
public class WrappedItemStack implements ItemStack {

	protected static final UUID nameDisplayModifier = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");

	private net.minecraft.item.ItemStack item;

	public WrappedItemStack(net.minecraft.item.ItemStack item) {
		this.item = item;
	}

	@Override
	public int getAmount() {
		return item.stackSize;
	}

	@Override
	public int getMaxDurability() {
		return item.getMaxDamage();
	}

	@Override
	public int getCurrentDurability() {
		return item.getItemDamage();
	}

	@Override
	public String getKey() {
		return getResourceKey(item);
	}

	@Override
	public String getDisplayName() {
		return item.getDisplayName();
	}

	@Override
	public List<String> getLore() {
		return item.getTooltip(((Variables) MinecraftFactory.getVars()).getPlayer(), false);
	}

	@Override
	public int getHealAmount() {
		return item.getItem() instanceof ItemFood ? ((ItemFood) item.getItem()).getHealAmount(item) : 0;
	}

	@Override
	public float getSaturationModifier() {
		return item.getItem() instanceof ItemFood ? ((ItemFood) item.getItem()).getSaturationModifier(item) : 0;
	}

	@Override
	public void render(int x, int y, boolean withGenericAttributes) {
		if (item == null)
			return;
		((Variables) MinecraftFactory.getVars()).renderItem(item, x, y);

		if (withGenericAttributes) {
			Multimap<String, AttributeModifier> multimap = item.getAttributeModifiers();
			for (Map.Entry<String, AttributeModifier> entry : multimap.entries()) {
				AttributeModifier attribute = entry.getValue();
				double value = attribute.getAmount();
				if (nameDisplayModifier.equals(attribute.getID())) {
					value = value + (double) EnchantmentHelper.getModifierForCreature(item, EnumCreatureAttribute.UNDEFINED);
				}
				if (entry.getKey().equals("generic.attackDamage") || entry.getKey().equals("generic.armor")) {
					GLUtil.disableDepth();
					GLUtil.pushMatrix();
					GLUtil.translate(x + 8, y + 10, 1);
					GLUtil.scale(0.7f, 0.7f, 0.7f);
					MinecraftFactory.getVars().drawString(ChatColor.BLUE + "+" + Math.round(value), 0, 0);
					GLUtil.popMatrix();
					GLUtil.enableDepth();
				}
			}
		}
	}

	public static String getResourceKey(net.minecraft.item.ItemStack item) {
		net.minecraft.util.ResourceLocation resourceLocation = Item.itemRegistry.getNameForObject(item.getItem());
		return resourceLocation == null ? null : resourceLocation.toString();
	}
}
