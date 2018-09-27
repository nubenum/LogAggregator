package de.nubenum.app.plugin.logaggregator.core;

import java.net.URI;
import java.util.function.Function;

public class Utils {

	/**
	 * Check whether objects are equal by comparing the given members (if the other object is of the same type)
	 * @param cls The type of the object
	 * @param thisObject The existing object
	 * @param otherObject The object to check
	 * @param members A Function that maps an object of Type to one of its members that should be checked
	 * @return whether the to objects equal because they are identical or their to-be-checked members are equal
	 */
	@SafeVarargs
	public static <Type> boolean objectsEqual(Class<Type> cls, Type thisObject, Object otherObject, Function<Type, Object>... members) {
		if (thisObject == otherObject)
			return true;
		if (cls.isInstance(otherObject)) {
			Type cast = cls.cast(otherObject);
			for(Function<Type, Object> member : members) {
				Object a = member.apply(thisObject);
				if (a == null || !a.equals(member.apply(cast)))
					return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * Get the file name from an URI. This will lead to undefined results when using it on a directory path.
	 * @param path The path from which to extract the name
	 * @return The file name (the part behind the last slash)
	 */
	public static String getFileName(URI path) {
		String[] parts = path.getPath().split("[/\\\\]");
		return parts[parts.length-1];
	}
}
