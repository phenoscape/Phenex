package org.phenoscape.model;

import org.obo.datamodel.OBOObject;
import org.obo.datamodel.OBOSession;

public class RelationTermFilter implements TermFilter {
  
  private final OBOSession session;

  public RelationTermFilter(OBOSession session) {
    this.session = session;
  }
  
  public boolean include(OBOObject term) {
    return !term.equals(this.session.getObject("part_of"));
  }

}
