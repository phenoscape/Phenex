package org.phenoscape.view;

import org.bbop.framework.AbstractComponentFactory;
import org.phenoscape.model.PhenoscapeController;

public class CharacterTableComponentFactory extends AbstractComponentFactory<CharacterTableComponent> {
  
  private final PhenoscapeController controller;

  public CharacterTableComponentFactory(PhenoscapeController controller) {
    this.controller = controller;
  }
  
  @Override
  public CharacterTableComponent doCreateComponent(String id) {
    return new CharacterTableComponent(id, this.controller);
  }

  public FactoryCategory getCategory() {
    return FactoryCategory.ANNOTATION;
  }

  public String getID() {
    return "phenoscape_characters_browser";
  }

  public String getName() {
    return "Characters";
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

}
