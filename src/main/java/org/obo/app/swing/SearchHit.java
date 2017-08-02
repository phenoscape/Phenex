package org.obo.app.swing;

public interface SearchHit<T> {
    
    /**
     * Returns the actual value object of interest which was matched by the search.
     */
    public T getHit();
    
    /**
     * Returns the textual component of this item which was somehow matched by the input text.
     */
    public String getMatchText();
    
    /**
     * Return the MatchType corresponding to how this item was matched via the input text.
     */
    public MatchType getMatchType();
    
    /**
     * Returns the preferred label for this item - not necessarily the component text that was matched.
     */
    public String getPrimaryText();
    
    /**
     * Return the Class of the value object this hit represents.
     */
    public Class<T> getHitClass();

}
