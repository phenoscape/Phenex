package org.phenoscape.model;

import org.apache.commons.lang.ObjectUtils;
import org.obo.app.model.AbstractPropertyChangeObject;
import org.obo.datamodel.OBOClass;

public class Specimen extends AbstractPropertyChangeObject {

	private OBOClass collectionCode;
	private String catalogID;
	private String comment;
	public static String COLLECTION_CODE = "collectionCode";
	public static String CATALOG_ID  = "catalogID";
	public static final String COMMENT = "comment";

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
		this.firePropertyChange(COLLECTION_CODE, oldValue, collectionCode);
	}

	public String getCatalogID() {
		return this.catalogID;
	}

	public void setCatalogID(String catalogID) {
		if (ObjectUtils.equals(this.catalogID, catalogID)) return;
		final String oldValue = this.catalogID;
		this.catalogID = catalogID;
		this.firePropertyChange(CATALOG_ID, oldValue, catalogID);
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
		} else if (propertyKey.equals(COMMENT)) {
			return String.class;
		} else {
			return super.getClass(propertyKey);
		}
	}

}
