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

public class PacketQueueWrapper extends ArrayList<Packet> {
	
	private static final long serialVersionUID = 1L;
	
	private final String type;
	private final long startTime;
	private boolean active;
	private final List<Packet> queue;;

	public PacketQueueWrapper(List<Packet> queue, String type) {
		this.startTime = System.currentTimeMillis();
		this.queue = new ArrayList<Packet>(queue);
		this.type = type;
	}
	
	@Override
	public boolean add(Packet p) {
		if (!active && System.currentTimeMillis() > startTime + 200) {
			Bukkit.getLogger().info("Activating " + type);
			active = true;
			for (Packet pp : queue) {
				add(pp);
			}
		}
		if (active) {
			Bukkit.getLogger().info(type + ") Main Packet " + p.getClass().getSimpleName() + " " + (System.currentTimeMillis() - startTime));
			return super.add(p);
		} else {
			Bukkit.getLogger().info(type + ") Login Packet " + p.getClass().getSimpleName() + " " + (System.currentTimeMillis() - startTime));
			return queue.add(p);
		}
	}
}
