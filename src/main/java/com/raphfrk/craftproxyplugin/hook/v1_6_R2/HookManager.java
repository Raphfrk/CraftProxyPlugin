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
package com.raphfrk.craftproxyplugin.hook.v1_6_R2;

import java.util.Collections;
import java.util.List;

import net.minecraft.server.v1_6_R2.NetworkManager;
import net.minecraft.server.v1_6_R2.Packet;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.raphfrk.craftproxyplugin.CraftProxyPlugin;
import com.raphfrk.craftproxyplugin.hook.CacheManager;
import com.raphfrk.craftproxyplugin.reflect.ReflectManager;

public class HookManager extends com.raphfrk.craftproxyplugin.hook.HookManager {

	@Override
	public String getVersion() {
		return "v1_6_R2";
	}

	@SuppressWarnings("unchecked")
	@Override
	public void hookQueue(CraftProxyPlugin plugin, Player player) {
		try {
			CraftPlayer p = (CraftPlayer) player;
			NetworkManager nm = (NetworkManager) p.getHandle().playerConnection.networkManager;
			Object sync = ReflectManager.getField(nm, "h");
			synchronized (sync) {
				List<?> highPriorityQueue = (List<?>) ReflectManager.getField(nm, "highPriorityQueue");
				//List<?> lowPriorityQueue = (List<?>) ReflectManager.getField(nm, "lowPriorityQueue");
				CacheManager manager = new CacheManager(plugin, player);
				PacketQueueWrapper queue = new PacketQueueWrapper((List<Packet>) highPriorityQueue, manager, "high");
				ReflectManager.setField(nm, "highPriorityQueue", Collections.synchronizedList(queue));
				//ReflectManager.setField(nm, "lowPriorityQueue", Collections.synchronizedList(new PacketQueueWrapper((List<Packet>) lowPriorityQueue, manager, "low")));
			}
		} catch (Exception e) {
			Bukkit.getLogger().info("Exception thrown " + e);
		}
	}

}
