package org.obo.app.swing;

public class DefaultHit<T> implements SearchHit<T> {

	private final T item;
	private final MatchType type;
	private final String text;

	public DefaultHit(T item, String matchedText, MatchType type) {
		this.item = item;
		this.text = matchedText;
		this.type = type;
	}

	@Override
	public T getHit() {
		return this.item;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<T> getHitClass() {
		// a little unsure if this is a dangerous use of generics
		return (Class<T>)this.item.getClass();
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
	public String getPrimaryText() {
		return this.item.toString();
	}

}
