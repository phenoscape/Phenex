package org.phenoscape.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.phenoscape.io.CharacterTabReader;
import org.phenoscape.io.TaxonTabReader;
import org.phenoscape.model.Character;
import org.phenoscape.model.DataSet;
import org.phenoscape.model.Specimen;
import org.phenoscape.model.State;
import org.phenoscape.model.Taxon;

public class DataMerger {
  
  /**
   * Merge an annotated taxon list from a tab file into an existing data set.  Taxa are matched 
   * via their "Publication Name".  TTO identifiers and specimen lists are applied to the matched 
   * taxa.  Any taxa in the tab file that do not match a taxon in the existing data set are added 
   * to the data set.
   */
  public static void mergeTaxa(TaxonTabReader reader, DataSet existingData) {
    final List<Taxon> importedTaxa = reader.getTaxa();
    for (Taxon importedTaxon : importedTaxa) {
      boolean matched = false;
      for (Taxon taxon : existingData.getTaxa()) {
        if (taxon.getPublicationName() != null && importedTaxon.getPublicationName() != null) {
          if (taxon.getPublicationName().equals(importedTaxon.getPublicationName())) {
            matched = true;
            taxon.setValidName(importedTaxon.getValidName());
            taxon.setComment(importedTaxon.getComment());
            for (Specimen specimen : importedTaxon.getSpecimens()) {
              taxon.addSpecimen(specimen);
            }
          }
        }
      }
      if (!matched) { existingData.addTaxon(importedTaxon); }
    }
  }

  /**
   * Merge EQ annotations from a tab file into an existing data set. The "Character Number" 
   * and "State Number" columns are used to match a character (by index) and state (by symbol) 
   * in the existing data set.  If the index falls outside the current range of characters, 
   * a new character is appended to the existing data set.  If a state with the given symbol does 
   * not exist, a new state is appended to the given character.
   */
  public static void mergeCharacters(CharacterTabReader reader, DataSet existingData) {
    final Map<Integer, Character> characterMap = reader.getCharacters();
    final List<Entry<Integer, Character>> unusedEntries = new ArrayList<Entry<Integer, Character>>();
    for (Entry<Integer, Character> entry : characterMap.entrySet()) {
      if (existingData.getCharacters().size() >= entry.getKey()) {
        // merge
        log().debug("Merging character originally numbered: " + entry.getKey());
        final int entryIndex = entry.getKey() - 1;
        final Character character = existingData.getCharacters().get(entryIndex);
        character.setLabel(entry.getValue().getLabel());
        for (State newState: entry.getValue().getStates()) {
          final State state = findState(character.getStates(), newState.getSymbol());
          if (state != null) {
            state.setLabel(newState.getSymbol());
            state.getPhenotypes().clear();
            state.getPhenotypes().addAll(newState.getPhenotypes());
          } else {
            character.addState(newState);
          }
        }
      } else {
        unusedEntries.add(entry);
      }
    }
    Collections.sort(unusedEntries, new Comparator<Entry<Integer, Character>>() {
      public int compare(Entry<Integer, Character> o1, Entry<Integer, Character> o2) {
        return o1.getKey().compareTo(o2.getKey());
      }
    });
    for (Entry<Integer, Character> entry : unusedEntries) {
      log().debug("Adding new character originally numbered: " + entry.getKey());
      existingData.addCharacter(entry.getValue());
    }
  }
  
  /**
   * Merge matrix values into an existing data set.  Characters are matched via their index. 
   * Extra characters are appended to the existing data. Values are matched by comparing the symbol 
   * - if a state with that symbol is not available for the character in the existing data set, a 
   * new state with that symbol is added to the character. Taxa are matched via their Publication 
   * Name.  Matrix values for unmatched taxa are unaltered.
   */
  public static void mergeDataSets(DataSet newData, DataSet existingData) {
    for (int i = 0; i < newData.getCharacters().size(); i++) {
      while (i >= existingData.getCharacters().size()) {
        existingData.addCharacter(newData.getCharacters().get(i));
      }
      for (Taxon newTaxon : newData.getTaxa()) {
        final String taxonName = newTaxon.getPublicationName();
        final Taxon taxon;
        final Taxon existingTaxon = findTaxon(existingData.getTaxa(), taxonName);
        if (existingTaxon == null) { 
          taxon = newTaxon;
          existingData.addTaxon(newTaxon);
        } else {
          taxon = existingTaxon;
        }
        final State newStateValue = newData.getStateForTaxon(newTaxon, newData.getCharacters().get(i));
        if (newStateValue == null) continue;
        final String valueSymbol = newStateValue.getSymbol();
        final Character currentCharacter = existingData.getCharacters().get(i);
        final State existingState = findState(currentCharacter.getStates(), valueSymbol);
        final State state;
        if (existingState == null) {
          state = newStateValue;
          currentCharacter.addState(state);
        } else {
          state = existingState;
        }
        existingData.setStateForTaxon(taxon, existingData.getCharacters().get(i), state);
      }
    }
  }
  
  private static Taxon findTaxon(List<Taxon> taxa, String pubName) {
    for (Taxon taxon : taxa) {
      if (pubName.equals(taxon.getPublicationName())) { return taxon; }
    }
    return null;
  }

  private static State findState(List<State> states, String symbol) {
    for (State state: states) {
      if (symbol.equals(state.getSymbol())) { return state; }
    }
    return null;
  }
  
  private static Logger log() {
    return Logger.getLogger(DataMerger.class);
  }

}
