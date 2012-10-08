package org.phenoscape.io;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import junit.framework.Assert;
import org.apache.xmlbeans.XmlException;
import org.junit.Test;
import org.obo.datamodel.impl.OBOSessionImpl;
import org.phenoscape.model.Character;
import org.phenoscape.model.DataSet;
import org.phenoscape.model.MultipleState;
import org.phenoscape.model.MultipleState.MODE;
import org.phenoscape.model.State;
import org.phenoscape.model.Taxon;

public class TestWritePolymorphismsInBrandNewFile {

	@Test
	public void testWritingPolymorphisms() throws IOException, XmlException {
		final DataSet data = new DataSet();
		final Character character = new Character();
		final State state0 = new State();
		state0.setSymbol("0");
		state0.setLabel("zero");
		final State state1 = new State();
		state1.setSymbol("1");
		state1.setLabel("one");
		character.addState(state0);
		character.addState(state1);
		data.addCharacter(character);
		final Taxon taxon = new Taxon();
		taxon.setPublicationName("taxon");
		data.addTaxon(taxon);
		final Set<State> states = new HashSet<State>();
		states.addAll(character.getStates());
		final MultipleState multiState = new MultipleState(states, MODE.POLYMORPHIC);
		data.setStateForTaxon(taxon, character, multiState);
		final NeXMLWriter writer = new NeXMLWriter(UUID.randomUUID().toString());
		writer.setDataSet(data);
		final StringWriter output = new StringWriter();
		writer.write(output);
		output.close();
		final String text = output.getBuffer().toString();
		System.out.println(text);
		final StringReader input = new StringReader(text);
		final NeXMLReader reader = new NeXMLReader(input, new OBOSessionImpl());
		final DataSet newData = reader.getDataSet();
		final Character newCharacter = newData.getCharacters().get(0);
		final Taxon newTaxon = newData.getTaxa().get(0);
		final State newState = reader.getDataSet().getStateForTaxon(newTaxon, newCharacter);
		Assert.assertNotNull(newState);
		Assert.assertEquals(MultipleState.class, newState.getClass());
	}

}
