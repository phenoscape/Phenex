package org.phenoscape.model;

public class MatrixCell {

	private final Taxon taxon;
	private final Character character;

	public MatrixCell(Taxon taxon, Character character) {
		this.taxon = taxon;
		this.character = character;
	}

	public Taxon getTaxon() {
		return this.taxon;
	}

	public Character getCharacter() {
		return this.character;
	}

}
