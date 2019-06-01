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

import eu.the5zig.util.minecraft.ChatColor;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by 5zig. All rights reserved © 2015
 * <p>
 * Utility class for ChatComponent serialization.
 */
public class ChatComponentBuilder {

	private static final Map<ChatColor, EnumChatFormatting> TRANSLATE = new HashMap<ChatColor, EnumChatFormatting>();
	private static final Pattern url = Pattern.compile("^(?:(https?)://)?([-\\w_\\.]{2,}\\.[a-z]{2,4})(/\\S*)?$");

	static {
		for (int i = 0; i < EnumChatFormatting.values().length; i++) {
			TRANSLATE.put(ChatColor.values()[i], EnumChatFormatting.values()[i]);
		}
	}

	public static IChatComponent fromLegacyText(String message) {
		ChatComponentText base = new ChatComponentText("");
		StringBuilder builder = new StringBuilder();
		ChatComponentText currentComponent = new ChatComponentText("");
		ChatStyle currentStyle = new ChatStyle();
		currentComponent.setChatStyle(currentStyle);
		Matcher matcher = url.matcher(message);

		for (int i = 0; i < message.length(); i++) {
			char c = message.charAt(i);

			if (c == ChatColor.COLOR_CHAR) {
				i++;
				c = message.charAt(i);
				if (c >= 'A' && c <= 'Z') {
					c += 32;
				}

				ChatColor format = ChatColor.getByChar(c);
				if (format == null) {
					continue;
				}

				if (builder.length() > 0) {
					ChatComponentText old = currentComponent;
					currentComponent = old.createCopy();
					currentStyle = currentComponent.getChatStyle();
					old.appendText(builder.toString());
					builder = new StringBuilder();
					base.appendSibling(old);
				}

				switch (format) {
					case BOLD:
						currentStyle.setBold(true);
						break;
					case ITALIC:
						currentStyle.setItalic(true);
						break;
					case STRIKETHROUGH:
						currentStyle.setStrikethrough(true);
						break;
					case UNDERLINE:
						currentStyle.setUnderlined(true);
						break;
					case MAGIC:
						currentStyle.setObfuscated(true);
						break;
					case RESET:
						format = ChatColor.WHITE;
					default:
						currentComponent = new ChatComponentText("");
						currentStyle = new ChatStyle();
						currentComponent.setChatStyle(currentStyle);
						currentStyle.setColor(TRANSLATE.get(format));
						break;
				}
				continue;
			}
			int pos = message.indexOf(' ', i);
			if (pos == -1) {
				pos = message.length();
			}
			if (matcher.region(i, pos).find()) {
				if (builder.length() > 0) {
					ChatComponentText old = currentComponent;
					currentComponent = old.createCopy();
					currentStyle = currentComponent.getChatStyle();
					old.appendText(builder.toString());
					builder = new StringBuilder();
					base.appendSibling(old);
				}

				ChatComponentText old = currentComponent;
				currentComponent = old.createCopy();
				currentStyle = currentComponent.getChatStyle();
				String urlStr = message.substring(i, pos);
				if (!urlStr.startsWith("http"))
					urlStr = "http://" + urlStr;
				currentComponent.appendText(urlStr);
				ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.OPEN_URL, urlStr);
				currentStyle.setChatClickEvent(clickEvent);
				base.appendSibling(currentComponent);
				i += pos - i - 1;
				currentComponent = old;
				currentStyle = currentComponent.getChatStyle();
				continue;
			}
			builder.append(c);
		}
		if (builder.length() > 0) {
			currentComponent.appendText(builder.toString());
			base.appendSibling(currentComponent);
		}


		return base;
	}

}
