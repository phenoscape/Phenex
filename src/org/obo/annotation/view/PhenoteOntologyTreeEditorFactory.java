package org.obo.annotation.view;

import org.oboedit.gui.factory.TermPanelFactory;

/**
 * This class is used only to provide a customized name for this OBO-Edit 
 * component within Phenote.
 * @author Jim Balhoff
 */
public class PhenoteOntologyTreeEditorFactory extends TermPanelFactory {

  @Override
  public String getName() {
    return "Complete Ontology Tree View";
  }

  @Override
  public org.bbop.framework.GUIComponentFactory.FactoryCategory getCategory() {
    return FactoryCategory.ONTOLOGY;
  }
  
  

}
