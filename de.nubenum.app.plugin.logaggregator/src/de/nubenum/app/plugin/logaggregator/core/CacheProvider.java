package de.nubenum.app.plugin.logaggregator.core;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

import de.nubenum.app.plugin.logaggregator.core.layers.entries.IEntryLog;
import de.nubenum.app.plugin.logaggregator.core.model.ReferenceOffset;
import de.nubenum.app.plugin.logaggregator.core.model.entry.IEntry;

/**
 * Provides a Cache for IEntries.
 *
 */
public class CacheProvider implements IEntryLog {
	private static final int MAX_SIZE = 50;
	private Map<ReferenceOffset, IEntry> cache;

	public CacheProvider() {
		cache = new LinkedHashMap<ReferenceOffset, IEntry>(MAX_SIZE * 4 / 3, 0.75f, false) {
			private static final long serialVersionUID = -4731634430629826923L;

			@Override
			protected boolean removeEldestEntry(java.util.Map.Entry<ReferenceOffset, IEntry> eldest) {
				return size() > MAX_SIZE;
			}
		};
	}

	@Override
	public IEntry getAt(IEntry reference, int offset) {
		return cache.get(new ReferenceOffset(reference, offset));
	}

	/**
	 * Save a new IEntry to the cache.
	 *
	 * @param reference
	 *            The reference used to identify the cached item.
	 * @param offset
	 *            The offset used to identify the cached item.
	 * @param cached
	 *            The item to be cached.
	 */
	public void put(IEntry reference, int offset, IEntry cached) {
		cache.put(new ReferenceOffset(reference, offset), cached);
	}

	/**
	 * For IEntries with a lot of children, it can be worth to try and find these by
	 * one of their children instead of the ReferenceOffset. This is useful if a
	 * cached entry is accessed by another ReferenceOffset and thus would not be
	 * found with the existing ReferenceOffset.
	 *
	 * @param entry
	 *            A child IEntry used to obtain the parent IEntry.
	 * @return The parent IEntry found or null.
	 */
	public IEntry getByChildAt(IEntry entry) {
		Predicate<IEntry> contains = e -> {
			return e.contains(entry);
		};
		IEntry cached = cache.values().stream().filter(contains).findFirst().orElse(null);
		return cached;
	}

	@Override
	public void close(boolean keepInit) {
		if (!keepInit)
			cache.clear();
	}
}
