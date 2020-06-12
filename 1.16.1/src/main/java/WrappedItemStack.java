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
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.text.Text;
import net.minecraft.util.registry.Registry;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
		return item.getCount();
	}

	@Override
	public int getMaxDurability() {
		return item.getMaxDamage();
	}

	@Override
	public int getCurrentDurability() {
		return item.getDamage();
	}

	@Override
	public String getKey() {
		return getResourceKey(item);
	}

	@Override
	public String getDisplayName() {
		return item.getName().getString();
	}

	@Override
	public List<String> getLore() {
		List<Text> comps =
				item.getTooltip(((Variables) MinecraftFactory.getVars()).getPlayer(), TooltipContext.Default.NORMAL);
		return comps.stream().map(Text::getString).collect(Collectors.toList());
	}

	@Override
	public int getHealAmount() {
		return item.getItem().isFood() ? item.getItem().getFoodComponent().getHunger() : 0;
	}

	@Override
	public float getSaturationModifier() {
		return item.getItem().isFood() ? item.getItem().getFoodComponent().getSaturationModifier() : 0;
	}

	@Override
	public void render(int x, int y, boolean withGenericAttributes) {
		if (item == null)
			return;
		((Variables) MinecraftFactory.getVars()).renderItem(item, x, y);

		if (withGenericAttributes) {
			Multimap<EntityAttribute, EntityAttributeModifier> multimap = item.getAttributeModifiers(EquipmentSlot.MAINHAND);
			for (Map.Entry<EntityAttribute, EntityAttributeModifier> entry : multimap.entries()) {
				EntityAttributeModifier attribute = entry.getValue();
				double value = attribute.getValue();
				if (nameDisplayModifier.equals(attribute.getId())) {
					value = value + (double) EnchantmentHelper.getAttackDamage(item, EntityGroup.DEFAULT);
				}
				if (entry.getKey() == EntityAttributes.GENERIC_ATTACK_DAMAGE || entry.getKey() == EntityAttributes.GENERIC_ARMOR) {
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
		return "minecraft:" + Registry.ITEM.getKey(item.getItem()).get().toString();
	}
}
