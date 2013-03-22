package org.phenoscape.util;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.xmlbeans.XmlException;
import org.junit.Assert;
import org.junit.Test;
import org.obo.annotation.base.UserOntologyConfiguration;
import org.obo.datamodel.OBOSession;
import org.obo.datamodel.impl.OBOSessionImpl;
import org.phenoscape.controller.OntologyController;
import org.phenoscape.io.CharacterTabReader;
import org.phenoscape.io.nexml_1_0.NeXMLReader_1_0;
import org.phenoscape.model.DataSet;
import org.phenoscape.model.Phenotype;
import org.phenoscape.model.State;

public class DataMergerTest {

	@Test
	public void mergeCharacters() throws IOException, XmlException {
		final OBOSession session = new OBOSessionImpl();
		final CharacterTabReader reader = new CharacterTabReader(new File("testfiles/CharacterTabReaderTestFile1.tab"), session);
		final NeXMLReader_1_0 nexmlReader = new NeXMLReader_1_0(new File("testfiles/DataMergerTestFile1.xml"), session);
		final DataSet data = nexmlReader.getDataSet();
		Assert.assertNull("Character 2, State 0, should not exist in the original data set", this.findState(data.getCharacters().get(1).getStates(), "0"));
		final Phenotype originalPhenotypeC1S0 = data.getCharacters().get(0).getStates().get(0).getPhenotypes().get(0);
		final int originalPhenotypeCountC2S0 = data.getCharacters().get(1).getStates().get(0).getPhenotypes().size();
		Assert.assertEquals("Character 2, State 1, should not have any Phenotypes", 0, originalPhenotypeCountC2S0);
		DataMerger.mergeCharacters(reader.getDataSet(), data);
		Assert.assertNotNull("Character 2, State 0, should now exist because there is data for it in the tab file", this.findState(data.getCharacters().get(1).getStates(), "0"));
		final Phenotype newPhenotypeC1S0 = data.getCharacters().get(0).getStates().get(0).getPhenotypes().get(0);
		Assert.assertNotSame("The phenotypes for this state should have been replaced with new ones", newPhenotypeC1S0, originalPhenotypeC1S0);
		final int newPhenotypeCountC2S0 = data.getCharacters().get(1).getStates().get(0).getPhenotypes().size();
		Assert.assertEquals("Character 2, State 1, should have had a Phenotype added", 1, newPhenotypeCountC2S0);
	}

	@Test
	public void mergeTaxa() throws XmlException, IOException {
		final OBOSession session = new OntologyController(new UserOntologyConfiguration()).getOBOSession(); //TODO use a custom config with test ontologies
		final NeXMLReader_1_0 nexmlReader4 = new NeXMLReader_1_0(new File("testfiles/DataMergerTestFile4.xml"), session);
		final NeXMLReader_1_0 nexmlReader5 = new NeXMLReader_1_0(new File("testfiles/DataMergerTestFile5.xml"), session);
		final DataSet existingData = nexmlReader4.getDataSet();
		Assert.assertEquals("Three taxa to start out with", 3, existingData.getTaxa().size());
		DataMerger.mergeTaxa(nexmlReader5.getDataSet(), existingData);
		Assert.assertEquals("First taxon was matched by valid name and now has a specimen", 1, existingData.getTaxa().get(0).getSpecimens().size());
		Assert.assertNull("First taxon still has no publication name", existingData.getTaxa().get(0).getPublicationName());
		Assert.assertEquals("Second taxon was matched by publication name and now has a specimen", 1, existingData.getTaxa().get(1).getSpecimens().size());
		Assert.assertNull("Second taxon still has no valid name", existingData.getTaxa().get(1).getValidName());
		Assert.assertNotNull("Third taxon was matched by publication name and now has valid name", existingData.getTaxa().get(2).getValidName());
		Assert.assertEquals("Five taxa after merging", 5, existingData.getTaxa().size());
		Assert.assertEquals("Check fourth taxon's name", "Species 8472", existingData.getTaxa().get(3).getPublicationName());
		Assert.assertEquals("Check fifth taxon's valid name", session.getObject("TTO:10000106"), existingData.getTaxa().get(4).getValidName());
	}

	private  State findState(List<State> states, String symbol) {
		for (State state : states) {
			if (symbol.equals(state.getSymbol())) { return state; }
		}
		return null;
	}

}
