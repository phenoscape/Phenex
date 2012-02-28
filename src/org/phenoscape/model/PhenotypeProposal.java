package org.phenoscape.model;

import java.util.ArrayList;
import java.util.List;

import org.obo.app.model.AbstractPropertyChangeObject;
import org.obo.datamodel.OBOClass;

public class PhenotypeProposal extends AbstractPropertyChangeObject {

	private boolean resolved;
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

	public boolean isResolved() {
		return this.resolved;
	}

	public void setResolved(boolean resolved) {
		this.resolved = resolved;
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
