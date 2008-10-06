package org.phenoscape.view;

import java.util.List;

import org.obo.datamodel.OBOObject;
import org.obo.datamodel.Synonym;

import ca.odell.glazedlists.TextFilterator;

public class TermFilterator implements TextFilterator<OBOObject> {

  public void getFilterStrings(List<String> list, OBOObject term) {
    list.add(term.getName());
    for (Synonym synonym : term.getSynonyms()) {
      list.add(synonym.getText());
    }
  }
  
}
