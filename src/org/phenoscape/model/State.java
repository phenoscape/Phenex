package org.phenoscape.model;

import java.util.UUID;

import org.phenoscape.app.AbstractPropertyChangeObject;
import org.phenoscape.util.ObservableEventList;

import ca.odell.glazedlists.BasicEventList;

/**
 * A State is a possible evolutionary state for a Character, and represents a possible cell 
 * value in an evolutionary character matrix.
 * A State is described by free text as well as zero or more ontology-based Phenotypes.
 * @author Jim Balhoff
 */
public class State extends AbstractPropertyChangeObject {

    public static final String COMMENT = "comment";
    public static final String SYMBOL = "symbol";
    public static final String LABEL = "label";
    private final String nexmlID;
    private final ObservableEventList<Phenotype> phenotypes = new ObservableEventList<Phenotype>(new BasicEventList<Phenotype>());
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

    public ObservableEventList<Phenotype> getPhenotypes() {
        return this.phenotypes;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String aLabel) {
        final String oldValue = this.label;
        this.label = aLabel;
        this.firePropertyChange(LABEL, oldValue, aLabel);
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
        final String oldValue = this.symbol;
        this.symbol = aSymbol;
        this.firePropertyChange(SYMBOL, oldValue, aSymbol);
    }

    public String getComment() {
        return this.comment;
    }

    public void setComment(String notes) {
        final String oldValue = this.comment;
        this.comment = notes;
        this.firePropertyChange(COMMENT, oldValue, notes);
    }

    public String toString() {
        final String prefix = this.getSymbol() != null ? this.getSymbol() + " - " : "";
        final String suffix = this.getLabel() != null ? this.getLabel() : "untitled";
        return prefix + suffix;
    }

    @Override
    public Class<?> getClass(String propertyKey) throws UndefinedKeyException {
        if (propertyKey.equals(LABEL)) {
            return String.class;
        } else if (propertyKey.equals(SYMBOL)) {
            return String.class;
        } else if (propertyKey.equals(COMMENT)) {
            return String.class;
        } else {
            return super.getClass(propertyKey);
        }
    }

}
