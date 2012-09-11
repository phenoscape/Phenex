package org.phenoscape.view;

import org.bbop.framework.AbstractComponentFactory;
import org.phenoscape.controller.PhenexController;

public class PhenotypeTableComponentFactory extends AbstractComponentFactory<PhenotypeTableComponent> {

	private final PhenexController controller;

	public PhenotypeTableComponentFactory(PhenexController controller) {
		super();
		this.controller = controller;
	}

	@Override
	public PhenotypeTableComponent doCreateComponent(String id) {
		return new PhenotypeTableComponent(id, this.controller);
	}

	@Override
	public FactoryCategory getCategory() {
		return FactoryCategory.ANNOTATION;
	}

	@Override
	public String getID() {
		return "phenoscape_phenotypes_table";
	}

	@Override
	public String getName() {
		return "Phenotypes";
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}
