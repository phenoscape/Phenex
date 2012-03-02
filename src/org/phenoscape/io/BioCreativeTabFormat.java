package org.phenoscape.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

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
		for (Character character : data.getCharacters()) {
			for (State state : character.getStates()) {
				for (Phenotype phenotype : state.getPhenotypes()) {
					writer.write(character.getNexmlID());
					writer.write("\t");
					writer.write(state.getNexmlID());
					writer.write("\t");
					writer.write(character.getLabel());
					writer.write("\t");
					writer.write(state.getLabel());
					writer.write("\t");
					if (phenotype.getEntity() != null) {
						writer.write(phenotype.getEntity().getID());
					}
					writer.write("\t");
					if (phenotype.getQuality() != null) {
						writer.write(phenotype.getQuality().getID());
					}
					writer.write("\t");
					if (phenotype.getRelatedEntity() != null) {
						writer.write(phenotype.getRelatedEntity().getID());
					}
					writer.write("\n");
				}
			}
		}
	}

}
