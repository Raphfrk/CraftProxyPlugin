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

import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.raphfrk.craftproxycommon.hash.Hash;
import com.raphfrk.craftproxycommon.hash.section.SectionMap;
import com.raphfrk.craftproxycommon.hash.section.SectionMapException;
import com.raphfrk.craftproxycommon.hash.tree.HashTreeSet;
import com.raphfrk.craftproxycommon.message.MessageManager;
import com.raphfrk.craftproxyplugin.CraftProxyPlugin;

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
	
	private final SectionMap sectionMap = new SectionMap();
	private final Player player;
	private final CraftProxyPlugin plugin;
	private final TLongSet sentSet = new TLongHashSet();
	private final HashTreeSet hashSet = new HashTreeSet();
	private short sectionId = 0;
	
	public CacheManager(CraftProxyPlugin plugin, Player player) {
		this.player = player;
		this.plugin = plugin;
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
		int hashCount = Hash.getHashCount(data);

		int encodedLength = 4; // magic
		encodedLength += 2; // sectionId
		encodedLength += 4; // length
		encodedLength += 1; // hash count
		encodedLength += 9 * hashCount; // hashes
		encodedLength += 8; // sectionhash;
		
		byte[] encoded = new byte[encodedLength];
		ByteBuffer buf = ByteBuffer.wrap(encoded);
		
		short sectionId = this.sectionId++;

		buf.putInt(MessageManager.getMagicInt());
		buf.putShort(sectionId);
		buf.putInt(data.length);
		buf.put((byte) hashCount);
		
		int pos = 0;
		for (int i = 0; i < hashCount; i++) {
			int hashLength = Math.min(Hash.getHashLength(), data.length - pos);
			Hash h = new Hash(data, pos, hashLength);
			try {
				sectionMap.add(sectionId, h);
			} catch (SectionMapException e) {
				kickPlayerAsync(player, "ChunkCache: " + e.getMessage());
				return null;
			}
			if (!hashSet.writeHash(buf, h.getHash())) {
				kickPlayerAsync(player, "ChunkCache: Unable to encode reduced hash");
				return null;
			}
			pos += Hash.getHashLength();
		}
		
		buf.putLong(Hash.hash(data));

		byte[] clipped = new byte[buf.position()];
		buf.flip();
		buf.get(clipped);
		return clipped;
	}
	
	public CraftProxyPlugin getPlugin() {
		return plugin;
	}
	
	public Hash getHash(long hash) throws SectionMapException {
		return sectionMap.get(hash);
	}
	
	public boolean setSent(Hash hash) {
		return sentSet.add(hash.getHash());
	}
	
	public void ackSection(short id) {
		try {
			sectionMap.ackSection(id);
		} catch (SectionMapException e) {
			player.kickPlayer("ChunkCache: " + e.getMessage());
		}
	}
	
	private void kickPlayerAsync(final Player p, final String message) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				p.kickPlayer(message);
			}	
		});
	}
	
}
