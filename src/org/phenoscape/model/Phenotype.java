package org.phenoscape.model;

import org.obo.datamodel.OBOClass;

/**
 * An ontological description of a phenotype, using the EQ format.
 * @author Jim Balhoff
 */
public class Phenotype {
  
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
    this.entity = entity;
  }
  
  public OBOClass getQuality() {
    return this.quality;
  }
  
  public void setQuality(OBOClass quality) {
    this.quality = quality;
  }
  
  public OBOClass getRelatedEntity() {
    return this.relatedEntity;
  }
  
  public void setRelatedEntity(OBOClass relatedEntity) {
    this.relatedEntity = relatedEntity;
  }
  
  public Integer getCount() {
    return this.count;
  }
  
  public void setCount(Integer count) {
    this.count = count;
  }
  
  public Float getMeasurement() {
    return this.measurement;
  }
  
  public void setMeasurement(Float measurement) {
    this.measurement = measurement;
  }
  
  public OBOClass getUnit() {
    return this.unit;
  }
  
  public void setUnit(OBOClass unit) {
    this.unit = unit;
  }
  
  public String getComment() {
    return this.comment;
  }
  
  public void setComment(String notes) {
    this.comment = notes;
  }
  
}
