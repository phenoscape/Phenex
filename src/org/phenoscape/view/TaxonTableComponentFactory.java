package org.phenoscape.view;

import org.bbop.framework.AbstractComponentFactory;
import org.phenoscape.controller.PhenexController;

public class TaxonTableComponentFactory extends AbstractComponentFactory<TaxonTableComponent> {

  private final PhenexController controller;
  
  public TaxonTableComponentFactory(PhenexController controller) {
    this.controller = controller;
  }

  @Override
  public TaxonTableComponent doCreateComponent(String id) {
    return new TaxonTableComponent(id, this.controller);
  }

  public FactoryCategory getCategory() {
    return FactoryCategory.ANNOTATION;
  }

  public String getID() {
    return "phenoscape_taxa_table";
  }

  public String getName() {
    return "Taxa";
  }
  
  @Override
  public boolean isSingleton() {
    return true;
  }

}
