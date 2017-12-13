package org.phenoscape.view;

import org.bbop.framework.AbstractComponentFactory;
import org.phenoscape.controller.PhenexController;

public class SpecimenTableComponentFactory extends AbstractComponentFactory<SpecimenTableComponent> {

	private final PhenexController controller;

	public SpecimenTableComponentFactory(PhenexController controller) {
		super();
		this.controller = controller;
	}

	@Override
	public SpecimenTableComponent doCreateComponent(String id) {
		return new SpecimenTableComponent(id, this.controller);
	}

	@Override
	public FactoryCategory getCategory() {
		return FactoryCategory.ANNOTATION;
	}

	@Override
	public String getID() {
		return "phenoscape_specimens_table";
	}

	@Override
	public String getName() {
		return "Specimens";
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}
