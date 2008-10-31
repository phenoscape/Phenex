package org.phenoscape.model;

import java.util.UUID;

import org.apache.commons.lang.ObjectUtils;
import org.phenoscape.app.AbstractPropertyChangeObject;
import org.phenoscape.app.ObservableEventList;

import ca.odell.glazedlists.BasicEventList;

/**
 * A Character is a unit of evolutionary change, and represents a column in an evolutionary character matrix.
 * A Character has zero or more States.
 * @author Jim Balhoff
 */
public class Character extends AbstractPropertyChangeObject {

    public static final String COMMENT = "comment";
    public static final String LABEL = "label";
    private final String nexmlID;
    private final String statesNexmlID;
    private final ObservableEventList<State> states = new ObservableEventList<State>(new BasicEventList<State>());
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

    public ObservableEventList<State> getStates() {
        return this.states;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String aLabel) {
        if (ObjectUtils.equals(this.label, aLabel)) return;
        final String oldLabel = this.label;
        this.label = aLabel;
        this.firePropertyChange(LABEL, oldLabel, aLabel);
    }

    public String getComment() {
        return this.comment;
    }

    public void setComment(String notes) {
        if (ObjectUtils.equals(this.comment, notes)) return;
        final String oldValue = this.comment;
        this.comment = notes;
        this.firePropertyChange(COMMENT, oldValue, notes);
    }

    public String toString() {
        return this.getLabel() != null ? this.getLabel() : "untitled";
    }

    @Override
    public Class<?> getClass(String propertyKey) throws UndefinedKeyException {
        if (propertyKey.equals(LABEL)) {
            return String.class;
        } else if (propertyKey.equals(COMMENT)) {
            return String.class;
        } else {
            return super.getClass(propertyKey);
        }
    }
    
}
