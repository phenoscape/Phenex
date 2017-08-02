package org.phenoscape.view;

import org.bbop.framework.AbstractComponentFactory;
import org.phenoscape.controller.PhenexController;

public class ConsistencyReviewComponentFactory extends AbstractComponentFactory<ConsistencyReviewComponent> {

	private final PhenexController controller;

	public ConsistencyReviewComponentFactory(PhenexController controller) {
		super();
		this.controller = controller;
	}

	@Override
	public FactoryCategory getCategory() {
		return FactoryCategory.ANNOTATION;
	}

	@Override
	public String getID() {
		return "phenoscape_consistency_review_view";
	}

	@Override
	public String getName() {
		return "Consistency Review Panel";
	}

	@Override
	public ConsistencyReviewComponent doCreateComponent(String id) {
		return new ConsistencyReviewComponent(id, this.controller);
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}
