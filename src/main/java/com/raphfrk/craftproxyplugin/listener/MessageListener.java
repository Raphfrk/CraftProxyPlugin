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
package com.raphfrk.craftproxyplugin.listener;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.raphfrk.craftproxycommon.message.MessageManager;
import com.raphfrk.craftproxycommon.message.SubMessage;
import com.raphfrk.craftproxyplugin.CraftProxyPlugin;
import com.raphfrk.craftproxyplugin.handler.HandlerManager;

public class MessageListener implements PluginMessageListener {
	
	private final CraftProxyPlugin plugin;
	
	public MessageListener(CraftProxyPlugin plugin) {
		this.plugin = plugin;
	}
	
	public void register() {
		Bukkit.getServer().getMessenger().registerIncomingPluginChannel(plugin, MessageManager.getChannelName(), this);
		Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(plugin, MessageManager.getChannelName());
	}

	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		if (!MessageManager.getChannelName().equals(channel)) {
			return;
		}
		SubMessage m;
		try {
			m = MessageManager.decode(message);
		} catch (IOException e) {
			plugin.getLogger().info("Unable to decode custom message " + e.getMessage());
			return;
		}
		if (m == null) {
			plugin.getLogger().info("Unable to decode custom message");
			return;
		}
		try {
			HandlerManager.handle(player, m);
		} catch (IOException e) {
			player.kickPlayer("Cache packet handler error " + e.getMessage());
		}
	}
}
