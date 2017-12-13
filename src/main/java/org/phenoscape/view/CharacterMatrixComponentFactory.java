package org.phenoscape.view;

import org.bbop.framework.AbstractComponentFactory;
import org.phenoscape.controller.PhenexController;

public class CharacterMatrixComponentFactory extends AbstractComponentFactory<CharacterMatrixComponent> {

	private final PhenexController controller;

	public CharacterMatrixComponentFactory(PhenexController controller) {
		super();
		this.controller = controller;
	}

	@Override
	public CharacterMatrixComponent doCreateComponent(String id) {
		return new CharacterMatrixComponent(id, this.controller);
	}

	@Override
	public FactoryCategory getCategory() {
		return FactoryCategory.ANNOTATION;
	}

	@Override
	public String getID() {
		return "phenoscape_character_matrix";
	}

	@Override
	public String getName() {
		return "Matrix";
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}
