package org.phenoscape.model;

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
    /**
     * The matrix is represented as a map using this format:
     * <taxonID, Map<characterID, stateID>>
     */
    private final Map<String, Map<String, String>> matrix = new HashMap<String, Map<String, String>>();
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

    public Map<String, Map<String, String>> getMatrixData() {
        return this.matrix;
    }

    public void setMatrixData(Map<String, Map<String, String>> aMatrix) {
        this.matrix.clear();
        this.matrix.putAll(aMatrix);
    }

    public State getStateForTaxon(Taxon taxon, Character character) {
        final Map<String, String> states = this.matrix.get(taxon.getNexmlID());
        if (states != null) {
            final String stateID = states.get(character.getNexmlID());
            for (State state : character.getStates()) {
                if (state.getNexmlID().equals(stateID)) { return state; }
            }
        }
        return null;
    }

    public void setStateForTaxon(Taxon taxon, Character character, State state) {
        if (taxon == null || character == null) { return; }
        if (ObjectUtils.equals(this.getStateForTaxon(taxon, character), state)) {
            return;
        }
        final MatrixCellValue oldValue = new MatrixCellValue(taxon, character, this.getStateForTaxon(taxon, character));
        final Map<String, String> states;
        if (this.matrix.containsKey(taxon.getNexmlID())) {
            states = this.matrix.get(taxon.getNexmlID());
        } else {
            states = new HashMap<String, String>();
        }
        this.matrix.put(taxon.getNexmlID(), states);
        states.put(character.getNexmlID(), (state != null ? state.getNexmlID() : null));
        this.firePropertyChange(MATRIX_CELL, oldValue, new MatrixCellValue(taxon, character, state));
    }
    
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
