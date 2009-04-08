package org.phenoscape.swing;

public interface SearchHit<T> {
    
    public T getHit();
    
    public String getMatchText();
    
    public MatchType getMatchType();
    
    public String getPrimaryText();
    
    public Class<T> getHitClass();

}
