package org.phenoscape.view;

import org.bbop.framework.AbstractComponentFactory;
import org.phenoscape.controller.PhenexController;

public class StateSupportComponentFactory extends AbstractComponentFactory<StateSupportComponent> {

	private final PhenexController controller;

	public StateSupportComponentFactory(PhenexController controller) {
		super();
		this.controller = controller;
	}

	@Override
	public StateSupportComponent doCreateComponent(String id) {
		return new StateSupportComponent(id, this.controller);
	}

	@Override
	public FactoryCategory getCategory() {
		return FactoryCategory.ANNOTATION;
	}

	@Override
	public String getID() {
		return "phenoscape_state_support_table";
	}

	@Override
	public String getName() {
		return "Supporting State Sources";
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
}
