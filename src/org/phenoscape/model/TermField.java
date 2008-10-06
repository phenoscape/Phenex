package org.phenoscape.model;

public enum TermField {
  
  TAXON ("Taxon", "http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/taxonomy/teleost_taxonomy.obo"),
  ENTITY ("Entity", "http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/anatomy/gross_anatomy/animal_gross_anatomy/fish/teleost_anatomy.obo http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/anatomy/caro/spatial.obo"),
  QUALITY ("Quality", "http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/phenotype/quality.obo"),
  RELATED_ENTITY ("Related Entity", "http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/anatomy/gross_anatomy/animal_gross_anatomy/fish/teleost_anatomy.obo"),
  UNIT ("Unit", "http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/phenotype/unit.obo");

  private final String displayName;
  private final String defaultURLs;
  
  TermField(String displayName, String defaultURLs) {
    this.displayName = displayName;
    this.defaultURLs = defaultURLs;
  }
  
  public String displayName() {
    return this.displayName;
  }
  
  public String defaultURLs() {
    return this.defaultURLs;
  }
  
}
