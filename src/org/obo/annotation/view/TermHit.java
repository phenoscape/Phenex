package org.obo.annotation.view;

import org.obo.app.swing.MatchType;
import org.obo.app.swing.SearchHit;
import org.obo.datamodel.OBOObject;

public class TermHit implements SearchHit<OBOObject> {
    
    private final OBOObject term;
    private final MatchType type;
    private final String text;
    
    public TermHit(OBOObject term, String text, MatchType matchType) {
        this.term = term;
        this.text = text;
        this.type = matchType;
    }

    public OBOObject getHit() {
        return this.term;
    }

    public String getMatchText() {
        return this.text;
    }

    public MatchType getMatchType() {
        return this.type;
    }

    @Override
    public String toString() {
        return this.getMatchText() + ": {" + this.getMatchType().getName() + "}";
    }
    
    public String getPrimaryText() {
        return term.getName();
    }
    
    public Class<OBOObject> getHitClass() {
        return OBOObject.class;
    }
    
}
