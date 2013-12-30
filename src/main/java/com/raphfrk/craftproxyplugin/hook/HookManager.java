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

import com.raphfrk.craftproxyplugin.CraftProxyPlugin;

public abstract class HookManager {
	
	private final static Map<String, String> managers = new HashMap<String, String>();
	
	private static HookManager manager;
	
	static {
		register("v1_6_R3", "com.raphfrk.craftproxyplugin.hook.v1_6_R3.HookManager");
		register("v1_6_R2", "com.raphfrk.craftproxyplugin.hook.v1_6_R2.HookManager");
		register("v1_7_R1", "com.raphfrk.craftproxyplugin.hook.v1_7_R1.HookManager");
	}
	
	private static void register(String version, String manager) {
		if (managers.put(version, manager) != null) {
			throw new IllegalStateException("HookManager version string " + version + " used more than once");
		}
	}
	
	public static String getVersionString() {
		String[] split = Bukkit.getServer().getClass().getPackage().getName().split("\\.");
		return split[split.length - 1];
	}
	
	public static boolean init() {
		String fullName = managers.get(getVersionString());
		if (fullName != null) {
			try {
				@SuppressWarnings("unchecked")
				Class<HookManager> clazz = (Class<HookManager>) Class.forName(fullName);
				manager = clazz.newInstance();
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException e) {
				manager = null;
			}
		}
		return manager != null;
	}
	
	public static HookManager getManager() {
		return manager;
	}
	
	public abstract String getVersion();
	
	public abstract void hookQueue(CraftProxyPlugin plugin, Player player);
	
}
