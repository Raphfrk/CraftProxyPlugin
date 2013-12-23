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
package com.raphfrk.craftproxyplugin.hash;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.TShortObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.map.hash.TShortObjectHashMap;
import gnu.trove.set.hash.THashSet;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

public class SectionMap {
	
	private TLongObjectMap<Reference<Hash>> hashMap = new TLongObjectHashMap<Reference<Hash>>();
	private TShortObjectMap<THashSet<Hash>> activeSections = new TShortObjectHashMap<THashSet<Hash>>();
	private ReferenceQueue<Hash> refQueue = new ReferenceQueue<Hash>();

	public void add(short id, Hash hash) {
		processQueue();
		Reference<Hash> mappedHashRef = hashMap.get(hash.getHash());
		Hash mappedHash = null;
		if (mappedHashRef != null) {
			mappedHash = mappedHashRef.get();
			if (mappedHash != null) {
				hash = mappedHash;
			}
		}
		if (mappedHash == null) {
			hashMap.put(hash.getHash(), new KeyWeakReference(hash, refQueue));
		}
		THashSet<Hash> set = activeSections.get(id);
		if (set == null) {
			set = new THashSet<Hash>();
			activeSections.put(id, set);
		}
		set.add(hash);
	}
	
	public Hash get(long hash) {
		processQueue();
		Reference<Hash> ref = hashMap.get(hash);
		if (ref == null) {
			return null;
		}
		return ref.get();
	}
	
	public void ackSection(short id) {
		processQueue();
		activeSections.remove(id);
	}
	
	private void processQueue() {
		KeyWeakReference ref;
		while ((ref = (KeyWeakReference) refQueue.poll()) != null) {
			hashMap.remove(ref.getKey());
		}
	}
	
	private static class KeyWeakReference extends WeakReference<Hash> {

		private long key;
		
		public KeyWeakReference(Hash hash, ReferenceQueue<Hash> queue) {
			super(hash, queue);
			this.key = hash.getHash();
		}
		
		public long getKey() {
			return key;
		}
		
	}
	

}
