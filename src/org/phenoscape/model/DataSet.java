package org.phenoscape.model;

import java.util.HashMap;
import java.util.Map;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

public class DataSet {
  
  private final EventList<Character> characters = new BasicEventList<Character>();
  private final EventList<Taxon> taxa = new BasicEventList<Taxon>();
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
  
  public EventList<Character> getCharacters() {
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
  
  public EventList<Taxon> getTaxa() {
    return this.taxa;
  }
  
  public String getPublication() {
    return this.publication;
  }
  
  public void setPublication(String aPublication) {
    this.publication = aPublication;
  }

  public String getPublicationNotes() {
    return this.publicationNotes;
  }

  public void setPublicationNotes(String publicationNotes) {
    this.publicationNotes = publicationNotes;
  }

  public String getCurators() {
    return this.curators;
  }

  public void setCurators(String curators) {
    this.curators = curators;
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
    final Map<String, String> states;
    if (this.matrix.containsKey(taxon.getNexmlID())) {
      states = this.matrix.get(taxon.getNexmlID());
    } else {
      states = new HashMap<String, String>();
    }
    this.matrix.put(taxon.getNexmlID(), states);
    states.put(character.getNexmlID(), (state != null ? state.getNexmlID() : null));
  }

}
