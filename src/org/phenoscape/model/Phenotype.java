package org.phenoscape.model;

import org.obo.datamodel.OBOClass;
import org.phenoscape.app.AbstractPropertyChangeObject;

/**
 * An ontological description of a phenotype, using the EQ format.
 * @author Jim Balhoff
 */
public class Phenotype extends AbstractPropertyChangeObject {

    public static final String COMMENT = "comment";
    public static final String UNIT = "unit";
    public static final String MEASUREMENT = "measurement";
    public static final String COUNT = "count";
    public static final String RELATED_ENTITY = "relatedEntity";
    public static final String QUALITY = "quality";
    public static final String ENTITY = "entity";
    private OBOClass entity;
    private OBOClass quality;
    private OBOClass relatedEntity;
    private Integer count;
    private Float measurement;
    private OBOClass unit;
    private String comment;

    public OBOClass getEntity() {
        return entity;
    }

    public void setEntity(OBOClass entity) {
        final OBOClass oldValue = this.entity;
        this.entity = entity;
        this.firePropertyChange(ENTITY, oldValue, entity);
    }

    public OBOClass getQuality() {
        return this.quality;
    }

    public void setQuality(OBOClass quality) {
        final OBOClass oldValue = this.quality;
        this.quality = quality;
        this.firePropertyChange(QUALITY, oldValue, quality);
    }

    public OBOClass getRelatedEntity() {
        return this.relatedEntity;
    }

    public void setRelatedEntity(OBOClass relatedEntity) {
        final OBOClass oldValue = relatedEntity;
        this.relatedEntity = relatedEntity;
        this.firePropertyChange(RELATED_ENTITY, oldValue, relatedEntity);
    }

    public Integer getCount() {
        return this.count;
    }

    public void setCount(Integer count) {
        final Integer oldValue = this.count;
        this.count = count;
        this.firePropertyChange(COUNT, oldValue, count);
    }

    public Float getMeasurement() {
        return this.measurement;
    }

    public void setMeasurement(Float measurement) {
        final Float oldValue = this.measurement;
        this.measurement = measurement;
        this.firePropertyChange(MEASUREMENT, oldValue, measurement);
    }

    public OBOClass getUnit() {
        return this.unit;
    }

    public void setUnit(OBOClass unit) {
        final OBOClass oldValue = this.unit;
        this.unit = unit;
        this.firePropertyChange(UNIT, oldValue, unit);
    }

    public String getComment() {
        return this.comment;
    }

    public void setComment(String notes) {
        final String oldValue = this.comment;
        this.comment = notes;
        this.firePropertyChange(COMMENT, oldValue, notes);
    }

    @Override
    public Class<?> getClass(String propertyKey) throws UndefinedKeyException {
        if (propertyKey.equals(ENTITY)) {
            return OBOClass.class;
        } else if (propertyKey.equals(QUALITY)) {
            return OBOClass.class;
        } else if (propertyKey.equals(RELATED_ENTITY)) {
            return OBOClass.class;
        } else if (propertyKey.equals(COUNT)) {
            return Integer.class;
        } else if (propertyKey.equals(MEASUREMENT)) {
            return Float.class;
        } else if (propertyKey.equals(UNIT)) {
            return OBOClass.class;
        } else if (propertyKey.equals(COMMENT)) {
            return String.class;
        } else {
            return super.getClass(propertyKey);
        }
    }

}
