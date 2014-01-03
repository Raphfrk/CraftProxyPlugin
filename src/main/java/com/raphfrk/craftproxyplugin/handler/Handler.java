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
package com.raphfrk.craftproxyplugin.handler;

import java.io.IOException;

import org.bukkit.entity.Player;

import com.raphfrk.craftproxycommon.message.MessageManager;
import com.raphfrk.craftproxycommon.message.SubMessage;
import com.raphfrk.craftproxyplugin.hook.CacheManager;

public abstract class Handler<M extends SubMessage> {
	
	public void handle(Player p, M m) throws IOException {
		throw new IOException("Unexpected message " + m.getClass().getName() + " received");
	}
	
	protected static CacheManager getManager(Player p) {
		return CacheManager.getCacheManager(p);
	}
	
	protected static void sendSubMessage(Player p, SubMessage m) {
		try {
			p.sendPluginMessage(getManager(p).getPlugin(), MessageManager.getChannelName(), MessageManager.encode(m));
		} catch (IOException e) {
			p.kickPlayer("ChunkCache: Cache SubMessage encode error, " + e.getMessage());
		}
	}

}
