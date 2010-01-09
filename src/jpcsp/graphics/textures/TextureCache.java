/*
This file is part of jpcsp.

Jpcsp is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Jpcsp is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Jpcsp.  If not, see <http://www.gnu.org/licenses/>.
 */
package jpcsp.graphics.textures;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.media.opengl.GL;

public class TextureCache {
	public static final int cacheMaxSize = 1000;
	public static final float cacheLoadFactor = 0.75f;
	private static TextureCache instance = null;
	private LinkedHashMap<Integer, Texture> cache;
	public Statistics statistics = new Statistics();
	// Remember which textures have already been hashed during one display
	// (for applications reusing the same texture multiple times in one display)
	private Set<Integer> textureAlreadyHashed;

	public class Statistics {
		public long totalHits = 0;			// Number of times a texture was searched
		public long successfulHits = 0;		// Number of times a texture was successfully found
		public long notPresentHits = 0;		// Number of times a texture was not present
		public long changedHits = 0;		// Number of times a texture was present but had to be discarded because it was changed
		public long entriesRemoved = 0;		// Number of times a texture had to be removed from the cache due to the size limit
		public long maxSizeUsed = 0;		// Maximum size of the cache

		private String percentage(long n, long max) {
			return String.format("%.2f%%", (n / (double) max) * 100);
		}

		private String percentage(long hits) {
			return percentage(hits, totalHits);
		}

		@Override
		public String toString() {
			StringBuffer result = new StringBuffer();
			result.append("TextureCache Statistics: ");
			if (totalHits == 0) {
				result.append("Cache deactivated");
			} else {
			    result.append("TotalHits=" + totalHits + ", ");
			    result.append("SuccessfulHits=" + successfulHits + " (" + percentage(successfulHits) + "), ");
			    result.append("NotPresentHits=" + notPresentHits + " (" + percentage(notPresentHits) + "), ");
			    result.append("ChangedHits=" + changedHits + " (" + percentage(changedHits) + "), ");
			    result.append("EntriesRemoved=" + entriesRemoved + ", ");
			    result.append("MaxSizeUsed=" + maxSizeUsed + " (" + percentage(maxSizeUsed, cacheMaxSize) + ")");
			}
			return result.toString();
		}
	}

	public static TextureCache getInstance() {
		if (instance == null) {
			instance = new TextureCache();
		}

		return instance;
	}

	private TextureCache() {
		//
		// Create a cache having
		// - initial size large enough so that no rehash will occur
		// - the LinkedList is based on access-order for LRU
		//
		cache = new LinkedHashMap<Integer, Texture>((int) (cacheMaxSize / cacheLoadFactor) + 1, cacheLoadFactor, true);
		textureAlreadyHashed = new HashSet<Integer>();
	}

	private Integer getKey(int addr, int clutAddr) {
		// Some games use the same texture address with different cluts.
		// Keep a combination of both texture address and clut address in the cache
		return new Integer(addr + clutAddr);
	}

	public boolean hasTexture(int addr, int clutAddr) {
		return cache.containsKey(getKey(addr, clutAddr));
	}

	private Texture getTexture(int addr, int clutAddr) {
		return cache.get(getKey(addr, clutAddr));
	}

	public void addTexture(GL gl, Texture texture) {
		Integer key = getKey(texture.getAddr(), texture.getClutAddr());
		Texture previousTexture = cache.get(key);
		if (previousTexture != null) {
		    previousTexture.deleteTexture(gl);
		} else {
			// Check if the cache is not growing too large
			if (cache.size() >= cacheMaxSize) {
				// Remove the LRU cache entry
				Iterator<Map.Entry<Integer, Texture>> it = cache.entrySet().iterator();
				if (it.hasNext()) {
					Map.Entry<Integer, Texture> entry = it.next();
					entry.getValue().deleteTexture(gl);
					it.remove();

					statistics.entriesRemoved++;
				}
			}
		}

        cache.put(key, texture);

        if (cache.size() > statistics.maxSizeUsed) {
            statistics.maxSizeUsed = cache.size();
        }
	}

	public Texture getTexture(int addr, int lineWidth, int width, int height, int pixelStorage, int clutAddr, int clutMode, int clutStart, int clutShift, int clutMask, int clutNumBlocks, int mipmapLevels, boolean mipmapShareClut) {
		statistics.totalHits++;
		Texture texture = getTexture(addr, clutAddr);

		if (texture == null) {
			statistics.notPresentHits++;
			return texture;
		}

		if (texture.equals(addr, lineWidth, width, height, pixelStorage, clutAddr, clutMode, clutStart, clutShift, clutMask, clutNumBlocks, mipmapLevels, mipmapShareClut)) {
			statistics.successfulHits++;
			return texture;
		}

		statistics.changedHits++;
		return null;
	}

	public void resetTextureAlreadyHashed() {
		textureAlreadyHashed.clear();
	}

	public boolean textureAlreadyHashed(int addr, int clutAddr) {
		return textureAlreadyHashed.contains(getKey(addr, clutAddr));
	}

	public void setTextureAlreadyHashed(int addr, int clutAddr) {
		textureAlreadyHashed.add(getKey(addr, clutAddr));
	}
}
