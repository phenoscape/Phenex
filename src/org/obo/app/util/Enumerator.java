package org.obo.app.util;

import java.util.Enumeration;
import java.util.Iterator;

public class Enumerator<T> implements Iterable<T>, Iterator<T> {

	private final Enumeration<T> enumeration;

	public Enumerator(Enumeration<T> enumeration) {
		this.enumeration = enumeration;
	}

	@Override
	public Iterator<T> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {
		return this.enumeration.hasMoreElements();
	}

	@Override
	public T next() {
		return this.enumeration.nextElement();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
