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

import net.minecraft.server.v1_6_R3.Packet;
import net.minecraft.server.v1_6_R3.Packet250CustomPayload;
import net.minecraft.server.v1_6_R3.Packet51MapChunk;
import net.minecraft.server.v1_6_R3.Packet56MapChunkBulk;

import com.raphfrk.craftproxycommon.compression.CompressionManager;
import com.raphfrk.craftproxycommon.message.InitMessage;
import com.raphfrk.craftproxycommon.message.MessageManager;
import com.raphfrk.craftproxyplugin.hook.CacheManager;
import com.raphfrk.craftproxyplugin.reflect.ReflectManager;

public class PacketQueueWrapper extends ArrayList<Packet> {
	
	private static final long serialVersionUID = 1L;
	
	private final CacheManager manager;
	private final String type;
	private final long startTime;
	private boolean normal = false;
	private boolean caching = false;
	private final List<Packet> queue;

	public PacketQueueWrapper(List<Packet> queue, CacheManager manager, String type) {
		this.startTime = System.currentTimeMillis();
		this.queue = new ArrayList<Packet>(queue);
		this.type = type;
		this.manager = manager;
	}
	
	@Override
	public boolean add(Packet p) {
		if (p.n() == 0xFA) {
			Packet250CustomPayload custom = (Packet250CustomPayload) p;
			if (MessageManager.getChannelName().equals(custom.tag)) {
				if (custom.length == InitMessage.getSubCommandRaw().length() * 2 + 2) {
					if (!(normal || caching)) {
						dumpQueue();
					} else {
						normal = false;
					}
					caching = true;
					return super.add(p);
				}
			}
		}
		
		if (!(normal || caching) && System.currentTimeMillis() > startTime + 200) {
			dumpQueue();
			normal = true;
			return super.add(p);
		}
	
		if (normal) {
			return super.add(p);
		} else if (caching) {
			if (p instanceof Packet51MapChunk) {
				Packet51MapChunk packet = (Packet51MapChunk) p;
				byte[] oldBuffer = (byte[]) ReflectManager.getField(packet, "inflatedBuffer", "field_73596_g");
				byte[] newBuffer = manager.process(oldBuffer);
				
				if (newBuffer == null) {
					return false;
				}

				byte[] deflated = new byte[newBuffer.length + 100];

				int size = CompressionManager.deflate(newBuffer, deflated);

				ReflectManager.setField(packet, deflated, "buffer", "field_73595_f");
				ReflectManager.setField(packet, size, "size", "field_73602_h");
			} else if (p instanceof Packet56MapChunkBulk) {
				Packet56MapChunkBulk packet = (Packet56MapChunkBulk) p;
				byte[][] oldBuffers = (byte[][]) ReflectManager.getField(packet, "inflatedBuffers",  "field_73584_f");
				byte[][] newBuffers = new byte[oldBuffers.length][];
				int newSize = 0;
				for (int i = 0; i < newBuffers.length; i++) {
					newBuffers[i] = manager.process(oldBuffers[i]);
					newSize += newBuffers[i].length;
				}
				
				byte[] buildBuffer = (byte[]) ReflectManager.getField(packet, "buildBuffer");
				if (buildBuffer == null || buildBuffer.length == 0) {
					// Forge
					System.arraycopy(newBuffers, 0, oldBuffers, 0, newBuffers.length);
				} else {
					byte[] newBuffer = new byte[newSize];
					int pos = 0;
					for (int i = 0; i < newBuffers.length; i++) {
						System.arraycopy(newBuffers[i], 0, newBuffer, pos, newBuffers[i].length);
						pos += newBuffers[i].length;
					}
					ReflectManager.setField(packet, newBuffer, "buildBuffer");
				}
			}
			return super.add(p);
		} else {
			return queue.add(p);
		}
	}
	
	private void dumpQueue() {
		for (Packet pp : queue) {
			super.add(pp);
		}
		queue.clear();
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
}
