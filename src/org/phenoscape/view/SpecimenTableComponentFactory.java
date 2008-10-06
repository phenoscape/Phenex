package org.phenoscape.view;

import org.bbop.framework.AbstractComponentFactory;
import org.phenoscape.model.PhenoscapeController;

public class SpecimenTableComponentFactory extends AbstractComponentFactory<SpecimenTableComponent> {

  private final PhenoscapeController controller;
  
  public SpecimenTableComponentFactory(PhenoscapeController controller) {
    super();
    this.controller = controller;
  }

  @Override
  public SpecimenTableComponent doCreateComponent(String id) {
    return new SpecimenTableComponent(id, this.controller);
  }

  public FactoryCategory getCategory() {
    return FactoryCategory.ANNOTATION;
    }

  public String getID() {
    return "phenoscape_specimens_table";
    }

  public String getName() {
    return "Specimens";
  }
  
  @Override
  public boolean isSingleton() {
    return true;
  }

}
