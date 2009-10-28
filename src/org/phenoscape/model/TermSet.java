package org.phenoscape.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.obo.datamodel.IdentifiedObject;
import org.obo.datamodel.OBOObject;
import org.obo.datamodel.OBOSession;
import org.obo.filters.Filter;
import org.obo.query.QueryEngine;
import org.obo.query.impl.FilterQuery;
import org.obo.query.impl.SearchHit;
import org.oboedit.controller.SessionManager;

/**
 * A TermSet is used to define a collection of ontology terms.  Currently the collection 
 * can be defined using both OBO namespaces and categories (aka "slims" or "subsets").
 * @author Jim Balhoff
 */
public class TermSet {

    private OBOSession session;
    private Collection<OBOObject> cachedTerms = null;
    private Filter<IdentifiedObject> filter = null;

    /**
     * @return The OBOSession from which this TermSet draws its terms.
     */
    public OBOSession getOBOSession() {
        return this.session;
    }

    /**
     * @param oboSession The OBOSession from which this TermSet should draw its terms.
     */
    public void setOBOSession(OBOSession oboSession) {
        this.session = oboSession;
        this.invalidateTerms();
    }

    public Filter<IdentifiedObject> getTermFilter() {
        return this.filter;
    }

    public void setTermFilter(Filter<IdentifiedObject> filter) {
        this.filter = filter;
    }

    /**
     * @return All terms matching the search criteria of this TermSet, such as its namespaces and categories.
     */
    public Collection<OBOObject> getTerms() {
        if (this.cachedTerms != null) {
            return this.cachedTerms;
        }
        final QueryEngine engine = new QueryEngine(this.getOBOSession());
        final FilterQuery<IdentifiedObject> query = new FilterQuery<IdentifiedObject>(this.getTermFilter(), IdentifiedObject.class, SessionManager.getManager().getReasoner());
        final Collection<SearchHit<IdentifiedObject>> termHits = engine.query(query);
        final List<OBOObject> terms = new ArrayList<OBOObject>(); 
        for (SearchHit<IdentifiedObject> hit : termHits) {
            terms.add((OBOObject)hit.getHit());  //TODO fix need for this cast
        }
        this.cachedTerms = terms;
        return this.cachedTerms;
    }

    private void invalidateTerms() {
        this.cachedTerms = null;
    }
    
    @SuppressWarnings("unused")
    private Logger log() {
        return Logger.getLogger(this.getClass());
    }

}
