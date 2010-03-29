package org.obo.app.util;

import java.util.Enumeration;
import java.util.Iterator;

public class Enumerator<T> implements Iterable<T>, Iterator<T> {

    private final Enumeration<T> enumeration;

    public Enumerator(Enumeration<T> enumeration) {
        this.enumeration = enumeration;
    }

    public Iterator<T> iterator() {
        return this;
    }

    public boolean hasNext() {
        return this.enumeration.hasMoreElements();
    }

    public T next() {
        return this.enumeration.nextElement();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

}
