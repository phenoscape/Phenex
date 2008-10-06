package org.phenoscape.view;

import org.bbop.framework.AbstractComponentFactory;
import org.phenoscape.model.PhenoscapeController;

public class PhenotypeTableComponentFactory extends AbstractComponentFactory<PhenotypeTableComponent> {
  
  private final PhenoscapeController controller;

  public PhenotypeTableComponentFactory(PhenoscapeController controller) {
    super();
    this.controller = controller;
  }

  @Override
  public PhenotypeTableComponent doCreateComponent(String id) {
    return new PhenotypeTableComponent(id, this.controller);
  }

  public FactoryCategory getCategory() {
    return FactoryCategory.ANNOTATION;
  }

  public String getID() {
    return "phenoscape_phenotypes_table";
  }

  public String getName() {
    return "Phenotypes";
  }

  @Override
  public boolean isSingleton() {
    return true;
  }
  
}
