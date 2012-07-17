package org.phenoscape.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.phenoscape.model.Character;
import org.phenoscape.model.DataSet;
import org.phenoscape.model.Phenotype;
import org.phenoscape.model.State;

public class BioCreativeTabFormat {

	private final DataSet data;

	public BioCreativeTabFormat(DataSet data) {
		this.data = data;
	}

	public void write(File file) throws IOException {
		final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
		writer.write("CharacterID\t");
		writer.write("StateID\t");
		writer.write("CharacterLabel\t");
		writer.write("StateLabel\t");
		writer.write("EntityLabel\t");
		writer.write("EntityID\t");
		writer.write("QualityLabel\t");
		writer.write("QualityID\t");
		writer.write("RelatedEntityLabel\t");
		writer.write("RelatedEntityID\t");
		writer.write("\n");
		for (Character character : data.getCharacters()) {
			for (State state : character.getStates()) {
				if (state.getPhenotypes().isEmpty()) {
					writer.write(StringUtils.trimToEmpty(character.getNexmlID()));
					writer.write("\t");
					writer.write(StringUtils.trimToEmpty(state.getNexmlID()));
					writer.write("\t");
					writer.write(StringUtils.trimToEmpty(character.getLabel()));
					writer.write("\t");
					writer.write(StringUtils.trimToEmpty(state.getLabel()));
					writer.write("\t");
					writer.write("\t");
					writer.write("\t");
					writer.write("\t");
					writer.write("\t");
					writer.write("\t");
					writer.write("\n");
				} else {
					for (Phenotype phenotype : state.getPhenotypes()) {
						writer.write(StringUtils.trimToEmpty(character.getNexmlID()));
						writer.write("\t");
						writer.write(StringUtils.trimToEmpty(state.getNexmlID()));
						writer.write("\t");
						writer.write(StringUtils.trimToEmpty(character.getLabel()));
						writer.write("\t");
						writer.write(StringUtils.trimToEmpty(state.getLabel()));
						writer.write("\t");
						if (phenotype.getEntity() != null) {
							writer.write(StringUtils.trimToEmpty(phenotype.getEntity().getName()));
						}
						writer.write("\t");
						if (phenotype.getEntity() != null) {
							writer.write(StringUtils.trimToEmpty(phenotype.getEntity().getID()));
						}
						writer.write("\t");
						if (phenotype.getQuality() != null) {
							writer.write(StringUtils.trimToEmpty(phenotype.getQuality().getName()));
						}
						writer.write("\t");
						if (phenotype.getQuality() != null) {
							writer.write(StringUtils.trimToEmpty(phenotype.getQuality().getID()));
						}
						writer.write("\t");
						if (phenotype.getRelatedEntity() != null) {
							writer.write(StringUtils.trimToEmpty(phenotype.getRelatedEntity().getName()));
						}
						writer.write("\t");
						if (phenotype.getRelatedEntity() != null) {
							writer.write(StringUtils.trimToEmpty(phenotype.getRelatedEntity().getID()));
						}
						writer.write("\n");
					}
				}
			}
		}
		writer.close();
	}
	
	@SuppressWarnings("unused")
	private Logger log() {
		return Logger.getLogger(this.getClass());
	}

}
