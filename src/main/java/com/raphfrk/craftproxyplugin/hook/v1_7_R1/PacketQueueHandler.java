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

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.v1_7_R1.Packet;
import net.minecraft.server.v1_7_R1.PacketPlayOutCustomPayload;
import net.minecraft.server.v1_7_R1.PacketPlayOutMapChunk;
import net.minecraft.server.v1_7_R1.PacketPlayOutMapChunkBulk;
import net.minecraft.util.io.netty.channel.ChannelHandlerContext;
import net.minecraft.util.io.netty.channel.ChannelOutboundHandlerAdapter;
import net.minecraft.util.io.netty.channel.ChannelPromise;

import com.raphfrk.craftproxyplugin.hook.CacheManager;
import com.raphfrk.craftproxyplugin.hook.CompressionManager;
import com.raphfrk.craftproxyplugin.message.InitMessage;
import com.raphfrk.craftproxyplugin.message.MessageManager;
import com.raphfrk.craftproxyplugin.reflect.ReflectManager;

public class PacketQueueHandler extends ChannelOutboundHandlerAdapter {
	private final CacheManager manager;
	private final String type;
	private final long startTime;
	private boolean normal = false;
	private boolean caching = false;
	private final List<Packet> queue;

	public PacketQueueHandler(CacheManager manager, String type) {
		this.startTime = System.currentTimeMillis();
		this.queue = new ArrayList<Packet>();
		this.type = type;
		this.manager = manager;
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object packet, ChannelPromise promise) throws Exception {
		Packet p = (Packet) packet;
		if (p instanceof PacketPlayOutCustomPayload) {
			PacketPlayOutCustomPayload custom = (PacketPlayOutCustomPayload) p;
			String tag = (String) ReflectManager.getField(custom, "tag");
			byte[] data = (byte[]) ReflectManager.getField(custom, "data");
			if (MessageManager.getChannelName().equals(tag)) {
				if (data.length == InitMessage.getSubCommandRaw().length() * 2 + 2) {
					if (!(normal || caching)) {
						dumpQueue(ctx, promise);
					} else {
						normal = false;
					}
					caching = true;
					super.write(ctx, p, promise);
					return;
				}
			}
		}
		
		if (!(normal || caching) && System.currentTimeMillis() > startTime + 200) {
			dumpQueue(ctx, promise);
			normal = true;
			super.write(ctx, p, promise);
			return;
		}
	
		if (normal) {
			super.write(ctx, p, promise);
			return;
		} else if (caching) {
			if (p instanceof PacketPlayOutMapChunk) {
				PacketPlayOutMapChunk mapChunk = (PacketPlayOutMapChunk) p;
				byte[] oldBuffer = (byte[]) ReflectManager.getField(mapChunk, "buffer");
				byte[] newBuffer = manager.process(oldBuffer);
				
				if (newBuffer == null) {
					return;
				}

				byte[] deflated = new byte[newBuffer.length + 100];

				int size = CompressionManager.deflate(newBuffer, deflated);

				ReflectManager.setField(mapChunk, "e", deflated);
				ReflectManager.setField(mapChunk, "size", size);
			} else if (p instanceof PacketPlayOutMapChunkBulk) {
				PacketPlayOutMapChunkBulk mapChunkBulk = (PacketPlayOutMapChunkBulk) p;
				byte[][] oldBuffers = (byte[][]) ReflectManager.getField(mapChunkBulk, "inflatedBuffers");
				
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
				ReflectManager.setField(mapChunkBulk, "buildBuffer", newBuffer);
			}
			super.write(ctx, p, promise);
			return;
		} else {
			queue.add(p);
			return;
		}
	}
	
	private void dumpQueue(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
		for (Packet pp : queue) {
			super.write(ctx, pp, promise);
		}
		queue.clear();
	}
}
