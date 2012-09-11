package org.phenoscape.view;

import org.bbop.framework.AbstractComponentFactory;
import org.phenoscape.controller.PhenexController;

public class DataSetComponentFactory extends AbstractComponentFactory<DataSetComponent> {

	private final PhenexController controller;

	public DataSetComponentFactory(PhenexController controller) {
		this.controller = controller;
	}

	@Override
	public DataSetComponent doCreateComponent(String id) {
		return new DataSetComponent(id, this.controller);
	}

	@Override
	public FactoryCategory getCategory() {
		return FactoryCategory.ANNOTATION;
	}

	@Override
	public String getID() {
		return "phenoscape_dataset_component";
	}

	@Override
	public String getName() {
		return "Data Set";
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}
