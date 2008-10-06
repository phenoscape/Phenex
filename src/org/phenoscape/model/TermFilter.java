package org.phenoscape.model;

import org.obo.datamodel.OBOObject;

public interface TermFilter {
  
  public boolean include(OBOObject term);

}
