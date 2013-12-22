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
package com.raphfrk.craftproxyplugin.hook;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

import org.bukkit.entity.Player;

import com.raphfrk.craftproxyplugin.message.MessageManager;

public class CacheManager {
	
	private static WeakHashMap<Player, WeakReference<CacheManager>> map = new WeakHashMap<Player, WeakReference<CacheManager>>();
	
	public static CacheManager getCacheManager(Player player) {
		synchronized (map) {
			WeakReference<CacheManager> ref = map.get(player);
			if (ref == null) {
				throw new IllegalStateException("Cache manager player map should alway retain mapping");
			}
			CacheManager manager = ref.get();
			if (manager == null) {
				throw new IllegalStateException("Cache manager player map should alway retain mapping");
			}
			return manager;
		}
	}
	
	private final Player player;
	private PacketQueue queue;
	
	public CacheManager(Player player) {
		this.player = player;
		synchronized (map) {
			map.put(player, new WeakReference<CacheManager>(this));
		}
	}
	
	/**
	 * Processes the given byte array
	 * 
	 * @param data
	 * @return
	 */
	public byte[] process(byte[] data) {
		DataOutputStream dos = new DataOutputStream(new ByteArrayOutputStream(data.length + 8));
		/*try {
			dos.writeInt(MessageManager.getMagicInt());
			dos.writeInt(data.length);
			dos.write(data);
		} catch (IOException e) {
		}*/

		for (int i = 0; i < data.length; i++) {
			data[i] = (byte) (~data[i]);
		}
		
		return data;
	}
	
	public void setQueue(PacketQueue queue) {
		this.queue = queue;
	}
	
	public void activateCaching() {
		if (queue == null) {
			throw new IllegalStateException("Init packet received before queue hook replacement");
		}
		queue.setCaching();
	}
	
}
