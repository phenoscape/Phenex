package org.phenoscape.view;

import org.bbop.framework.AbstractComponentFactory;
import org.phenoscape.model.PhenoscapeController;

public class CharacterMatrixComponentFactory extends AbstractComponentFactory<CharacterMatrixComponent> {

  private final PhenoscapeController controller;
  
  public CharacterMatrixComponentFactory(PhenoscapeController controller) {
    super();
    this.controller = controller;
  }

  @Override
  public CharacterMatrixComponent doCreateComponent(String id) {
    return new CharacterMatrixComponent(id, this.controller);
  }

  public FactoryCategory getCategory() {
    return FactoryCategory.ANNOTATION;
    }

  public String getID() {
    return "phenoscape_character_matrix";
    }

  public String getName() {
    return "Matrix";
  }
  
  @Override
  public boolean isSingleton() {
    return true;
  }
  
}
