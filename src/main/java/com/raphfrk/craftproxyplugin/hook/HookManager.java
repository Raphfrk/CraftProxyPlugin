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

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public abstract class HookManager {
	
	private final static Map<String, HookManager> managers = new HashMap<String, HookManager>();
	
	private static HookManager manager;
	
	static {
		register(new com.raphfrk.craftproxyplugin.hook.v1_6_R3.HookManager());
	}
	
	private static void register(HookManager manager) {
		if (managers.put(manager.getVersion(), manager) != null) {
			throw new IllegalStateException("HookManager version string " + manager.getVersion() + " used more than once");
		}
	}
	
	private static String getVersionString() {
		String[] split = Bukkit.getServer().getClass().getPackage().getName().split("\\.");
		return split[split.length - 1];
	}
	
	public static boolean init() {
		manager = managers.get(getVersionString());
		return manager != null;
	}
	
	public static HookManager getManager() {
		return manager;
	}
	
	public abstract String getVersion();
	
	public abstract void hookQueue(Player player);

}
