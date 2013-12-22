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
package com.raphfrk.craftproxyplugin.hook.v1_6_R3;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;

import net.minecraft.server.v1_6_R3.Packet;
import net.minecraft.server.v1_6_R3.Packet51MapChunk;
import net.minecraft.server.v1_6_R3.Packet56MapChunkBulk;

import com.raphfrk.craftproxyplugin.hook.CacheManager;
import com.raphfrk.craftproxyplugin.hook.CompressionManager;
import com.raphfrk.craftproxyplugin.hook.PacketQueue;
import com.raphfrk.craftproxyplugin.reflect.ReflectManager;

public class PacketQueueWrapper extends ArrayList<Packet> implements PacketQueue {
	
	private static final long serialVersionUID = 1L;
	
	private final CacheManager manager;
	private final String type;
	private final long startTime;
	private boolean normal = false;
	private boolean caching = false;
	private final List<Packet> queue;;

	public PacketQueueWrapper(List<Packet> queue, CacheManager manager, String type) {
		this.startTime = System.currentTimeMillis();
		this.queue = new ArrayList<Packet>(queue);
		this.type = type;
		this.manager = manager;
	}
	
	@Override
	public boolean add(Packet p) {
		if (!(normal || caching) && System.currentTimeMillis() > startTime + 200) {
			normal = true;
			for (Packet pp : queue) {
				add(pp);
			}
		}
		if (normal) {
			return super.add(p);
		} else if (caching) {
			if (p instanceof Packet51MapChunk) {
				Packet51MapChunk packet = (Packet51MapChunk) p;
				byte[] oldBuffer = (byte[]) ReflectManager.getField(packet, "inflatedBuffer");
				byte[] newBuffer = manager.process(oldBuffer);

				byte[] deflated = new byte[newBuffer.length + 100];

				int size = CompressionManager.deflate(newBuffer, deflated);

				ReflectManager.setField(packet, "buffer", deflated);
				ReflectManager.setField(packet, "size", size);
			} else if (p instanceof Packet56MapChunkBulk) {
				Packet56MapChunkBulk packet = (Packet56MapChunkBulk) p;
				byte[][] oldBuffers = (byte[][]) ReflectManager.getField(packet, "inflatedBuffers");
				
				byte[][] newBuffers = new byte[oldBuffers.length][];
				int newSize = 0;
				for (int i = 0; i < newBuffers.length; i++) {
					newBuffers[i] = manager.process(oldBuffers[i]);
					newSize += newBuffers[i].length;
				}
				byte[] newBuffer = new byte[newSize];
				int pos = 0;
				for (int i = 0; i < newBuffers.length; i++) {
					System.arraycopy(newBuffers[i], 0, newBuffer, pos, newBuffers[i].length);
					pos += newBuffers[i].length;
				}
				ReflectManager.setField(packet, "buildBuffer", newBuffer);
			}
			return super.add(p);
		} else {
			return queue.add(p);
		}
	}
	
	public String hexToString(byte[] data) {
		StringBuilder sb = new StringBuilder();
		for (byte b : data) {
			int i = b & 0xFF;
			if (i < 0x10) {
				sb.append("0");
			}
			sb.append(Integer.toHexString(i));
		}
		return sb.toString();
	}

	public void setCaching() {
		synchronized (this) {
			caching = true;
		}
	}

}
