package org.phenoscape.model;

import org.apache.commons.lang.ObjectUtils;
import org.obo.app.model.AbstractPropertyChangeObject;
import org.obo.datamodel.OBOClass;

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
        if (ObjectUtils.equals(this.collectionCode, collectionCode)) return;
        final OBOClass oldValue = this.collectionCode;
        this.collectionCode = collectionCode;
        this.firePropertyChange("collectionCode", oldValue, collectionCode);
    }

    public String getCatalogID() {
        return this.catalogID;
    }

    public void setCatalogID(String catalogID) {
        if (ObjectUtils.equals(this.catalogID, catalogID)) return;
        final String oldValue = this.catalogID;
        this.catalogID = catalogID;
        this.firePropertyChange("catalogID", oldValue, catalogID);
    }

    @Override
    public String toString() {
        final StringBuffer buffer = new StringBuffer();
        if (this.collectionCode != null) {
            buffer.append(this.collectionCode.getName());
            buffer.append(" ");
        }
        if (this.catalogID != null) { buffer.append(this.catalogID); }
        return buffer.toString();
    }

    @Override
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
