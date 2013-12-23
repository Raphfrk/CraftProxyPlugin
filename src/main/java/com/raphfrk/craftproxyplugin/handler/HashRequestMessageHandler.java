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

import org.bukkit.entity.Player;

import com.raphfrk.craftproxyplugin.hash.Hash;
import com.raphfrk.craftproxyplugin.hook.CacheManager;
import com.raphfrk.craftproxyplugin.message.HashDataMessage;
import com.raphfrk.craftproxyplugin.message.HashRequestMessage;

public class HashRequestMessageHandler extends Handler<HashRequestMessage> {

	@Override
	public void handle(Player p, HashRequestMessage m) {
		CacheManager manager = getManager(p);
		long[] hashes = m.getHashes();
		Hash[] hashData = new Hash[hashes.length];
		for (int i = 0; i < hashes.length; i++) {
			hashData[i] = manager.getHash(hashes[i]);
			if (hashData[i] == null) {
				p.kickPlayer("Unable to find requested hash");
				return;
			}
		}
		int pos = 0;
		while (pos < hashes.length) {
			int len = 0;
			int i;
			for (i = pos; i < hashes.length; i++) {
				int hashLength = hashData[i].getLength();
				if (hashLength + len > 25000) {
					break;
				}
				len += hashLength;
			}

			HashDataMessage dataMessage = new HashDataMessage(hashData, pos, i - pos);
			sendSubMessage(p, dataMessage);
					
			pos = i;
		}
	}
}
