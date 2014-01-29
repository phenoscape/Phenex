package org.phenoscape.orb;

import java.util.ArrayList;
import java.util.List;

import org.obo.app.model.AbstractPropertyChangeObject;
import org.obo.datamodel.OBOClass;

public class ORBTerm extends AbstractPropertyChangeObject {

	private String label;
	private OBOClass parent;
	private String definition;
	private final List<Synonym> synonyms = new ArrayList<Synonym>();

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDefinition() {
		return definition;
	}

	public void setDefinition(String definition) {
		this.definition = definition;
	}

	public OBOClass getParent() {
		return parent;
	}

	public void setParent(OBOClass parent) {
		this.parent = parent;
	}

	public void addSynonym(Synonym synonym) {
		this.synonyms.add(synonym);
	}

	public void removeSynonym(Synonym synonym) {
		this.synonyms.remove(synonym);
	}

	public List<Synonym> getSynonyms() {
		return this.synonyms;
	}

	public static class Synonym implements Comparable<Synonym> {

		private String label;

		public String getLabel() {
			return this.label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		@Override
		public int compareTo(Synonym other) {
			return this.getLabel().compareTo(other.getLabel());
		}
		
		@Override
		public String toString() {
			return this.label;
		}

	}

}
