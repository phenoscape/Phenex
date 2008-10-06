package org.phenoscape.view;

import org.bbop.framework.AbstractComponentFactory;

public class OntologyPreferencesComponentFactory extends AbstractComponentFactory<OntologyPreferencesComponent> {

  @Override
  public OntologyPreferencesComponent doCreateComponent(String id) {
    return new OntologyPreferencesComponent(id);
  }

  public FactoryCategory getCategory() {
    return FactoryCategory.CONFIG;
  }

  public String getID() {
    return "phenoscape_ontology_preferences";
  }

  public String getName() {
    return "Ontology Preferences";
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

  @Override
  public boolean getPreferSeparateWindow() {
    return true;
  }
  
}
