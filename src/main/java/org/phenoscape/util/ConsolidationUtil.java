package org.phenoscape.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.phenoscape.model.Character;
import org.phenoscape.model.DataSet;
import org.phenoscape.model.MultipleState;
import org.phenoscape.model.State;
import org.phenoscape.model.Taxon;
import org.phenoscape.model.MultipleState.MODE;

public class ConsolidationUtil {

	public static Character createNewCharacterWithStates(Collection<State> states, Character currentCharacter, DataSet data) {
		final Character newCharacter = new Character();
		newCharacter.addStates(states);
		for (Taxon taxon : data.getTaxa()) {
			//FIXME some of this logic should be moved down into the model
			final State stateValue = data.getStateForTaxon(taxon, currentCharacter);
			if (stateValue instanceof MultipleState) {
				final MultipleState multiple = (MultipleState)stateValue;
				if (!Collections.disjoint(states, multiple.getStates())) {
					final Set<State> statesForOldCharacter = new HashSet<State>(multiple.getStates());
					statesForOldCharacter.removeAll(states);
					final Set<State> statesForNewCharacter = new HashSet<State>(multiple.getStates());
					statesForNewCharacter.retainAll(states);
					if (statesForOldCharacter.isEmpty()) {
						data.setStateForTaxon(taxon, currentCharacter, null);
					} else if (statesForOldCharacter.size() == 1) {
						data.setStateForTaxon(taxon, currentCharacter, statesForOldCharacter.iterator().next());
					} else {
						data.setStateForTaxon(taxon, currentCharacter, new MultipleState(statesForOldCharacter, multiple.getMode()));
					}
					if (statesForNewCharacter.isEmpty()) {
						data.setStateForTaxon(taxon, newCharacter, null);
					} else if (statesForNewCharacter.size() == 1) {
						data.setStateForTaxon(taxon, newCharacter, statesForNewCharacter.iterator().next());
					} else {
						data.setStateForTaxon(taxon, newCharacter, new MultipleState(statesForNewCharacter, multiple.getMode()));
					}
				}
			} else {
				if (states.contains(stateValue)) {
					data.setStateForTaxon(taxon, newCharacter, stateValue);
					data.setStateForTaxon(taxon, currentCharacter, null);
				}
			}
		}
		currentCharacter.removeStates(states);
		data.addCharacter(newCharacter);
		return newCharacter;
	}

	public static State consolidateStates(Collection<State> states, Character currentCharacter, DataSet data) {
		final State newState = new State();
		final List<String> labels = new ArrayList<String>();
		final List<String> comments = new ArrayList<String>();
		final List<String> figures = new ArrayList<String>();
		for (State state : states) {
			labels.add(state.getLabel());
			comments.add(state.getComment());
			figures.add(state.getFigure());
		}
		newState.setLabel(StringUtils.stripToNull(org.obo.app.util.Collections.join(labels, "; ")));
		newState.setComment(StringUtils.stripToNull(org.obo.app.util.Collections.join(comments, "; ")));
		newState.setFigure(StringUtils.stripToNull(org.obo.app.util.Collections.join(figures, "; ")));
		for (Taxon taxon : data.getTaxa()) {
			//FIXME some of this logic should be moved down into the model
			final State stateValue = data.getStateForTaxon(taxon, currentCharacter);
			if (stateValue instanceof MultipleState) {
				final MultipleState multiple = (MultipleState)stateValue;
				if (!Collections.disjoint(states, multiple.getStates())) {
					final Set<State> oldStates = new HashSet<State>(multiple.getStates());
					oldStates.removeAll(states);
					oldStates.add(newState);
					if (oldStates.size() > 1) {
						data.setStateForTaxon(taxon, currentCharacter, new MultipleState(oldStates, multiple.getMode()));
					} else {
						data.setStateForTaxon(taxon, currentCharacter, newState);
					}
				}
			} else {
				if (states.contains(stateValue)) {
					data.setStateForTaxon(taxon, currentCharacter, newState);
				}
			}
		}
		currentCharacter.removeStates(states);
		currentCharacter.addState(newState);
		return newState;
	}

	public static Character consolidateCharacters(Collection<Character> characters, DataSet data) {
		//FIXME some of this logic should be moved down into the model
		final Character newCharacter = new Character();
		final List<String> labels = new ArrayList<String>();
		final List<String> comments = new ArrayList<String>();
		final List<String> figures = new ArrayList<String>();
		final List<String> discussions = new ArrayList<String>();
		for (Character character : characters) {
			labels.add(character.getLabel());
			comments.add(character.getComment());
			figures.add(character.getFigure());
			discussions.add(character.getDiscussion());
			newCharacter.addStates(character.getStates());
		}
		newCharacter.setLabel(StringUtils.stripToNull(org.obo.app.util.Collections.join(labels, "; ")));
		newCharacter.setComment(StringUtils.stripToNull(org.obo.app.util.Collections.join(comments, "; ")));
		newCharacter.setFigure(StringUtils.stripToNull(org.obo.app.util.Collections.join(figures, "; ")));
		newCharacter.setDiscussion(StringUtils.stripToNull(org.obo.app.util.Collections.join(discussions, "; ")));
		for (Taxon taxon : data.getTaxa()) {
			final Set<State> statesForTaxon = new HashSet<State>();
			for (Character character : characters) {
				final State stateValue = data.getStateForTaxon(taxon, character);
				if (stateValue instanceof MultipleState) {
					statesForTaxon.addAll(((MultipleState)stateValue).getStates());
				} else if (stateValue != null) {
					statesForTaxon.add(stateValue);
				}
			}
			final State newStateValue;
			if (statesForTaxon.size() > 1) {
				newStateValue = new MultipleState(statesForTaxon, MODE.POLYMORPHIC);
			} else if (statesForTaxon.size() == 1) {
				newStateValue = statesForTaxon.iterator().next();
			} else {
				newStateValue = null;
			}
			data.setStateForTaxon(taxon, newCharacter, newStateValue);
		}
		data.removeCharacters(characters);
		int i = 0;
		for (State state : newCharacter.getStates()) {
			state.setSymbol("" + i);
			i += 1;
		}
		data.addCharacter(newCharacter);
		return newCharacter;
	}

}
