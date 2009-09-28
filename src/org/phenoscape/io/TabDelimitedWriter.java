package org.phenoscape.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.obo.datamodel.OBOClass;
import org.obo.datamodel.TermSubset;
import org.phenoscape.model.Character;
import org.phenoscape.model.DataSet;
import org.phenoscape.model.Phenotype;
import org.phenoscape.model.Specimen;
import org.phenoscape.model.State;
import org.phenoscape.model.Taxon;

import phenote.datamodel.OboUtil;
import ca.odell.glazedlists.SortedList;

import com.eekboom.utils.Strings;

public class TabDelimitedWriter {

    private static final String TTO = "http://bioportal.bioontology.org/virtual/1081/";
    private static final String TAO = "http://bioportal.bioontology.org/virtual/1110/";
    private static final String PATO = "http://bioportal.bioontology.org/virtual/1107/";
    private static final String LINK_FORMAT = "=HYPERLINK(\"%s\", \"%s\")";
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
            final List<State> sortedStates = new SortedList<State>(character.getStates(), new Comparator<State>() {
                public int compare(State o1, State o2) {
                    return Strings.compareNatural(o1.getSymbol(), o2.getSymbol());
                }});
            for (State state : sortedStates) {
                for (Phenotype phenotype : state.getPhenotypes()) {
                    writer.write(i + "");
                    writer.write("\t");
                    writer.write(character.getLabel());
                    writer.write("\t");
                    writer.write(character.getComment());
                    writer.write("\t");
                    writer.write(state.getSymbol());
                    writer.write("\t");
                    writer.write(state.getLabel());
                    writer.write("\t");
                    writer.write(state.getComment());
                    writer.write("\t");
                    if (phenotype.getEntity() != null) {
                        if (!OboUtil.isPostCompTerm(phenotype.getEntity())) {
                            writer.write(String.format(LINK_FORMAT, TAO + phenotype.getEntity().getID(), phenotype.getEntity().getName()));
                        } else {
                            writer.write(phenotype.getEntity().getName());
                        }
                    }
                    writer.write("\t");
                    if (phenotype.getQuality() != null) {
                        if (!OboUtil.isPostCompTerm(phenotype.getQuality())) {
                            writer.write(String.format(LINK_FORMAT, PATO + phenotype.getQuality().getID(), phenotype.getQuality().getName()));
                        } else {
                            writer.write(phenotype.getQuality().getName());
                        }
                    }
                    writer.write("\t");
                    if (phenotype.getQuality() != null) {
                        final OBOClass characterAttribute = this.getCharacterAttributeForValue(phenotype.getQuality());
                        if (!OboUtil.isPostCompTerm(characterAttribute)) {
                            writer.write(String.format(LINK_FORMAT, PATO + characterAttribute.getID(), characterAttribute.getName()));
                        } else {
                            writer.write(characterAttribute.getName());
                        }
                    }
                    writer.write("\t");
                    if (phenotype.getRelatedEntity() != null) {
                        if (!OboUtil.isPostCompTerm(phenotype.getRelatedEntity())) {
                            writer.write(String.format(LINK_FORMAT, TAO + phenotype.getRelatedEntity().getID(), phenotype.getRelatedEntity().getName()));
                        } else {
                            writer.write(phenotype.getRelatedEntity().getName());
                        }
                    }
                    writer.write("\t");
                    writer.write(phenotype.getCount() != null ? phenotype.getCount().toString() : "");
                    writer.write("\t");
                    writer.write(phenotype.getComment() != null ? phenotype.getComment() : "");
                    writer.newLine();
                }
            }
            i++;
        }
        writer.close();
    }

    private String getTaxonHeader() {
        return "Publication Taxon\tTTO Taxon\tMatrix Taxon\tTaxon Comment\tSpecimens\t" + this.getColumnHeadings();
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
        if (taxon.getValidName() != null) {
            sb.append(String.format(LINK_FORMAT, TTO + taxon.getValidName().getID(), taxon.getValidName().getName()));
        }
        sb.append("\t");
        sb.append(taxon.getMatrixTaxonName());
        sb.append("\t");
        sb.append(taxon.getComment());
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
        return "Character Number\tCharacter Description\tCharacter Comment\tState Number\tState Description\tState Comment\tEntity\tQuality\tCharacter Attribute\tRelated Entity\tCount\tComment";
    }
    
    private OBOClass getCharacterAttributeForValue(OBOClass valueTerm) {
        final Set<TermSubset> categories = valueTerm.getSubsets();
        final Set<String> categoryNames = new HashSet<String>();
        for (TermSubset category : categories) {
            categoryNames.add(category.getName());
        }
        if (categoryNames.contains("character_slim")) {
            return valueTerm;
        } else {
            final OBOClass parent = OboUtil.getIsaParentForTerm(valueTerm);
            if (parent != null) {
                return getCharacterAttributeForValue(parent);
            } else {
                return valueTerm;
            }
        }
    }
    
}
