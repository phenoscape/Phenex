package org.phenoscape.view;

import org.bbop.framework.AbstractComponentFactory;
import org.phenoscape.model.PhenoscapeController;

public class DataSetComponentFactory extends AbstractComponentFactory<DataSetComponent> {
  
  private final PhenoscapeController controller;

  public DataSetComponentFactory(PhenoscapeController controller) {
    this.controller = controller;
  }

  @Override
  public DataSetComponent doCreateComponent(String id) {
    return new DataSetComponent(id, this.controller);
  }

  public FactoryCategory getCategory() {
    return FactoryCategory.ANNOTATION;
  }

  public String getID() {
    return "phenoscape_dataset_component";
  }

  public String getName() {
    return "Data Set";
  }
  
  @Override
  public boolean isSingleton() {
    return true;
  }

}
