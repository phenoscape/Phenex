package org.phenoscape.view;

import org.bbop.framework.AbstractComponentFactory;
import org.phenoscape.controller.PhenexController;

public class CharacterTableComponentFactory extends AbstractComponentFactory<CharacterTableComponent> {

	private final PhenexController controller;

	public CharacterTableComponentFactory(PhenexController controller) {
		this.controller = controller;
	}

	@Override
	public CharacterTableComponent doCreateComponent(String id) {
		return new CharacterTableComponent(id, this.controller);
	}

	@Override
	public FactoryCategory getCategory() {
		return FactoryCategory.ANNOTATION;
	}

	@Override
	public String getID() {
		return "phenoscape_characters_browser";
	}

	@Override
	public String getName() {
		return "Characters";
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}
