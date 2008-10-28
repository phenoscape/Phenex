package org.phenoscape.model;

import org.obo.datamodel.OBOClass;
import org.phenoscape.app.AbstractPropertyChangeObject;

public class Specimen extends AbstractPropertyChangeObject {

    private OBOClass collectionCode;
    private String catalogID;
    public static String COLLECTION_CODE = "collectionCode";
    public static String CATALOG_ID  = "catalogID";
    
    public Specimen() {
        super();
    }

    /**
     * Create a new specimen with the same collection code and 
     * catalog ID as the given specimen.
     */
    public Specimen(Specimen template) {
        this.collectionCode = template.getCollectionCode();
        this.catalogID = template.getCatalogID();
    }

    public OBOClass getCollectionCode() {
        return this.collectionCode;
    }
    
    public void setCollectionCode(OBOClass collectionCode) {
        final OBOClass oldValue = this.collectionCode;
        this.collectionCode = collectionCode;
        this.firePropertyChange("collectionCode", oldValue, collectionCode);
    }
    
    public String getCatalogID() {
        return this.catalogID;
    }
    
    public void setCatalogID(String catalogID) {
        final String oldValue = this.catalogID;
        this.catalogID = catalogID;
        this.firePropertyChange("catalogID", oldValue, catalogID);
    }

    public String toString() {
        final StringBuffer buffer = new StringBuffer();
        if (this.collectionCode != null) {
            buffer.append(this.collectionCode.getName());
            buffer.append(" ");
        }
        if (this.catalogID != null) { buffer.append(this.catalogID); }
        return buffer.toString();
    }
    
    public Class<?> getClass(String propertyKey) throws UndefinedKeyException {
        if (propertyKey.equals(COLLECTION_CODE)) {
            return OBOClass.class;
        } else if (propertyKey.equals(CATALOG_ID)) {
            return String.class;
        } else {
            return super.getClass(propertyKey);
        }
    }

}
