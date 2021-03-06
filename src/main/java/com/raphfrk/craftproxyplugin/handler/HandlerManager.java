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

import com.raphfrk.craftproxycommon.message.SubMessage;

public class HandlerManager {
	
	@SuppressWarnings("rawtypes")
	private final static Handler[] handlers = new Handler[4];
	
	static {
		handlers[0] = new InitMessageHandler();
		handlers[1] = new HashRequestMessageHandler();
		handlers[2] = new HashDataMessageHandler();
		handlers[3] = new SectionAckMessageHandler();
	}
	
	@SuppressWarnings("unchecked")
	public static void handle(Player p, SubMessage m) throws IOException {
		int id = m.getId();
		if (id < 0) {
			return;
		} else if (id >= handlers.length) {
			throw new IllegalStateException("Id out of range");
		}
		
		@SuppressWarnings("rawtypes")
		Handler h = handlers[id];
		h.handle(p, m);
	}

}
