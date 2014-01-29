package org.obo.annotation.view;

import org.bbop.framework.AbstractComponentFactory;

public class TermInfoComponentFactory extends AbstractComponentFactory<TermInfoComponent> {

	private final OntologyCoordinator coordinator;

	public TermInfoComponentFactory(OntologyCoordinator coordinator) {
		this.coordinator = coordinator;
	}

	@Override
	public TermInfoComponent doCreateComponent(String id) {
		return new TermInfoComponent(id, this.coordinator);
	}

	@Override
	public FactoryCategory getCategory() {
		return FactoryCategory.ONTOLOGY;
	}

	@Override
	public String getName() {
		return "Term Info";
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public String getID() {
		return "term-info";
	}

}

