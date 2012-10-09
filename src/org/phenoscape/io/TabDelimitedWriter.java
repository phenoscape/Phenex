package org.phenoscape.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.obo.annotation.base.OBOUtil;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.TermSubset;
import org.phenoscape.model.Character;
import org.phenoscape.model.DataSet;
import org.phenoscape.model.Phenotype;
import org.phenoscape.model.Specimen;
import org.phenoscape.model.State;
import org.phenoscape.model.Taxon;

import ca.odell.glazedlists.SortedList;

import com.eekboom.utils.Strings;

public class TabDelimitedWriter {

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
		int i = 0;
		for (Character character : this.data.getCharacters()) {
			i++;
			if (character.getStates().size() == 0) {
				this.writeCharacter(character, i, writer);
				writer.newLine();
				continue;
			}
			final List<State> sortedStates = new SortedList<State>(character.getStates(), new Comparator<State>() {
				@Override
				public int compare(State o1, State o2) {
					return Strings.compareNatural(o1.getSymbol(), o2.getSymbol());
				}});
			for (State state : sortedStates) {
				if (state.getPhenotypes().size() == 0) {
					this.writeCharacter(character, i, writer);
					writer.write("\t");
					this.writeState(state, writer);
					writer.newLine();
					continue;
				}
				for (Phenotype phenotype : state.getPhenotypes()) {
					this.writeCharacter(character, i, writer);
					writer.write("\t");
					this.writeState(state, writer);
					writer.write("\t");
					if (phenotype.getEntity() != null) {
						writer.write(phenotype.getEntity().getID());
						writer.write("\t");
						writer.write(phenotype.getEntity().getName());
					} else {
						writer.write("\t");
					}
					writer.write("\t");
					if (phenotype.getQuality() != null) {
						writer.write(phenotype.getQuality().getID());
						writer.write("\t");
						writer.write(phenotype.getQuality().getName());
					} else {
						writer.write("\t");
					}
					writer.write("\t");
					if (phenotype.getQuality() != null) {
						final OBOClass characterAttribute = this.getCharacterAttributeForValue(phenotype.getQuality());
						writer.write(characterAttribute.getID());
						writer.write("\t");
						writer.write(characterAttribute.getName());
					} else {
						writer.write("\t");
					}
					writer.write("\t");
					if (phenotype.getRelatedEntity() != null) {
						writer.write(phenotype.getRelatedEntity().getID());
						writer.write("\t");
						writer.write(phenotype.getRelatedEntity().getName());
					} else {
						writer.write("\t");
					}
					writer.write("\t");
					writer.write(phenotype.getCount() != null ? phenotype.getCount().toString() : "");
					writer.write("\t");
					writer.write(StringUtils.defaultString(phenotype.getComment()));
					writer.newLine();
				}
			}
		}
		writer.close();
	}

	private void writeCharacter(Character character, int index, Writer writer) throws IOException {
		writer.write(index + "");
		writer.write("\t");
		writer.write(StringUtils.defaultString(character.getLabel()));
		writer.write("\t");
		writer.write(StringUtils.defaultString(character.getComment()));
	}

	private void writeState(State state, Writer writer) throws IOException {
		writer.write(StringUtils.defaultString(state.getSymbol()));
		writer.write("\t");
		writer.write(StringUtils.defaultString(state.getLabel()));
		writer.write("\t");
		writer.write(StringUtils.defaultString(state.getComment()));
	}

	private String getTaxonHeader() {
		return "Publication Taxon\tValid Taxon\tValid Taxon label\tMatrix Taxon\tTaxon Comment\tSpecimens\t" + this.getColumnHeadings();
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
			sb.append(taxon.getValidName().getID());
			sb.append("\t");
			sb.append(taxon.getValidName().getName());
		} else {
			sb.append("\t");
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
		return "Character Number\tCharacter Description\tCharacter Comment\tState Number\tState Description\tState Comment\tEntity\tEntity label\tQuality\tQuality label\tCharacter Attribute\tAttribute label\tRelated Entity\tRelated Entity label\tCount\tComment";
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
			final OBOClass parent = OBOUtil.getIsaParentForTerm(valueTerm);
			if (parent != null) {
				return getCharacterAttributeForValue(parent);
			} else {
				return valueTerm;
			}
		}
	}

}
