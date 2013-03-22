package org.phenoscape.util;

import org.phenoscape.model.Character;
import org.phenoscape.model.State;

public class ConsistencyIssue {

	private final Character character;
	private final State state;
	private final String issue;

	public ConsistencyIssue(Character character, State state, String issue) {
		this.character = character;
		this.state = state;
		this.issue = issue;
	}

//	public String getCharacterNumber() {
//		return this.character.g; //TODO
//	}

	public Character getCharacter() {
		return this.character;
	}

	public State getState() {
		return this.state;
	}

	public String getIssue() {
		return this.issue;
	}

}
