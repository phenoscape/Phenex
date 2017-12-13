package org.obo.annotation.view;

import org.obo.datamodel.OBOSession;

public class DefaultOntologyCoordinator implements OntologyCoordinator {

	private final OBOSession session;
	private final SelectionManager selectionManager;

	public DefaultOntologyCoordinator(OBOSession session, SelectionManager selectionManager) {
		this.session = session;
		this.selectionManager = selectionManager;
	}

	@Override
	public OBOSession getOBOSession() {
		return this.session;
	}

	@Override
	public SelectionManager getSelectionManager() {
		return this.selectionManager;
	}

}
