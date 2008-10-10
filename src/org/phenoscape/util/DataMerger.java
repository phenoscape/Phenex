package org.phenoscape.util;

import java.util.List;

import org.apache.log4j.Logger;
import org.obo.datamodel.OBOClass;
import org.phenoscape.model.Character;
import org.phenoscape.model.DataSet;
import org.phenoscape.model.Specimen;
import org.phenoscape.model.State;
import org.phenoscape.model.Taxon;

/**
 * @author jim
 *
 */
/**
 * @author jim
 *
 */
public class DataMerger {
 
    /**
     * Merge values from a data set into an existing data set.  Merges characters,
     * taxa, matrix data, and document data, in that order.
     */
    public static void mergeDataSets(DataSet newData, DataSet existingData) {
        mergeCharacters(newData, existingData);
        mergeTaxa(newData, existingData);
        mergeMatrix(newData, existingData);
        mergeDocumentData(newData, existingData);
    }

    /**
     * Merge characters and EQ annotations from a data set into an existing data set. The 
     * "Character Number" and "State Number" columns are used to match a character (by index) and state (by symbol) 
     * in the existing data set.  If the index falls outside the current range of characters, 
     * a new character is appended to the existing data set.  If a state with the given symbol does 
     * not exist, a new state is appended to the given character.  Description information for the new 
     * characters overwrites that of the existing characters.  EQ annotations are replaced.
     */
    public static void mergeCharacters(DataSet newData, DataSet existingData) {
        for (int i = 0; i < newData.getCharacters().size(); i++) {
            final Character newCharacter = newData.getCharacters().get(i);
            if (newCharacter == null) continue; // this handles a "sparse" collection of characters - only certain indexes represented
            if (i >= existingData.getCharacters().size()) {
                //add character
                existingData.addCharacter(newCharacter);
            } else {
                //merge character data
                final Character character = existingData.getCharacters().get(i);
                character.setLabel(newCharacter.getLabel());
                for (State newState : newCharacter.getStates()) {
                    final State state = findState(character.getStates(), newState.getSymbol());
                    if (state != null) {
                        state.setLabel(newState.getLabel());
                        state.getPhenotypes().clear();
                        state.getPhenotypes().addAll(newState.getPhenotypes());
                    } else {
                        character.addState(newState);
                    }
                }
            }
        }
    }

    /**
     * Merge taxa from a dataset into an existing data set.  Taxa are matched 
     * via their "Publication Name" first.  TTO identifiers and specimen lists are applied to the matched 
     * taxa.  Any taxa in the tab file that do not match a taxon in the existing data set are added 
     * to the data set.  If a match wasn't found by publication name, taxa are matched by TTO identifer.
     */
    public static void mergeTaxa(DataSet newData, DataSet existingData) {
        final List<Taxon> importedTaxa = newData.getTaxa();
        for (Taxon importedTaxon : importedTaxa) {
            final Taxon pubNameMatch = findTaxon(existingData.getTaxa(), importedTaxon.getPublicationName());
            final Taxon existingTaxon = (pubNameMatch == null) ? (findTaxon(existingData.getTaxa(), importedTaxon.getValidName())) : pubNameMatch;
            if (existingTaxon != null) {
                existingTaxon.setValidName(importedTaxon.getValidName());
                existingTaxon.setComment(importedTaxon.getComment());
                for (Specimen specimen : importedTaxon.getSpecimens()) {
                    existingTaxon.addSpecimen(specimen);
                }
            } else {
                existingData.addTaxon(importedTaxon);
            }
        }
    }

    /**
     * Merge matrix values into an existing data set.  Characters are matched via their index. 
     * Only matrix values for existing taxa and characters are applied. Values are matched by comparing 
     * the symbol - if a state with that symbol is not available for the character in the existing data set, a 
     * new state with that symbol is added to the character. Taxa are matched via their Publication 
     * Name.  Matrix values for unmatched taxa are unaltered.
     */
    public static void mergeMatrix(DataSet newData, DataSet existingData) {
        for (int i = 0; i < existingData.getCharacters().size(); i++) {
            final Character currentCharacter = existingData.getCharacters().get(i);
            for (Taxon taxon : existingData.getTaxa()) {
                final String taxonName = taxon.getPublicationName();
                final Taxon newTaxon = findTaxon(newData.getTaxa(), taxonName);
                if (newTaxon == null) { continue; }
                final Character newCharacter = newData.getCharacters().get(i);
                final State newStateValue = newData.getStateForTaxon(newTaxon, newCharacter);
                final State state;
                if (newStateValue == null) {
                    state = null;
                } else {
                    final String valueSymbol = newStateValue.getSymbol();
                    final State existingState = findState(currentCharacter.getStates(), valueSymbol);
                    if (existingState == null) {
                        state = newStateValue;
                        currentCharacter.addState(state);
                    } else {
                        state = existingState;
                    }
                }
                existingData.setStateForTaxon(taxon, currentCharacter, state);
            }
        }
    }

    
    /**
     * Replace values such as publication, publication notes, and curators in the 
     * existing data with any non-null values in the new data.  If the new data is null 
     * for a field, the existing value is unchanged.
     */
    public static void mergeDocumentData(DataSet newData, DataSet existingData) {
        if (newData.getPublication() != null) {
            existingData.setPublication(newData.getPublication());
        }
        if (newData.getCurators() != null) {
            existingData.setCurators(newData.getCurators());
        }
        if (newData.getPublicationNotes() != null) {
            existingData.setPublicationNotes(newData.getPublicationNotes());
        }
    }

    private static Taxon findTaxon(List<Taxon> taxa, String pubName) {
        if ((pubName == null) || (pubName.equals(""))) { return null; }
        for (Taxon taxon : taxa) {
            if (pubName.equals(taxon.getPublicationName())) { return taxon; }
        }
        return null;
    }

    private static Taxon findTaxon(List<Taxon> taxa, OBOClass validName) {
        if (validName == null) { return null; }
        for (Taxon taxon : taxa) {
            if (validName.equals(taxon.getValidName())) { return taxon; }
        }
        return null;
    }

    private static State findState(List<State> states, String symbol) {
        for (State state: states) {
            if (symbol.equals(state.getSymbol())) { return state; }
        }
        return null;
    }

    @SuppressWarnings("unused")
    private static Logger log() {
        return Logger.getLogger(DataMerger.class);
    }

}
