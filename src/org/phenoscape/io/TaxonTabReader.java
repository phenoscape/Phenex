package org.phenoscape.io;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOObject;
import org.obo.datamodel.OBOSession;
import org.phenoscape.model.Specimen;
import org.phenoscape.model.Taxon;
import org.phenoscape.model.TermSet;

import phenote.util.Collections;

public class TaxonTabReader {
  
  private final OBOSession session;
  private final TermSet collections;
  private final List<Taxon> taxa = new ArrayList<Taxon>();
  
  public TaxonTabReader(File aFile, OBOSession session, TermSet collections) throws IOException {
    this.session = session;
    this.collections = collections;
    this.parse(aFile);
  }
  
  public List<Taxon> getTaxa() {
    return this.taxa;
  }
  
  private void parse(File aFile) throws IOException {
    final LineNumberReader reader = new LineNumberReader(new FileReader(aFile));
    final String header = reader.readLine();
    final List<String> fields = Arrays.asList(header.split("\t"));
    while (true) {
      final String line = reader.readLine();
      if (line == null) break;
      if (line.trim().equals("")) continue;
      final Taxon taxon = new Taxon();
      final String[] cells = line.split("\t", -1);
      final String taxonID = Collections.get(cells, fields.indexOf("Valid Taxon ID"));
      taxon.setValidName((OBOClass)(session.getObject(taxonID)));
      final String pubName = Collections.get(cells, fields.indexOf("Publication Taxon"));
      taxon.setPublicationName(pubName);
      final String comments = Collections.get(cells, fields.indexOf("Taxon Comments"));
      taxon.setComment(comments);
      final String specimensString = Collections.get(cells, fields.indexOf("Specimens"));
      if ((specimensString != null) && (!specimensString.equals(""))) {
        final String[] specimens = specimensString.split(",");
        for (String specimenString : specimens) {
          final Specimen specimen = new Specimen();
          String adjustedString = specimenString;
          if (specimenString.startsWith("\"")) { adjustedString = adjustedString.split("\"")[1]; }
          final String[] pieces = adjustedString.split(" ");
          if (pieces.length > 0) {
            final String collectionName = pieces[0];
            String collectionID = null;
            for (OBOObject term : collections.getTerms()) {
              if (term.getName().equals(collectionName)) {
                collectionID = term.getID();
                break;
              }
            }
            specimen.setCollectionCode((OBOClass)(session.getObject(collectionID)));
          }
          if (pieces.length > 1) {
            final String catalogNum = pieces[1];
            specimen.setCatalogID(catalogNum);
          }
          taxon.addSpecimen(specimen);
        }
      }
      
      this.taxa.add(taxon);
    }
  }
  
}
