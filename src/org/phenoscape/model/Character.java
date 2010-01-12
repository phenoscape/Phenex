package org.phenoscape.model;

import java.util.UUID;

import org.apache.commons.lang.ObjectUtils;
import org.obo.app.model.AbstractPropertyChangeObject;
import org.obo.app.model.ObservableEventList;

import ca.odell.glazedlists.BasicEventList;

/**
 * A Character is a unit of evolutionary change, and represents a column in an evolutionary character matrix.
 * A Character has zero or more States.
 * @author Jim Balhoff
 */
public class Character extends AbstractPropertyChangeObject {

    public static final String COMMENT = "comment";
    public static final String LABEL = "label";
    public static final String FIGURE = "figure";
    private final String nexmlID;
    private final String statesNexmlID;
    private final ObservableEventList<State> states = new ObservableEventList<State>(new BasicEventList<State>());
    private String label;
    private String comment;
    private String figure;

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

    public String getFigure() {
        return this.figure;
    }

    public void setFigure(String aFigure) {
        if (ObjectUtils.equals(this.figure, aFigure)) return;
        final String oldValue = this.figure;
        this.figure = aFigure;
        this.firePropertyChange(FIGURE, oldValue, aFigure);
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
        } else if (propertyKey.equals(FIGURE)) {
            return String.class;
        } else {
            return super.getClass(propertyKey);
        }
    }

}
