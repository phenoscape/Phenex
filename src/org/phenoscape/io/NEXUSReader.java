package org.phenoscape.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.biojava.bio.seq.io.ParseException;
import org.biojavax.bio.phylo.io.nexus.CharactersBlock;
import org.biojavax.bio.phylo.io.nexus.NexusBlock;
import org.biojavax.bio.phylo.io.nexus.NexusFileBuilder;
import org.biojavax.bio.phylo.io.nexus.NexusFileFormat;
import org.biojavax.bio.phylo.io.nexus.TaxaBlock;
import org.phenoscape.model.Character;
import org.phenoscape.model.DataSet;
import org.phenoscape.model.MultipleState;
import org.phenoscape.model.MultipleState.MODE;
import org.phenoscape.model.State;
import org.phenoscape.model.Taxon;

public class NEXUSReader {

	private final List<Taxon> taxa = new ArrayList<Taxon>();
	private final List<Character> characters = new ArrayList<Character>();
	private final Map<String, Map<String, State>> matrix = new HashMap<String, Map<String, State>>();

	public NEXUSReader(File nexusFile) throws ParseException, IOException {
		this.parseNEXUS(nexusFile);
	}

	public List<Taxon> getTaxa() {
		return this.taxa;
	}

	public List<Character> getCharacters() {
		return this.characters;
	}

	public Map<String, Map<String, State>> getMatrix() {
		return this.matrix;
	}

	public DataSet getDataSet() {
		//TODO should really just manipulate the dataset directly in the parse method
		final DataSet data = new DataSet();
		data.getCharacters().addAll(this.characters);
		data.getTaxa().addAll(this.taxa);
		data.setMatrixData(this.matrix);
		return data;
	}

	@SuppressWarnings("unchecked")
	private void parseNEXUS(File nexusFile) throws ParseException, IOException {
		final NexusFileBuilder builder = new NexusFileBuilder(); 
		NexusFileFormat.parseFile(builder, nexusFile);
		Iterator<NexusBlock> blocks = builder.getNexusFile().blockIterator();
		while (blocks.hasNext()) {
			final NexusBlock block = blocks.next();
			if (block instanceof TaxaBlock) {
				final TaxaBlock taxaBlock = (TaxaBlock)block;
				for (String taxLabel : (List<String>)taxaBlock.getTaxLabels()) {
					final Taxon newTaxon = new Taxon();
					newTaxon.setPublicationName(taxLabel);
					this.taxa.add(newTaxon);
				}
			}
			if (block instanceof CharactersBlock) {
				final CharactersBlock charactersBlock = (CharactersBlock)block;
				for (int i = 0; i < charactersBlock.getDimensionsNChar(); i++) {
					this.characters.add(new Character());
				}
				final String missing = charactersBlock.getMissing();
				final String gap = charactersBlock.getGap();
				for (Object o : charactersBlock.getAllCharStates()) {
					final String charNumberString = o.toString();
					final Character newChar = this.characters.get(Integer.parseInt(charNumberString) - 1);
					newChar.setLabel(charactersBlock.getCharStateLabel(charNumberString));
					for (int i = 0; i < charactersBlock.getSymbols().size(); i++) {
						final String symbol = (String)(charactersBlock.getSymbols().get(i));
						final State newState = new State();
						newState.setSymbol(symbol);
						if (i < charactersBlock.getCharStateLabelKeywords(charNumberString).size()) {
							newState.setLabel(charactersBlock.getCharStateLabelKeywords(charNumberString).get(i).toString());
						}            
						newChar.addState(newState);
					}
					if (charNumberString.equals("121")) {
						log().debug(newChar.getLabel());
						log().debug(newChar.getStates());
					}
				}
				final Set<State> usedStates = new HashSet<State>();
				for (Taxon taxon : this.taxa) {
					final Map<String, State> currentMap = new HashMap<String, State>();
					this.matrix.put(taxon.getNexmlID(), currentMap);
					List<?> cells = charactersBlock.getMatrixData(taxon.getPublicationName());
					cells.remove(0);
					for (int i = 0; i < cells.size(); i++) {
						final Object item = cells.get(i);
						if (i < this.characters.size()) {
							final Character character = this.characters.get(i);
							if (item instanceof String) {
								final String symbol = (String)(item);

								final State existingState = this.findState(character.getStates(), symbol);
								final State state;
								if (existingState != null) {
									state = existingState;
								} else if ((!symbol.equals(gap)) && (!symbol.equals(missing))) {
									state = character.newState();
									state.setSymbol(symbol);
								} else {
									state = null;
								}
								if (state != null) {
									currentMap.put(character.getNexmlID(), state);
									usedStates.add(state);
								}
							} else if (item instanceof List) {
								final Set<State> statesSet = this.findStatesSet(character.getStates(), (List<String>)item);
								final State state = new MultipleState(statesSet, MODE.POLYMORPHIC);
								currentMap.put(character.getNexmlID(), state);
								usedStates.addAll(statesSet);
								log().debug("Created polymorphic state: " + state);
							} else if (item instanceof Set) {
								//FIXME repeated code
								final Set<State> statesSet = this.findStatesSet(character.getStates(), (Set<String>)item);
								final State state = new MultipleState(statesSet, MODE.UNCERTAIN);
								currentMap.put(character.getNexmlID(), state);
								usedStates.addAll(statesSet);
								log().debug("Created uncertain state: " + state);
							}
						} 
					}
				}
				// remove unused states
				for (Character character : this.characters) {
					final List<State> statesToRemove = new ArrayList<State>();
					for (State state : character.getStates()) {
						if (!usedStates.contains(state) && StringUtils.isBlank(state.getLabel())) {
							statesToRemove.add(state);
						}
					}
					character.getStates().removeAll(statesToRemove);
				}
			}
		}
	}

	private Set<State> findStatesSet(List<State> allStates, Collection<String> symbols) {
		final Set<State> states = new HashSet<State>();
		for (String symbol : symbols) {
			if (StringUtils.stripToNull(symbol) != null) {
				states.add(this.findState(allStates, symbol));	
			}
		}
		return states;
	}

	private  State findState(List<State> states, String symbol) {
		for (State state: states) {
			if (symbol.equals(state.getSymbol())) { return state; }
		}
		return null;
	}

	private Logger log() {
		return Logger.getLogger(this.getClass());
	}

}
