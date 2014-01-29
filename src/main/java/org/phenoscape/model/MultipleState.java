package org.phenoscape.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.obo.app.model.ObservableEventList;

public class MultipleState extends State {

	public static enum MODE { POLYMORPHIC, UNCERTAIN };
	private final Set<State> states;
	private final MODE mode;

	public MultipleState(Set<State> states, MODE mode) {
		super();
		this.states = states;
		this.mode = mode;
	}
	
	public MultipleState(String nexmlID, Set<State> states, MODE mode) {
		super(nexmlID);
		this.states = states;
		this.mode = mode;
	}

	public Set<State> getStates() {
		return Collections.unmodifiableSet(this.states);
	}

	public MODE getMode() {
		return this.mode;
	}

	@Override
	public Phenotype newPhenotype() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addPhenotype(Phenotype aPhenotype) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removePhenotype(Phenotype aPhenotype) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ObservableEventList<Phenotype> getPhenotypes() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getLabel() {
		final List<String> labels = new ArrayList<String>();
		for (State state : this.getStates()) {
			labels.add(state.getLabel());
		}
		return StringUtils.join(labels, this.getSeparator());
	}

	@Override
	public void setLabel(String aLabel) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getSymbol() {
		final List<String> symbols = new ArrayList<String>();
		for (State state : this.getStates()) {
			symbols.add(state.getSymbol());
		}
		return StringUtils.join(symbols, this.getSeparator());
	}

	@Override
	public void setSymbol(String aSymbol) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getComment() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setComment(String notes) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getFigure() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setFigure(String aFigure) {
		throw new UnsupportedOperationException();
	}

	@Override
	public PhenotypeProposal getProposal() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setProposal(PhenotypeProposal proposal) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return StringUtils.join(getStates(), this.getSeparator());
	}
	
	private String getSeparator() {
		return this.getMode() == MODE.POLYMORPHIC ? " and " : " or ";
	}

}
