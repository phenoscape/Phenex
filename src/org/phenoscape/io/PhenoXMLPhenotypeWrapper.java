package org.phenoscape.io;

import org.obo.datamodel.OBOClass;
import org.phenoscape.model.Phenotype;

import phenote.datamodel.PhenotypeCharacterI;

public class PhenoXMLPhenotypeWrapper implements PhenotypeCharacterI {

  private final Phenotype phenotype;

  public PhenoXMLPhenotypeWrapper(Phenotype phenotype) {
    this.phenotype = phenotype;
  }

  public OBOClass getAdditionalEntity() {
    return this.phenotype.getRelatedEntity();
  }

  public void setAdditionalEntity(OBOClass term) {
    this.phenotype.setRelatedEntity(term);
  }

  public int getCount() {
    return this.phenotype.getCount();
  }

  public void setCount(int count) {
    this.phenotype.setCount(count);
  }

  public String getDescription() {
    return this.phenotype.getComment();
  }

  public void setDescription(String desc) {
    this.phenotype.setComment(desc);
  }

  public OBOClass getEntity() {
    return this.phenotype.getEntity();
  }

  public void setEntity(OBOClass term) {
    this.phenotype.setEntity(term);
  }

  public float getMeasurement() {
    return this.phenotype.getMeasurement();
  }

  public void setMeasurement(float measurement) {
    this.phenotype.setMeasurement(measurement);
  }

  public OBOClass getQuality() {
    return this.phenotype.getQuality();
  }

  public void setQuality(OBOClass term) {
    this.phenotype.setQuality(term);
  }

  public OBOClass getUnit() {
    return this.phenotype.getUnit();
  }

  public void setUnit(OBOClass term) {
    this.phenotype.setUnit(term);
  }

  public boolean hasCount() {
    return this.phenotype.getCount() != null;
  }

  public boolean hasMeasurement() {
    return this.phenotype.getMeasurement() != null;
  }
  
  public Phenotype getPhenotype() {
    return this.phenotype;
  }
  
  public static class PhenotypeWrapperFactory implements PhenotypeCharacterI.PhenotypeCharacterFactory {

    public PhenotypeCharacterI newPhenotypeCharacter() {
      return new PhenoXMLPhenotypeWrapper(new Phenotype());
    }
    
  }

}
