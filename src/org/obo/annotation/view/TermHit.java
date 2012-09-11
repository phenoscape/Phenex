package org.obo.annotation.view;

import org.obo.app.swing.MatchType;
import org.obo.app.swing.SearchHit;
import org.obo.datamodel.OBOObject;

public class TermHit implements SearchHit<OBOObject> {

	private final OBOObject term;
	private final MatchType type;
	private final String text;

	public TermHit(OBOObject term, String text, MatchType matchType) {
		this.term = term;
		this.text = text;
		this.type = matchType;
	}

	@Override
	public OBOObject getHit() {
		return this.term;
	}

	@Override
	public String getMatchText() {
		return this.text;
	}

	@Override
	public MatchType getMatchType() {
		return this.type;
	}

	@Override
	public String toString() {
		return this.getMatchText() + ": {" + this.getMatchType().getName() + "}";
	}

	@Override
	public String getPrimaryText() {
		return term.getName();
	}

	@Override
	public Class<OBOObject> getHitClass() {
		return OBOObject.class;
	}

}
