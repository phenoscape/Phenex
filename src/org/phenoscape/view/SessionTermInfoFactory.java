package org.phenoscape.view;

import phenote.gui.TermInfo2;
import phenote.gui.factories.TermInfoFactory;

public class SessionTermInfoFactory extends TermInfoFactory {

  @Override
  public TermInfo2 doCreateComponent(String id) {
    return new TermInfo2(true);
  }

  @Override
  public FactoryCategory getCategory() {
    return FactoryCategory.ONTOLOGY;
  }

  @Override
  public String getName() {
    return "Term Info";
  }

}
