package org.phenoscape.orb;

import org.obo.annotation.base.OBOUtil.Differentium;
import org.obo.app.model.AbstractPropertyChangeObject;
import org.obo.app.model.ObservableEventList;
import org.obo.datamodel.OBOClass;

import ca.odell.glazedlists.BasicEventList;

public class ORBTerm extends AbstractPropertyChangeObject {
    
    private OBOClass parent;
    private final ObservableEventList<Differentium> links = new ObservableEventList<Differentium>(new BasicEventList<Differentium>());

    public OBOClass getParent() {
        return parent;
    }

    public void setParent(OBOClass parent) {
        this.parent = parent;
    }
    
    public void addLink(Differentium aLink) {
        this.links.add(aLink);
    }

    public void removeLink(Differentium aLink) {
        this.links.remove(aLink);
    }

    public ObservableEventList<Differentium> getLinks() {
        return this.links;
    }

}
