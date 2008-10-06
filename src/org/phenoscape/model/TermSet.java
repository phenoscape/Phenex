package org.phenoscape.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.obo.datamodel.Namespace;
import org.obo.datamodel.OBOObject;
import org.obo.datamodel.OBOSession;
import org.obo.query.QueryEngine;
import org.obo.query.impl.CategoryObjQuery;
import org.obo.query.impl.NamespaceObjQuery;

/**
 * A TermSet is used to define a collection of ontology terms.  Currently the collection 
 * can be defined using both OBO namespaces and categories (aka "slims" or "subsets").
 * @author Jim Balhoff
 */
public class TermSet {

  private OBOSession session;
  private Collection<Namespace> namespaces = new ArrayList<Namespace>();
  private Collection<String> categories = new ArrayList<String>();
  private Collection<OBOObject> cachedTerms = null;
  private boolean includesProperties = false;
  private TermFilter filter = null;

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
  
  /**
   * @return The terms returned by this TermSet are a union of all terms in the given Namespaces.
   */
  public Collection<Namespace> getNamespaces() {
    return this.namespaces;
  }

  /**
   * @param namespaces The Namespaces from which this TermSet should draw its terms.
   */
  public void setNamespaces(Collection<Namespace> namespaces) {
    this.namespaces = namespaces;
    this.invalidateTerms();
  }
  
  /**
   * @return The terms returned by this TermSet are a union of all terms in the given categories.
   */
  public Collection<String> getCategories() {
    return this.categories;
  }
  
  /**
   * @param categories The categories from which this TermSet should draw its terms.
   */
  public void setCategories(Collection<String> categories) {
    this.categories = categories;
    this.invalidateTerms();
  }

  public boolean includesProperties() {
    return this.includesProperties;
  }

  public void setIncludesProperties(boolean includeProperties) {
    this.includesProperties = includeProperties;
    this.invalidateTerms();
  }
  
  public TermFilter getTermFilter() {
    return this.filter;
  }
  
  public void setTermFilter(TermFilter filter) {
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
    final List<String> namespaceIDs = new ArrayList<String>();
    for (Namespace ns : this.getNamespaces()) { namespaceIDs.add(ns.getID()); }
    final NamespaceObjQuery query = new NamespaceObjQuery(namespaceIDs, false, true);
    query.setJustTerms(!this.includesProperties());
    final Collection<OBOObject> termsInNamespaces = engine.query(query);
    if (this.hasAnyCategory()) {
      this.cachedTerms = engine.subquery(termsInNamespaces, new CategoryObjQuery(this.getCategories())).getResults();
    } else {
      this.cachedTerms = termsInNamespaces;
    }
    if (this.filter != null) {
      final List<OBOObject> keeps = new ArrayList<OBOObject>();
      for (OBOObject term : this.cachedTerms) {
        if (this.filter.include(term)) { keeps.add(term); }
      }
      this.cachedTerms = keeps;
    }
    return this.cachedTerms;
  }
  
  private boolean hasAnyCategory() {
    return !this.getCategories().isEmpty();
  }
  
  private void invalidateTerms() {
    this.cachedTerms = null;
  }
  
}
