package org.phenoscape.model;

public class Association {

	private final String taxonID;
	private final String characterID;
	private final String stateID;

	public Association(final String taxonID, final String characterID, final String stateID) {
		this.taxonID = taxonID;
		this.characterID = characterID;
		this.stateID = stateID;
	}

	public String getTaxonID() {
		return taxonID;
	}

	public String getCharacterID() {
		return characterID;
	}

	public String getStateID() {
		return stateID;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((characterID == null) ? 0 : characterID.hashCode());
		result = prime * result + ((stateID == null) ? 0 : stateID.hashCode());
		result = prime * result + ((taxonID == null) ? 0 : taxonID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Association other = (Association) obj;
		if (characterID == null) {
			if (other.characterID != null)
				return false;
		} else if (!characterID.equals(other.characterID))
			return false;
		if (stateID == null) {
			if (other.stateID != null)
				return false;
		} else if (!stateID.equals(other.stateID))
			return false;
		if (taxonID == null) {
			if (other.taxonID != null)
				return false;
		} else if (!taxonID.equals(other.taxonID))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Association [taxonID=" + taxonID + ", characterID=" + characterID + ", stateID=" + stateID + "]";
	}

}
