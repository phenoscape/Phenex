package org.phenoscape.view;

import org.apache.log4j.Logger;
import org.bbop.framework.AbstractGUIComponent;
import org.bbop.framework.ComponentManager;
import org.phenoscape.controller.PhenexController;

public class PhenoscapeGUIComponent extends AbstractGUIComponent {

    private final PhenexController controller;

    public PhenoscapeGUIComponent(String id, PhenexController controller) {
        super(id);
        this.controller = controller;
    }

    /**
     * Most interface components will need access to data.  The controller is the gatekeeper
     * to the data model.
     */
    protected PhenexController getController() {
        return this.controller;
    }

    /**
     * Change the title for the tab of this component;
     */
    protected void updatePanelTitle(String title) {
        ComponentManager.getManager().setLabel(this, title);
    }

	protected Logger log() {
        return Logger.getLogger(this.getClass());
    }

}
