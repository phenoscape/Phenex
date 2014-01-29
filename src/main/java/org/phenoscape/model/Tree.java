package org.phenoscape.model;

import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.ObjectUtils;
import org.obo.app.model.AbstractPropertyChangeObject;
import org.obo.datamodel.LinkedObject;

public class Tree extends AbstractPropertyChangeObject {

	private final String nexmlID;
	public static final String LABEL = "label";
	public static final String TOPOLOGY = "topology";
	private String label;
	private Map<LinkedObject, LinkedObject> topology;

	public Tree() {
		this("t" + UUID.randomUUID().toString());
	}

	public Tree(String nexmlID) {
		this.nexmlID = nexmlID;
	}

	public String getNexmlID() {
		return this.nexmlID;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String aLabel) {
		if (ObjectUtils.equals(this.label, aLabel)) return;
		final String oldLabel = this.label;
		this.label = aLabel;
		this.firePropertyChange(LABEL, oldLabel, aLabel);
	}

	public Map<LinkedObject, LinkedObject> getTopology() {
		return this.topology;
	}

	public void setTopology(Map<LinkedObject, LinkedObject> topology) {
		this.topology = topology;
	}

	@Override
	public Class<?> getClass(String propertyKey) throws UndefinedKeyException {
		if (propertyKey.equals(LABEL)) {
			return String.class;
		} else {
			return super.getClass(propertyKey);
		}
	}

}
