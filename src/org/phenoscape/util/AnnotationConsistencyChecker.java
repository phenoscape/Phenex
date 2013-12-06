package org.phenoscape.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.obo.annotation.base.OBOUtil;
import org.obo.datamodel.Link;
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
	private static final String BIOLOGICAL_PROCESS = "GO:0008150";
	private static final String PROCESS_QUALITY = "PATO:0001236";

	public AnnotationConsistencyChecker(OBOSession session) {
		this.session = session;
		for (TermSubset subset : session.getSubsets()) {
			if ((subset.getName() != null) && (subset.getName().equals("relational_slim"))) {
				relation_slim = subset;
				break;
			}
		}
	}

	public Collection<ConsistencyIssue> checkCharacter(Character character) {
		final Collection<ConsistencyIssue> issues = new ArrayList<ConsistencyIssue>();
		final Set<OBOClass> charactersUsed = new HashSet<OBOClass>();
		for (State state : character.getStates()) {
			issues.addAll(this.checkState(state, character));
			for (Phenotype phenotype : state.getPhenotypes()) {
				if (phenotype.getQuality() != null) {
					charactersUsed.add(OBOUtil.getCharacterForValue(phenotype.getQuality()));
				}
			}
		}
		if (charactersUsed.size() > 1) {
			if (character.getLabel().startsWith("Scapulocoracoid, anterior margin")) {
				System.err.println(charactersUsed);
			}
			issues.add(new ConsistencyIssue(character, null, "Qualities used descend from multiple character qualities."));
		}
		return issues;
	}

	public Collection<ConsistencyIssue> checkState(State state, Character character) {
		final Collection<ConsistencyIssue> issues = new ArrayList<ConsistencyIssue>();
		if (state.getPhenotypes().isEmpty()) {
			issues.add(new ConsistencyIssue(character, state, "State not annotated."));
		}
		for (Phenotype phenotype : state.getPhenotypes()) {
			issues.addAll(this.checkPhenotype(phenotype, state, character));
		}
		return issues;
	}

	public Collection<ConsistencyIssue> checkPhenotype(Phenotype phenotype, State state, Character character) {
		final Collection<ConsistencyIssue> issues = new ArrayList<ConsistencyIssue>();
		if (phenotype.getEntity() == null) {
			issues.add(new ConsistencyIssue(character, state, "No entity has been entered."));
		} else {
			if (isPostCompositionWithMultipleDifferentiae(phenotype.getEntity())) {
				issues.add(new ConsistencyIssue(character, state, "Entity post-composition used with more than one differentia (may be okay)."));
			}
		}
		if (phenotype.getQuality() == null) {
			issues.add(new ConsistencyIssue(character, state, "No quality has been entered."));

		} else {
			if (isPostCompositionWithMultipleDifferentiae(phenotype.getQuality())) {
				issues.add(new ConsistencyIssue(character, state, "Quality post-composition used with more than one differentia (may be okay)."));
			}
		}
		if (phenotype.getRelatedEntity() != null) {
			if (isPostCompositionWithMultipleDifferentiae(phenotype.getRelatedEntity())) {
				issues.add(new ConsistencyIssue(character, state, "Related entity post-composition used with more than one differentia (may be okay)."));
			}
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
		final OBOClass biologicalProcess = (OBOClass)(this.session.getObject(BIOLOGICAL_PROCESS));
		final OBOClass processQuality = (OBOClass)(this.session.getObject(PROCESS_QUALITY));
		if ((phenotype.getEntity() != null) && (phenotype.getQuality() != null)) {
			if ((biologicalProcess != null) && (processQuality != null) && (TermUtil.hasIsAAncestor(phenotype.getEntity(), biologicalProcess) || (BIOLOGICAL_PROCESS.equals(phenotype.getEntity().getID())))) {
				if (!(TermUtil.hasIsAAncestor(phenotype.getQuality(), processQuality) || (PROCESS_QUALITY.equals(phenotype.getQuality().getID())))) {
					issues.add(new ConsistencyIssue(character, state, "Biological process entities require a process quality."));
				}
			}
			if ((biologicalProcess != null) && (processQuality != null) && (TermUtil.hasIsAAncestor(phenotype.getQuality(), processQuality) || (PROCESS_QUALITY.equals(phenotype.getQuality().getID())))) {
				if (!(TermUtil.hasIsAAncestor(phenotype.getEntity(), biologicalProcess) || (BIOLOGICAL_PROCESS.equals(phenotype.getEntity().getID())))) {
					issues.add(new ConsistencyIssue(character, state, "Process qualities should only be used with biological process entities."));
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

	private boolean isPostCompositionWithMultipleDifferentiae(OBOClass term) {
		if (OBOUtil.isPostCompTerm(term)) {
			final List<Link> differentiae = OBOUtil.getAllDifferentia(term);
			if (differentiae.size() > 1) {
				return true;
			} else {
				return isPostCompositionWithMultipleDifferentiae((OBOClass)(differentiae.get(0).getParent()));
			}
		} else {
			return false;
		}
	}

}
