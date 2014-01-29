package org.phenoscape.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.obo.annotation.base.OBOUtil;
import org.phenoscape.model.Character;
import org.phenoscape.model.DataSet;
import org.phenoscape.model.Phenotype;
import org.phenoscape.model.State;

public class CharaparserEvaluationTabFormat {

	private final DataSet data;

	public CharaparserEvaluationTabFormat(DataSet data) {
		this.data = data;
	}

	public void write(File file) throws IOException {
		final BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
		writer.write("Character\t");
		writer.write("Character Label\t");
		writer.write("State Symbol\t");
		writer.write("State Label\t");
		writer.write("Entity ID\t");
		writer.write("Entity Label\t");
		writer.write("Quality ID\t");
		writer.write("Quality Label\t");
		writer.write("Related Entity ID\t");
		writer.write("Related Entity Label\t");
		writer.write("\n");
		int characterNumber = 0;
		for (Character character : data.getCharacters()) {
			characterNumber += 1;
			for (State state : character.getStates()) {
				if (state.getPhenotypes().isEmpty()) {
					writer.write("" + characterNumber);
					writer.write("\t");
					writer.write(StringUtils.trimToEmpty(character.getLabel()));
					writer.write("\t");
					writer.write(StringUtils.trimToEmpty(state.getSymbol()));
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
						writer.write("" + characterNumber);
						writer.write("\t");
						writer.write(StringUtils.trimToEmpty(character
								.getLabel()));
						writer.write("\t");
						writer.write(StringUtils.trimToEmpty(state.getSymbol()));
						writer.write("\t");
						writer.write(StringUtils.trimToEmpty(state.getLabel()));
						writer.write("\t");
						if (phenotype.getEntity() != null) {
							writer.write(StringUtils.trimToEmpty(OBOUtil
									.generateManchesterIDExpression(phenotype
											.getEntity())));
						}
						writer.write("\t");
						if (phenotype.getEntity() != null) {
							writer.write(StringUtils.trimToEmpty(OBOUtil
									.generateManchesterLabelExpression(phenotype
											.getEntity())));
						}
						writer.write("\t");
						if (phenotype.getQuality() != null) {
							writer.write(StringUtils.trimToEmpty(OBOUtil
									.generateManchesterIDExpression(phenotype
											.getQuality())));
						}
						writer.write("\t");
						if (phenotype.getQuality() != null) {
							writer.write(StringUtils.trimToEmpty(OBOUtil
									.generateManchesterLabelExpression(phenotype
											.getQuality())));
						}
						writer.write("\t");
						if (phenotype.getRelatedEntity() != null) {
							writer.write(StringUtils.trimToEmpty(OBOUtil
									.generateManchesterIDExpression(phenotype
											.getRelatedEntity())));
						}
						writer.write("\t");
						if (phenotype.getRelatedEntity() != null) {
							writer.write(StringUtils.trimToEmpty(OBOUtil
									.generateManchesterLabelExpression(phenotype
											.getRelatedEntity())));
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
