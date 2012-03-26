package org.phenoscape.view;

import org.bbop.framework.AbstractComponentFactory;
import org.phenoscape.controller.PhenexController;

public class PhenotypeProposalComponentFactory extends AbstractComponentFactory<PhenotypeProposalComponent> {
	
	private final PhenexController controller;
	
	public PhenotypeProposalComponentFactory(PhenexController controller) {
		this.controller = controller;
	}

	@Override
	public FactoryCategory getCategory() {
		return FactoryCategory.ANNOTATION;
	}

	@Override
	public String getID() {
		return "phenoscape_phenotype_proposal_view";
	}

	@Override
	public String getName() {
		return "Phenotype Proposals";
	}

	@Override
	public PhenotypeProposalComponent doCreateComponent(String id) {
		return new PhenotypeProposalComponent(id, this.controller);
	}

}
