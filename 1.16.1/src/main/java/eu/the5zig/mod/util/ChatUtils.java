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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import eu.the5zig.mod.MinecraftFactory;
import eu.the5zig.util.minecraft.ChatColor;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;

import java.util.List;
import java.util.Locale;

public class ChatUtils {
    public static void highlightChatLine(MatrixStack stack, OrderedText chatComponent, int x, int y, int alpha) {
        List<String> highlightWords;
        boolean onlyWordMatch;
        String chatSearchText = MinecraftFactory.getClassProxyCallback().getChatSearchText();
        if (!Strings.isNullOrEmpty(chatSearchText)) {
            onlyWordMatch = false;
            highlightWords = ImmutableList.of(chatSearchText);
        } else {
            onlyWordMatch = true;
            highlightWords = MinecraftFactory.getClassProxyCallback().getHighlightWords();
        }
        if (highlightWords.isEmpty()) {
            return;
        }
        String text = ChatColor.stripColor(getText(chatComponent).toLowerCase(Locale.ROOT));
        int currIndex = 0;
        for (String search : highlightWords) {
            search = search.replace("%player%", MinecraftFactory.getVars().getGameProfile().getName()).toLowerCase(Locale.ROOT);
            for (int nameIndex = text.indexOf(search, currIndex), unformattedIndex = text.indexOf(search); nameIndex != -1 && unformattedIndex != -1;
                 nameIndex = text.indexOf(search, nameIndex + search.length()), unformattedIndex =
                         text.indexOf(search, unformattedIndex + search.length())) {
                if (onlyWordMatch) {
                    if (unformattedIndex > 0) {
                        char previousChar = Character.toLowerCase(text.charAt(unformattedIndex - 1));
                        if ((previousChar >= 'a' && previousChar <= 'z') || (previousChar >= '0' && previousChar <= '9')) {
                            continue;
                        }
                    }
                    if (unformattedIndex + search.length() < text.length()) {
                        char nextChar = text.charAt(unformattedIndex + search.length());
                        if ((nextChar >= 'a' && nextChar <= 'z') || (nextChar >= '0' && nextChar <= '9')) {
                            continue;
                        }
                    }
                }
                int offset = MinecraftFactory.getVars().getStringWidth(text.substring(0, nameIndex));
                int width = MinecraftFactory.getVars().getStringWidth(text.substring(nameIndex, nameIndex + search.length()));
                MatrixStacks.drawRect(stack, x + offset, y, x + offset + width, y + MinecraftFactory.getVars().getFontHeight(), MinecraftFactory.getClassProxyCallback().getHighlightWordsColor() + (Math.min(0x80, alpha) << 24));
            }
        }
    }

    public static String getText(OrderedText in) {
        CharacterVisitor visitor = new CharacterVisitor() {
            final StringBuilder sb = new StringBuilder();
            @Override
            public boolean accept(int index, Style style, int codePoint) {
                sb.appendCodePoint(codePoint);
                return true;
            }

            @Override
            public String toString() {
                return sb.toString();
            }
        };
        in.accept(visitor);
        return visitor.toString();
    }
}
