package org.phenoscape.model;

import java.util.UUID;

import org.apache.commons.lang.ObjectUtils;
import org.obo.app.model.AbstractPropertyChangeObject;
import org.obo.app.model.ObservableEventList;
import org.obo.datamodel.OBOClass;

import ca.odell.glazedlists.BasicEventList;

public class Taxon extends AbstractPropertyChangeObject {

    private final String nexmlID;
    private OBOClass validName;
    private String publicationName;
    private String matrixTaxonName;
    private String comment;
    private String figure;
    private final ObservableEventList<Specimen> specimens = new ObservableEventList<Specimen>(new BasicEventList<Specimen>());
    public static String VALID_NAME = "validName";
    public static String PUBLICATION_NAME = "publicationName";
    public static String MATRIX_TAXON_NAME = "matrixTaxonName";
    public static String COMMENT = "comment";
    public static final String FIGURE = "figure";

    public Taxon() {
        this(UUID.randomUUID().toString());
    }

    public Taxon(String nexmlID) {
        this.nexmlID = nexmlID;
    }

    public String getNexmlID() {
        return this.nexmlID;
    }

    public OBOClass getValidName() {
        return this.validName;
    }

    public void setValidName(OBOClass validName) {
        if (ObjectUtils.equals(this.validName, validName)) return;
        final OBOClass oldValue = this.validName;
        this.validName = validName;
        this.firePropertyChange(VALID_NAME, oldValue, validName);
    }

    public String getPublicationName() {
        return this.publicationName;
    }

    public void setPublicationName(String publicationName) {
        if (ObjectUtils.equals(this.publicationName, publicationName)) return;
        final String oldValue = this.publicationName;
        this.publicationName = publicationName;
        this.firePropertyChange(PUBLICATION_NAME, oldValue, publicationName);
    }

    public String getMatrixTaxonName() {
        return this.matrixTaxonName;
    }

    public void setMatrixTaxonName(String matrixName) {
        if (ObjectUtils.equals(this.matrixTaxonName, matrixName)) return;
        final String oldValue = this.matrixTaxonName;
        this.matrixTaxonName = matrixName;
        this.firePropertyChange(MATRIX_TAXON_NAME, oldValue, matrixName);
    }

    public String getComment() {
        return this.comment;
    }

    public void setComment(String comment) {
        if (ObjectUtils.equals(this.comment, comment)) return;
        final String oldValue = this.comment;
        this.comment = comment;
        this.firePropertyChange(COMMENT, oldValue, comment);
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

    public Specimen newSpecimen() {
        final Specimen newSpecimen = new Specimen();
        this.addSpecimen(newSpecimen);
        return newSpecimen;
    }

    public void addSpecimen(Specimen aSpecimen) {
        this.specimens.add(aSpecimen);
    }

    public void removeSpecimen(Specimen aSpecimen) {
        this.specimens.remove(aSpecimen);
    }

    public ObservableEventList<Specimen> getSpecimens() {
        return this.specimens;
    }

    public String toString() {
        return this.getValidName() != null ? this.getValidName().toString() : "untitled";
    }

    /* TODO Work in Progress
    @Override
    public boolean equals(Object o){
    	if(o == null) return false;
    	if(o instanceof Taxon){
    		Taxon otherTaxon = (Taxon)o;
    		if(otherTaxon.getValidName() != null && 
    				otherTaxon.getValidName().getID() != null &&
    				otherTaxon.getValidName().getName() != null){
    			String otherTaxonName = otherTaxon.getValidName().getName();
    			String otherTaxonId = otherTaxon.getValidName().getID();
    			String thisTaxonName = this.getValidName().getName();
    			String thisTaxonId = this.getValidName().getID();
    			
    			if(thisTaxonName.equals(otherTaxonName) && thisTaxonId.equals(otherTaxonId))
    				return true;
    		}
    	}
    	return false;
    }
    */
    public Class<?> getClass(String propertyKey) throws UndefinedKeyException {
        if (propertyKey.equals(VALID_NAME)) {
            return OBOClass.class;
        } else if (propertyKey.equals(PUBLICATION_NAME)) {
            return String.class;
        } else if (propertyKey.equals(MATRIX_TAXON_NAME)) {
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
