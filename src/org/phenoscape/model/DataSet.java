package org.phenoscape.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.obo.app.model.AbstractPropertyChangeObject;
import org.obo.app.model.ObservableEventList;

import ca.odell.glazedlists.BasicEventList;

public class DataSet extends AbstractPropertyChangeObject {

	public static final String CURATORS = "curators";
	public static final String PUBLICATION_NOTES = "publicationNotes";
	public static final String PUBLICATION = "publication";
	public static final String MATRIX_CELL = "matrixCell";
	private final ObservableEventList<Character> characters = new ObservableEventList<Character>(new BasicEventList<Character>());
	private final ObservableEventList<Taxon> taxa = new ObservableEventList<Taxon>(new BasicEventList<Taxon>());
	private final ObservableEventList<Tree> trees = new ObservableEventList<Tree>(new BasicEventList<Tree>());
	/**
	 * The matrix is represented as a map using this format:
	 * <taxonID, Map<characterID, stateID>>
	 */
	private final Map<String, Map<String, State>> matrix = new HashMap<String, Map<String, State>>();
	private String publication;
	private String publicationNotes;
	private String curators;

	public Character newCharacter() {
		final Character newCharacter = new Character();
		this.addCharacter(newCharacter);
		return newCharacter;
	}

	public void addCharacter(Character aCharacter) {
		this.characters.add(aCharacter);
	}

	public void removeCharacter(Character aCharacter) {
		this.characters.remove(aCharacter);
	}

	public void removeCharacters(Collection<Character> characters) {
		this.characters.removeAll(characters);
	}

	public ObservableEventList<Character> getCharacters() {
		return this.characters;
	}

	public Taxon newTaxon() {
		final Taxon newTaxon = new Taxon();
		this.addTaxon(newTaxon);
		return newTaxon;
	}

	public void addTaxon(Taxon aTaxon) {
		this.taxa.add(aTaxon);
	}

	public void removeTaxon(Taxon aTaxon) {
		this.taxa.remove(aTaxon);
	}

	public ObservableEventList<Taxon> getTaxa() {
		return this.taxa;
	}

	public void addTree(Tree aTree) {
		this.trees.add(aTree);
	}

	public ObservableEventList<Tree> getTrees() {
		return this.trees;
	}

	public String getPublication() {
		return this.publication;
	}

	public void setPublication(String aPublication) {
		if (ObjectUtils.equals(this.publication, aPublication)) return;
		final String oldPub = this.publication;
		this.publication = aPublication;
		this.firePropertyChange(PUBLICATION, oldPub, aPublication);
	}

	public String getPublicationNotes() {
		return this.publicationNotes;
	}

	public void setPublicationNotes(String publicationNotes) {
		if (ObjectUtils.equals(this.publicationNotes, publicationNotes)) return;
		final String oldNotes = this.publicationNotes;
		this.publicationNotes = publicationNotes;
		this.firePropertyChange(PUBLICATION_NOTES, oldNotes, publicationNotes);
	}

	public String getCurators() {
		return this.curators;
	}

	public void setCurators(String curators) {
		if (ObjectUtils.equals(this.curators, curators)) return;
		final String oldCurators = this.curators;
		this.curators = curators;
		this.firePropertyChange(CURATORS, oldCurators, curators);
	}

	public Map<String, Map<String, State>> getMatrixData() {
		return this.matrix;
	}

	public void setMatrixData(Map<String, Map<String, State>> aMatrix) {
		this.matrix.clear();
		this.matrix.putAll(aMatrix);
	}

	public State getStateForTaxon(Taxon taxon, Character character) {
		final Map<String, State> states = this.matrix.get(taxon.getNexmlID());
		if (states != null) {
			return states.get(character.getNexmlID());
		}
		return null;
	}

	public void setStateForTaxon(Taxon taxon, Character character, State state) {
		if (taxon == null || character == null) { return; }
		if (ObjectUtils.equals(this.getStateForTaxon(taxon, character), state)) {
			return;
		}
		final MatrixCellValue oldValue = new MatrixCellValue(taxon, character, this.getStateForTaxon(taxon, character));
		final Map<String, State> states;
		if (this.matrix.containsKey(taxon.getNexmlID())) {
			states = this.matrix.get(taxon.getNexmlID());
		} else {
			states = new HashMap<String, State>();
		}
		this.matrix.put(taxon.getNexmlID(), states);
		states.put(character.getNexmlID(), state);
		this.firePropertyChange(MATRIX_CELL, oldValue, new MatrixCellValue(taxon, character, state));
	}    

	@Override
	public Class<?> getClass(String propertyKey) throws UndefinedKeyException {
		if (propertyKey.equals(CURATORS)) {
			return String.class;
		} else if (propertyKey.equals(PUBLICATION)) {
			return String.class;
		} else if (propertyKey.equals(PUBLICATION_NOTES)) {
			return String.class;
		} else if (propertyKey.equals(MATRIX_CELL)) {
			return MatrixCellValue.class;
		} else {
			return super.getClass(propertyKey);
		}
	}

}
