package de.nubenum.app.plugin.logaggregator.test;



import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.nubenum.app.plugin.logaggregator.core.VirtualBinarySearch;
import de.nubenum.app.plugin.logaggregator.core.model.Direction;

public class VirtualBinarySearchTest {

	@Test
	public void test() {
		Long size = 900000L;
		VirtualBinarySearch<Long> search = new VirtualBinarySearch<>();
		search.setComparator((a, b) -> Direction.get(a-b));
		search.setElementRetriever((pivot, offset) -> {
			Long s = pivot+offset*2;
			return (s >= 0 && s < size ? s : null);
		});
		assertEquals(0, (long)search.search(0L, 0L));
		assertEquals(0, (long)search.search(1L, 0L));
		assertEquals(0, (long)search.search(0L, 2L));
		assertEquals(500, (long)search.search(501L, 0L));
		assertEquals(500, (long)search.search(500L, 434L));
		assertEquals(100000, (long)search.search(100001L, 500000L));
		assertEquals(450000, (long)search.search(450000L, 0L));
		assertEquals(749436, (long)search.search(749435L, 34588L));
		assertEquals(899998, (long)search.search(899999L, 0L));
		assertEquals(899998, (long)search.search(900000L, 345346L));
		assertEquals(899998, (long)search.search(1034534L, 0L));
		assertEquals(0, (long)search.search(-1L, 0L));
	}

	@Test
	public void testInverse() {
		Long size = 900000000000000L;
		VirtualBinarySearch<Long> search = new VirtualBinarySearch<>();
		search.setComparator((a, b) -> Direction.get(a-b));
		search.setSearchDirection(-1L);
		search.setElementRetriever((pivot, offset) -> {
			Long s = pivot+offset*2;
			return (s >= 0 && s < size ? s : null);
		});
		assertEquals(0, (long)search.search(0L, 0L));
		assertEquals(0, (long)search.search(1L, 2L));
		assertEquals(50504, (long)search.search(50505L, 23497238L));
		assertEquals(348324069234L, (long)search.search(348324069235L, 34957395723958L));
		assertEquals(749434, (long)search.search(749434L, size));
		assertEquals(2345789357238L, (long)search.search(2345789357238L, size));
		assertEquals(0, (long)search.search(-900000L, 34579346L));
		assertEquals(13453452344L, (long)search.search(234589023405820385L, 13453452346L)); //wrong found since wrong direction

	}

}
