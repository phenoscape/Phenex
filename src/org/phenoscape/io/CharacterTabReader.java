package org.phenoscape.io;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOSession;
import org.phenoscape.model.Character;
import org.phenoscape.model.Phenotype;
import org.phenoscape.model.State;

import phenote.util.Collections;

public class CharacterTabReader {

  private final OBOSession session;
  final Map<Integer, Character> characterMap = new HashMap<Integer, Character>();

  public CharacterTabReader(File aFile, OBOSession session) throws IOException {
    this.session = session;
    this.parse(aFile);
  }

  public Map<Integer, Character> getCharacters() {
    return this.characterMap;
  }

  private void parse(File aFile) throws IOException {
    final LineNumberReader reader = new LineNumberReader(new FileReader(aFile));
    final String header = reader.readLine();
    final List<String> fields = Arrays.asList(header.split("\t"));
    while (true) {
      final String line = reader.readLine();
      if (line == null) break;
      if (line.trim().equals("")) continue;
      final String[] cells = line.split("\t", -1);
      final int characterNumber = Integer.parseInt(Collections.get(cells, fields.indexOf("Character Number")).trim());
      final String stateSymbol = Collections.get(cells, fields.indexOf("State Number")).trim();
      final String textDescription = Collections.get(cells, fields.indexOf("Textual Description"));
      final String entityID = Collections.get(cells, fields.indexOf("Entity ID"));
      final String qualityID = Collections.get(cells, fields.indexOf("Quality ID"));
      final String relatedEntityID = Collections.get(cells, fields.indexOf("Additional Entity ID"));
      Integer count = null;
      final String countFieldValue = Collections.get(cells, fields.indexOf("Count")).trim();
      boolean putCountInNotesField = false;
      if ((countFieldValue != null) && (!countFieldValue.equals(""))) {
        try {
          count = Integer.parseInt(countFieldValue);
        } catch (NumberFormatException e) {
          log().warn("Could not create number from Count field value: " + countFieldValue + ", adding to Notes field");
          putCountInNotesField = true;
        }
      }
      final Float measurement;
      final String measurementFieldValue = Collections.get(cells, fields.indexOf("Measurement")).trim();
      if ((measurementFieldValue != null) && (!measurementFieldValue.equals(""))) {
        measurement = Float.parseFloat(measurementFieldValue);
      } else { measurement = null; }
      final String unitID = Collections.get(cells, fields.indexOf("Unit ID"));
      final String notes = Collections.get(cells, fields.indexOf("Curator Notes"));
      final Character character;
      if (this.characterMap.containsKey(characterNumber)) {
        character = this.characterMap.get(characterNumber);
      } else {
        character = new Character();
        this.characterMap.put(characterNumber, character);
        character.setLabel(textDescription); //maybe don't do this?
      }
      final State state = this.getState(stateSymbol, character);
      state.setLabel(textDescription); //maybe don't do this?
      final Phenotype phenotype = state.newPhenotype();
      phenotype.setEntity((OBOClass)(session.getObject(entityID)));
      phenotype.setQuality((OBOClass)session.getObject(qualityID));
      phenotype.setRelatedEntity((OBOClass)session.getObject(relatedEntityID));
      phenotype.setCount(count);
      phenotype.setMeasurement(measurement);
      phenotype.setUnit((OBOClass)session.getObject(unitID)); 
      if (putCountInNotesField) {
        final String countNote = "count=" + countFieldValue;
        final String addedNotes = (notes != null && !notes.equals("")) ? (notes + "; " + countNote) : countNote;
        phenotype.setComment(addedNotes);
      } else {
        phenotype.setComment(notes);
      }
    }
  }
  
  private State getState(String symbol, Character character) {
    for (State currentState : character.getStates()) {
      if ((currentState.getSymbol() != null) && (currentState.getSymbol().equals(symbol))) {
        return currentState;
      }
    }
    final State state = character.newState();
    state.setSymbol(symbol);
    return state;
  }
  
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }

}