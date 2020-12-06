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

import eu.the5zig.mod.util.IServerData;
import eu.the5zig.mod.util.IServerPinger;
import eu.the5zig.util.minecraft.ChatColor;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.LiteralText;

import java.net.UnknownHostException;

public class ServerPinger implements IServerPinger {
	private final net.minecraft.client.network.MultiplayerServerListPinger handle = new net.minecraft.client.network.MultiplayerServerListPinger();

	@Override
	public IServerData createServerData(String serverIP) {
		return new ServerData(serverIP);
	}

	@Override
	public void ping(IServerData iServerData) {
		ServerInfo serverData = (ServerInfo) iServerData;
		try {
			handle.add(serverData, () -> {});
		} catch (UnknownHostException e) {
			serverData.ping = -1;
			serverData.label = new LiteralText(ChatColor.DARK_RED + "Can't resolve hostname");
		} catch (Exception e) {
			serverData.ping = -1;
			serverData.label = new LiteralText(ChatColor.DARK_RED + "Can't connect to server");
		}
	}

	@Override
	public void callPingPendingNetworks() {
		handle.tick();
	}

	@Override
	public void callClearPendingNetworks() {
		handle.cancel();
	}
}
