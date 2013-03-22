package org.phenoscape.view;

import org.bbop.framework.AbstractComponentFactory;
import org.phenoscape.controller.PhenexController;

public class AnnotationCheckerComponentFactory extends AbstractComponentFactory<AnnotationCheckerComponent> {

	private final PhenexController controller;

	public AnnotationCheckerComponentFactory(PhenexController controller) {
		super();
		this.controller = controller;
	}

	@Override
	public FactoryCategory getCategory() {
		return FactoryCategory.ANNOTATION;
	}

	@Override
	public String getID() {
		return "phenoscape_annotation_checker_view";
	}

	@Override
	public String getName() {
		return "Annotation Checker";
	}

	@Override
	public AnnotationCheckerComponent doCreateComponent(String id) {
		return new AnnotationCheckerComponent(id, this.controller);
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}
