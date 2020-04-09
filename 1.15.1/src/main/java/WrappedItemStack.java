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
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;

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
		return item.getDisplayName().getFormattedText();
	}

	@Override
	public List<String> getLore() {
		List<ITextComponent> comps =
				item.getTooltip(((Variables) MinecraftFactory.getVars()).getPlayer(), ITooltipFlag.TooltipFlags.NORMAL);
		return comps.stream().map(ITextComponent::getUnformattedComponentText).collect(Collectors.toList());
	}

	@Override
	public int getHealAmount() {
		return item.getItem().isFood() ? item.getItem().getFood().getHealing() : 0;
	}

	@Override
	public float getSaturationModifier() {
		return item.getItem().isFood() ? item.getItem().getFood().getSaturation() : 0;
	}

	@Override
	public void render(int x, int y, boolean withGenericAttributes) {
		if (item == null)
			return;
		((Variables) MinecraftFactory.getVars()).renderItem(item, x, y);

		if (withGenericAttributes) {
			Multimap<String, AttributeModifier> multimap = item.getAttributeModifiers(EquipmentSlotType.MAINHAND);
			for (Map.Entry<String, AttributeModifier> entry : multimap.entries()) {
				AttributeModifier attribute = entry.getValue();
				double value = attribute.getAmount();
				if (nameDisplayModifier.equals(attribute.getID())) {
					value = value + (double) EnchantmentHelper.getModifierForCreature(item, CreatureAttribute.UNDEFINED);
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
		return "minecraft:" + Registry.ITEM.getKey(item.getItem()).getPath();
	}
}
