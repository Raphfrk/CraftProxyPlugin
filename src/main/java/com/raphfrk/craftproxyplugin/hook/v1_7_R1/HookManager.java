/*
 * This file is part of CraftProxyPlugin.
 *
 * Copyright (c) 2013-2014, Raphfrk <http://raphfrk.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.raphfrk.craftproxyplugin.hook.v1_7_R1;

import net.minecraft.server.v1_7_R1.NetworkManager;
import net.minecraft.util.io.netty.channel.Channel;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.raphfrk.craftproxyplugin.CraftProxyPlugin;
import com.raphfrk.craftproxyplugin.hook.CacheManager;
import com.raphfrk.craftproxyplugin.reflect.ReflectManager;

public class HookManager extends com.raphfrk.craftproxyplugin.hook.HookManager {

	@Override
	public String getVersion() {
		return "v1_7_R1";
	}

	@Override
	public void hookQueue(CraftProxyPlugin plugin, Player player) {
		try {
			CraftPlayer p = (CraftPlayer) player;
			NetworkManager nm = (NetworkManager) p.getHandle().playerConnection.networkManager;
			Channel channel = (Channel) ReflectManager.getField(nm, "k");
			CacheManager manager = new CacheManager(plugin, player);
			channel.pipeline().addBefore("packet_handler", "chunk_cache", new PacketQueueHandler(manager, "high"));
		} catch (Exception e) {
			Bukkit.getLogger().info("Exception thrown " + e);
		}
	}
	
}
