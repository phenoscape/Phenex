package org.phenoscape.model;

import java.util.UUID;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

/**
 * A Character is a unit of evolutionary change, and represents a column in an evolutionary character matrix.
 * A Character has zero or more States.
 * @author Jim Balhoff
 */
public class Character {
  
  private final String nexmlID;
  private final String statesNexmlID;
  private final EventList<State> states = new BasicEventList<State>();
  private String label;
  private String comment;
  
  public Character() {
    this(UUID.randomUUID().toString(), UUID.randomUUID().toString());
  }
  
  public Character(String nexmlID) {
    this(nexmlID, UUID.randomUUID().toString());
  }

  public Character(String nexmlID, String statesNexmlID) {
    this.nexmlID = nexmlID;
    this.statesNexmlID = statesNexmlID;
  }
  
  public String getNexmlID() {
    return this.nexmlID;
  }
  
  public String getStatesNexmlID() {
    return this.statesNexmlID;
  }
  
  public State newState() {
    final State newState = new State();
    this.addState(newState);
    return newState;
  }
  
  public void addState(State aState) {
    this.states.add(aState);
  }
  
  public void removeState(State aState) {
    this.states.remove(aState);
  }
  
  public EventList<State> getStates() {
    return this.states;
  }
  
  public String getLabel() {
    return label;
  }

  public void setLabel(String aLabel) {
    this.label = aLabel;
  }
  
  public String getComment() {
    return this.comment;
  }
  
  public void setComment(String notes) {
    this.comment = notes;
  }
  
  public String toString() {
    return this.getLabel() != null ? this.getLabel() : "untitled";
  }
  
}
