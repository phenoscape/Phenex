package org.phenoscape.model;

import java.util.UUID;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

/**
 * A State is a possible evolutionary state for a Character, and represents a possible cell 
 * value in an evolutionary character matrix.
 * A State is described by free text as well as zero or more ontology-based Phenotypes.
 * @author Jim Balhoff
 */
public class State {
  
  private final String nexmlID;
  private final EventList<Phenotype> phenotypes = new BasicEventList<Phenotype>();
  private String label;
  private String symbol;
  private String comment;
  
  public State() {
   this(UUID.randomUUID().toString()); 
  }
  
  public State(String nexmlID) {
    this.nexmlID = nexmlID;
  }
  
  public String getNexmlID() {
    return this.nexmlID;
  }
  
  public Phenotype newPhenotype() {
    final Phenotype newPhenotype = new Phenotype();
    this.addPhenotype(newPhenotype);
    return newPhenotype;
  }
  
  public void addPhenotype(Phenotype aPhenotype) {
    this.phenotypes.add(aPhenotype);
  }
  
  public void removePhenotype(Phenotype aPhenotype) {
    this.phenotypes.remove(aPhenotype);
  }
  
  public EventList<Phenotype> getPhenotypes() {
    return this.phenotypes;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String aLabel) {
    this.label = aLabel;
  }
  
  /**
   * Returns the symbol used as shorthand for this
   * state in an evolutionary character matrix.
   */
  public String getSymbol() {
    return this.symbol;
  }

  /**
   * Set the shorthand symbol for this state. The symbol should be 
   * an single-character string. A symbol is required for each state.
   */
  public void setSymbol(String aSymbol) {
    this.symbol = aSymbol;
  }
  
  public String getComment() {
    return this.comment;
  }
  
  public void setComment(String notes) {
    this.comment = notes;
  }
  
  public String toString() {
    final String prefix = this.getSymbol() != null ? this.getSymbol() + " - " : "";
    final String suffix = this.getLabel() != null ? this.getLabel() : "untitled";
    return prefix + suffix;
  }
  
}
