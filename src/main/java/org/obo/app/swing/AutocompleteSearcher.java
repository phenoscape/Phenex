package org.obo.app.swing;

import java.util.List;

public interface AutocompleteSearcher<T> {

    public void setSearch(String input);

    public List<SearchHit<T>> getMatches();

    public String toString(T valueObject);

    public SearchHit<T> getExactHit(String text);

    public SearchHit<T> getAsHit(T item);

    /**
     * Returns whether the given text could match the given item as an exact hit
     */
    public boolean isSame(String text, T item);

    public Class<T> getHitClass();

}
