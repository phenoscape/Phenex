package org.phenoscape.util;

import java.util.ArrayList;
import java.util.Collection;

import org.obo.datamodel.LinkedObject;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOProperty;
import org.obo.datamodel.OBOSession;
import org.obo.datamodel.TermSubset;
import org.obo.filters.LinkFilter;
import org.obo.filters.LinkFilterImpl;
import org.obo.util.TermUtil;
import org.phenoscape.model.Character;
import org.phenoscape.model.Phenotype;
import org.phenoscape.model.State;

public class AnnotationConsistencyChecker {

	private final OBOSession session;
	private TermSubset relation_slim = null;
	private static final String STRUCTURE = "PATO:0000141";
	private static final String POSITION = "PATO:0000140";
	private static final String SIZE = "PATO:0000117";

	public AnnotationConsistencyChecker(OBOSession session) {
		this.session = session;
		for (TermSubset subset : session.getSubsets()) {
			if ((subset.getName() != null) && (subset.getName().equals("relational_slim"))) {
				relation_slim = subset;
				break;
			}
		}
	}

	public Collection<ConsistencyIssue> checkAnnotation(Character character, State state, Phenotype phenotype) {
		final Collection<ConsistencyIssue> issues = new ArrayList<ConsistencyIssue>();
		if (phenotype.getEntity() == null) {
			issues.add(new ConsistencyIssue(character, state, "No entity has been entered."));
		}
		if (phenotype.getQuality() == null) {
			issues.add(new ConsistencyIssue(character, state, "No quality has been entered."));
		}
		if (relation_slim != null) {
			if ((phenotype.getQuality() != null) && (phenotype.getQuality().getSubsets() != null)) {
				if (phenotype.getQuality().getSubsets().contains(relation_slim)) {
					if (phenotype.getRelatedEntity() == null) {
						issues.add(new ConsistencyIssue(character, state, "Relational quality has been used without a related entity."));
					}
				} else {
					if ((phenotype.getRelatedEntity() != null) && (!this.isOptionallyRelationalQuality(phenotype.getQuality()))) {
						issues.add(new ConsistencyIssue(character, state, "Related entity requires a relational quality."));
					}
				}
			}

		}
		return issues;
	}

	private boolean isOptionallyRelationalQuality(OBOClass quality) {
		if ((quality.getID().equals(STRUCTURE)) || (quality.getID().equals(POSITION))) {
			return true;
		} else {
			final OBOClass size = (OBOClass)(session.getObject(SIZE));
			final LinkFilter filter = new LinkFilterImpl((OBOProperty)(session.getObject("OBO_REL:is_a")));
			final Collection<LinkedObject> sizes = TermUtil.getDescendants(size, true, filter);
			return sizes.contains(quality);
		}
	}

}
