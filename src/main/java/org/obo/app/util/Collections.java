package org.obo.app.util;

import java.util.Collection;
import java.util.Iterator;

public class Collections {

	private Collections(){}

	public static String join(Collection<?> collection, String separator) {
		final Iterator<?> iterator = collection.iterator();
		final StringBuffer buffer = new StringBuffer();
		while (iterator.hasNext()) {
			final Object next = iterator.next();
			if (next != null) {
				buffer.append(next);
				if (iterator.hasNext()) buffer.append(separator);
			}

		}
		return buffer.toString();
	}

	public static <T> T get(T[] array, int index) {
		if (index < array.length) {
			return array[index];
		} else {
			return null;
		}
	}

}
