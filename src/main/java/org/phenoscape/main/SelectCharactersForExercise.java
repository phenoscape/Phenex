package org.phenoscape.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.xmlbeans.XmlException;
import org.obo.datamodel.OBOSession;
import org.obo.datamodel.impl.OBOSessionImpl;
import org.phenoscape.io.NeXMLReader;
import org.phenoscape.io.NeXMLWriter;
import org.phenoscape.model.Character;
import org.phenoscape.model.DataSet;
import org.phenoscape.model.State;

public class SelectCharactersForExercise {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws XmlException 
	 */
	public static void main(String[] args) throws XmlException, IOException {
		final DataSet combinedData = new DataSet();
		final OBOSession session = new OBOSessionImpl();
		final List<Character> selectedCharacters = new ArrayList<Character>();
		for (File file : new File("matrices").listFiles()) {
			final NeXMLReader reader = new NeXMLReader(file, session);
			final DataSet dataset = reader.getDataSet();
			final String pub = dataset.getPublication();
			final List<Character> characters = dataset.getCharacters();
			for (Character character : characters) {
				character.setComment(pub + ": " + (characters.indexOf(character) + 1));
			}
			final List<Character> shuffledCharacters = new ArrayList<Character>(characters);
			Collections.shuffle(shuffledCharacters);
			selectedCharacters.addAll(shuffledCharacters.subList(0, 29));
		}
		Collections.shuffle(selectedCharacters);
		for (Character character : selectedCharacters) {
			combinedData.addCharacter(copyCharacter(character));
		}
		final NeXMLWriter writer = new NeXMLWriter("b" + UUID.randomUUID().toString());
		writer.setDataSet(combinedData);
		writer.write(new File("combined_matrix.xml"));
		for (Character character : combinedData.getCharacters()) {
			character.setComment(null);
		}
		final NeXMLWriter hiddenPubsWriter = new NeXMLWriter("b" + UUID.randomUUID().toString());
		hiddenPubsWriter.setDataSet(combinedData);
		hiddenPubsWriter.write(new File("combined_matrix_removed_pubs.xml"));
	}

	private static Character copyCharacter(Character character) {
		final Character newCharacter = new Character();
		newCharacter.setLabel(character.getLabel());
		newCharacter.setComment(character.getComment());
		for (State state : character.getStates()) {
			newCharacter.addState(copyState(state));
		}
		return newCharacter;
	}
	
	private static State copyState(State state) {
		final State newState = new State();
		newState.setLabel(state.getLabel());
		newState.setSymbol(state.getSymbol());
		return newState;
	}

}
