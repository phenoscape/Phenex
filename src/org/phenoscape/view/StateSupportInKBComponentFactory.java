package org.phenoscape.view;

import org.bbop.framework.AbstractComponentFactory;
import org.phenoscape.controller.PhenexController;

public class StateSupportInKBComponentFactory extends AbstractComponentFactory<StateSupportInKBComponent> {

	private final PhenexController controller;

	public StateSupportInKBComponentFactory(PhenexController controller) {
		super();
		this.controller = controller;
	}

	@Override
	public StateSupportInKBComponent doCreateComponent(String id) {
		return new StateSupportInKBComponent(id, this.controller);
	}

	@Override
	public FactoryCategory getCategory() {
		return FactoryCategory.ANNOTATION;
	}

	@Override
	public String getID() {
		return "phenoscape_state_support_in_kb";
	}

	@Override
	public String getName() {
		return "Supporting States in the Phenoscape KB";
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
}
