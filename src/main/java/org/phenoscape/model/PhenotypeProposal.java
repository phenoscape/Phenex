package org.phenoscape.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.obo.annotation.base.OBOUtil;
import org.obo.annotation.base.OBOUtil.Differentium;
import org.obo.app.model.AbstractPropertyChangeObject;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOProperty;
import org.obo.datamodel.OBOSession;

public class PhenotypeProposal extends AbstractPropertyChangeObject {

	public static enum ResolvedStatus {
		PROPOSED, ACCEPTED, REJECTED
	}
	private ResolvedStatus status = ResolvedStatus.PROPOSED;
	private final String characterID;
	private final String stateID;
	private String entityText;
	private String entityLocatorText;
	private String qualityText;
	private String qualityModifierText;
	private final List<OBOClass> entities = new ArrayList<OBOClass>();
	private final List<OBOClass> entityLocators = new ArrayList<OBOClass>();
	private final List<OBOClass> qualities = new ArrayList<OBOClass>();
	private final List<OBOClass> qualityModifiers = new ArrayList<OBOClass>();
	private boolean qualityIsNegated;
	private OBOClass negatedQualityParent;

	public PhenotypeProposal(String characterID, String stateID) {
		super();
		this.characterID = characterID;
		this.stateID = stateID;
	}

	public ResolvedStatus getStatus() {
		return this.status;
	}

	public void setStatus(ResolvedStatus status) {
		this.status = status;
	}

	public String getEntityText() {
		return entityText;
	}

	public void setEntityText(String entityText) {
		this.entityText = entityText;
	}

	public String getEntityLocatorText() {
		return entityLocatorText;
	}

	public void setEntityLocatorText(String entityLocatorText) {
		this.entityLocatorText = entityLocatorText;
	}

	public String getQualityText() {
		return qualityText;
	}

	public void setQualityText(String qualityText) {
		this.qualityText = qualityText;
	}

	public String getQualityModifierText() {
		return qualityModifierText;
	}

	public void setQualityModifierText(String qualityModifierText) {
		this.qualityModifierText = qualityModifierText;
	}

	public boolean isQualityIsNegated() {
		return qualityIsNegated;
	}

	public void setQualityIsNegated(boolean qualityIsNegated) {
		this.qualityIsNegated = qualityIsNegated;
	}

	public OBOClass getNegatedQualityParent() {
		return negatedQualityParent;
	}

	public void setNegatedQualityParent(OBOClass negatedQualityParent) {
		this.negatedQualityParent = negatedQualityParent;
	}

	public List<OBOClass> getEntities() {
		return entities;
	}

	public List<OBOClass> getEntityLocators() {
		return entityLocators;
	}

	public List<OBOClass> getQualities() {
		return qualities;
	}
	
	public List<OBOClass> getProcessedQualities(OBOSession session) {
		final List<OBOClass> negatedQualities = new ArrayList<OBOClass>();
		if (this.qualityIsNegated && (!this.qualities.isEmpty()) && (this.getNegatedQualityParent() != null)) {
			for (OBOClass quality : this.getQualities()) {
				final Differentium diff = new Differentium();
				diff.setRelation((OBOProperty)(session.getObject("PHENOSCAPE:complement_of")));
				diff.setTerm(quality);
				negatedQualities.add(OBOUtil.createPostComposition(this.getNegatedQualityParent(), Collections.singletonList(diff)));
			}
			return negatedQualities;
		} else {
			return this.getQualities();
		}
	}

	public List<OBOClass> getQualityModifiers() {
		return qualityModifiers;
	}

	public String getCharacterID() {
		return characterID;
	}

	public String getStateID() {
		return stateID;
	}

}
