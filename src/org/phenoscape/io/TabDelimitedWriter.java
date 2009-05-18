package org.phenoscape.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.phenoscape.model.Character;
import org.phenoscape.model.DataSet;
import org.phenoscape.model.Phenotype;
import org.phenoscape.model.Specimen;
import org.phenoscape.model.State;
import org.phenoscape.model.Taxon;

import phenote.datamodel.OboUtil;

public class TabDelimitedWriter {

    private static final String TAO = "http://bioportal.bioontology.org/virtual/1110/";
    private static final String PATO = "http://bioportal.bioontology.org/virtual/1107/";
    private DataSet data;

    public void setDataSet(DataSet data) {
        this.data = data;
    }

    public void write(File aFile) throws IOException {
        final BufferedWriter writer = new BufferedWriter(new FileWriter(aFile));
        writer.write(this.getTaxonHeader());
        writer.newLine();
        for (Taxon taxon : this.data.getTaxa()) {
            writer.write(this.getTaxonRow(taxon));
            writer.newLine();
        }
        writer.newLine();
        writer.newLine();
        writer.write(this.getCharacterHeader());
        writer.newLine();
        int i = 1;
        for (Character character : this.data.getCharacters()) {
            writer.write(i + "\t");
            i++;
            writer.write(this.getCharacterRow(character));
            writer.newLine();
        }
        writer.close();
    }

    private String getTaxonHeader() {
        return "Publication Taxon\tValidTaxon\tMatrix Taxon\tSpecimens\t" + this.getColumnHeadings();
    }

    private String getColumnHeadings() {
        final StringBuffer sb = new StringBuffer();
        for (int i = 1; i <= data.getCharacters().size(); i++) {
            sb.append(i);
            if (i < data.getCharacters().size()) sb.append("\t");
        }
        return sb.toString();
    }
    
    private String getTaxonRow(Taxon taxon) {
        final StringBuffer sb = new StringBuffer();
        sb.append(taxon.getPublicationName());
        sb.append("\t");
        sb.append(taxon.getValidName() != null ? taxon.getValidName().getName() : "");
        sb.append("\t");
        sb.append(taxon.getMatrixTaxonName());
        sb.append("\t");
        sb.append(this.getSpecimens(taxon));
        sb.append("\t");
        sb.append(this.getMatrix(taxon));
        return sb.toString();
    }
    
    private String getSpecimens(Taxon taxon) {
        final StringBuffer sb = new StringBuffer();
        boolean first = true;
        for (Specimen specimen : taxon.getSpecimens()) {
            if (!first) sb.append(", ");
            first = false;
            sb.append(specimen.getCollectionCode() != null ? specimen.getCollectionCode().getName() + " " : "");
            sb.append(specimen.getCatalogID());
        }
        return sb.toString();
    }
    
    private String getMatrix(Taxon taxon) {
        final StringBuffer sb = new StringBuffer();
        boolean first = true;
        for (Character character : this.data.getCharacters()) {
            if (!first) sb.append("\t");
            first = false;
            final State state = this.data.getStateForTaxon(taxon, character);
            sb.append(state != null ? state.getSymbol() : "?");
        }
        return sb.toString();
    }
    
    private String getCharacterHeader() {
        return "\tCharacterDescription\t\tState Description\tEntity\tEntity Link\tQuality\tQuality Link\tRelated Entity\tRelated Entity Link\tCount\tCommnent";
    }
    
    private String getCharacterRow(Character character) {
        final StringBuffer sb = new StringBuffer();
        sb.append(character.getLabel());
        sb.append("\t");
        sb.append(this.getStates(character));
        return sb.toString();
    }
    
    private String getStates(Character character) {
        final StringBuffer sb = new StringBuffer();
        boolean first = true;
        for (State state : character.getStates()) {
            if (!first) sb.append("\n\t\t");
            first = false;
            sb.append(state.getSymbol());
            sb.append("\t");
            sb.append(state.getLabel());
            sb.append("\t");
            sb.append(this.getPhenotypes(state));
        }
        return sb.toString();
    }
    
    private String getPhenotypes(State state) {
        final StringBuffer sb = new StringBuffer();
        boolean first = true;
        for (Phenotype phenotype : state.getPhenotypes()) {
            if (!first) sb.append("\n\t\t\t\t");
            first = false;
            if (phenotype.getEntity() != null) {
                sb.append(phenotype.getEntity().getName());
                sb.append("\t");
                if (!OboUtil.isPostCompTerm(phenotype.getEntity())) {
                    sb.append(TAO + phenotype.getEntity().getID());
                }
            } else {
                sb.append("\t");
            }
            sb.append("\t");
            if (phenotype.getQuality() != null) {
                sb.append(phenotype.getQuality().getName());
                sb.append("\t");
                if (!OboUtil.isPostCompTerm(phenotype.getQuality())) {
                    sb.append(PATO + phenotype.getQuality().getID());
                }
            } else {
                sb.append("\t");
            }
            sb.append("\t");
            if (phenotype.getRelatedEntity() != null) {
                sb.append(phenotype.getRelatedEntity().getName());
                sb.append("\t");
                if (!OboUtil.isPostCompTerm(phenotype.getRelatedEntity())) {
                    sb.append(TAO + phenotype.getRelatedEntity().getID());
                }
            } else {
                sb.append("\t");
            }
            sb.append("\t");
            sb.append(phenotype.getCount() != null ? phenotype.getCount() : "");
            sb.append("\t");
            sb.append(phenotype.getComment() != null ? phenotype.getComment() : "");
        }
        return sb.toString();
    }
    
}
