package org.phenoscape.model;

public class MatrixCellValue {
    
    private final Taxon taxon;
    private final Character character;
    private final State state;
    
    public MatrixCellValue(Taxon taxon, Character character, State state) {
        this.taxon = taxon;
        this.character = character;
        this.state = state;
    }

    public Taxon getTaxon() {
        return this.taxon;
    }

    public Character getCharacter() {
        return this.character;
    }

    public State getState() {
        return this.state;
    }

}
