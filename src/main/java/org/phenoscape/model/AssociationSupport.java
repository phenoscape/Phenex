package org.phenoscape.model;

import java.util.HashMap;
import java.util.Map;

public class AssociationSupport {

	private final String descriptionText;
	private final String descriptionSource;
	private final boolean direct;
	private static final Map<String, AssociationSupport> pool = new HashMap<String, AssociationSupport>();

	private AssociationSupport(String descriptionText, String descriptionSource, boolean direct) {
		this.descriptionText = descriptionText;
		this.descriptionSource = descriptionSource;
		this.direct = direct;
	}

	public static AssociationSupport create(String descriptionText, String descriptionSource, boolean direct) {
		final String key = descriptionText + descriptionSource;
		final AssociationSupport newSupport;
		if (pool.containsKey(key)) {
			newSupport = pool.get(key);
		} else {
			newSupport = new AssociationSupport(descriptionText, descriptionSource, direct);
			pool.put(key, newSupport);
		}
		return newSupport;
	}

	public String getDescriptionText() {
		return descriptionText;
	}

	public String getDescriptionSource() {
		return descriptionSource;
	}

	public boolean isDirect() {
		return this.direct;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((descriptionSource == null) ? 0 : descriptionSource.hashCode());
		result = prime * result + ((descriptionText == null) ? 0 : descriptionText.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AssociationSupport other = (AssociationSupport) obj;
		if (descriptionSource == null) {
			if (other.descriptionSource != null)
				return false;
		} else if (!descriptionSource.equals(other.descriptionSource))
			return false;
		if (descriptionText == null) {
			if (other.descriptionText != null)
				return false;
		} else if (!descriptionText.equals(other.descriptionText))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AssociationSupport [descriptionText=" + descriptionText + ", descriptionSource=" + descriptionSource + "]";
	}

}
