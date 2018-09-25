package de.nubenum.app.plugin.logaggregator.core;

import java.util.function.Function;

public class EqualsHelper {

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
}
