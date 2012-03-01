package org.phenoscape.view;

import org.bbop.framework.AbstractComponentFactory;
import org.phenoscape.controller.PhenexController;

public class StateTableComponentFactory extends AbstractComponentFactory<StateTableComponent> {

	private final PhenexController controller;

	public StateTableComponentFactory(PhenexController controller) {
		this.controller = controller;
	}

	@Override
	public StateTableComponent doCreateComponent(String id) {
		return new StateTableComponent(id, this.controller);
	}

	public FactoryCategory getCategory() {
		return FactoryCategory.ANNOTATION;
	}

	public String getID() {
		return "phenoscape_states_browser";
	}

	public String getName() {
		return "States";
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}
