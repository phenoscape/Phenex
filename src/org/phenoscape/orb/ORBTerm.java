package org.phenoscape.orb;

import org.obo.annotation.base.OBOUtil.Differentium;
import org.obo.app.model.AbstractPropertyChangeObject;
import org.obo.app.model.ObservableEventList;
import org.obo.datamodel.OBOClass;

import ca.odell.glazedlists.BasicEventList;

public class ORBTerm extends AbstractPropertyChangeObject {
    
	private String label;
    private OBOClass parent;
    private String definition = "temporary definition";
    private final ObservableEventList<Differentium> links = new ObservableEventList<Differentium>(new BasicEventList<Differentium>());

    public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDefinition() {
		return definition;
	}

	public void setDefinition(String definition) {
		this.definition = definition;
	}

	public OBOClass getParent() {
        return parent;
    }

    public void setParent(OBOClass parent) {
        this.parent = parent;
    }
    
//    public void addLink(Differentium aLink) {
//        this.links.add(aLink);
//    }
//
//    public void removeLink(Differentium aLink) {
//        this.links.remove(aLink);
//    }

    public ObservableEventList<Differentium> getLinks() {
        return this.links;
    }

}
