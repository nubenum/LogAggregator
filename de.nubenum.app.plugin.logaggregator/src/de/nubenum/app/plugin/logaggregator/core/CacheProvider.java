package de.nubenum.app.plugin.logaggregator.core;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

import de.nubenum.app.plugin.logaggregator.core.layers.IEntryLog;
import de.nubenum.app.plugin.logaggregator.core.model.IEntry;
import de.nubenum.app.plugin.logaggregator.core.model.ReferenceOffset;

public class CacheProvider implements IEntryLog {
	private static final int MAX_SIZE = 50;
	private Map<ReferenceOffset, IEntry> cache;

	public CacheProvider() {
		cache = new LinkedHashMap<ReferenceOffset, IEntry>(MAX_SIZE*4/3, 0.75f, false) {
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

	public void put(IEntry reference, int offset, IEntry cached) {
		cache.put(new ReferenceOffset(reference, offset), cached);
	}

	public IEntry getByChildAt(IEntry entry) {
		Predicate<IEntry> contains = e -> {
			//if (e.getChildren().size() > 0 && (e.getChildren().get(0).compareTo(e) > 0 || e.getChildren().get(e.getChildren().size()-1).compareTo(e) < 0))
			//	return false;
			return e.getChildren().contains(entry);
		};
		IEntry cached = cache.values().stream()
				.filter(contains)
				.findFirst().orElse(null);
		return cached;
	}
}
