package org.phenoscape.model;

import java.util.Collections;
import java.util.Set;

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
		// TODO Auto-generated method stub
		return super.getLabel();
	}

	@Override
	public void setLabel(String aLabel) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getSymbol() {
		// TODO Auto-generated method stub
		return super.getSymbol();
	}

	@Override
	public void setSymbol(String aSymbol) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getComment() {
		// TODO Auto-generated method stub
		return super.getComment();
	}

	@Override
	public void setComment(String notes) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getFigure() {
		// TODO Auto-generated method stub
		return super.getFigure();
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
		// TODO Auto-generated method stub
		return super.toString();
	}

}
