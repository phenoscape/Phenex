package org.obo.annotation.view;

import org.bbop.framework.AbstractComponentFactory;
import org.obo.datamodel.OBOSession;

public class TermInfoComponentFactory extends AbstractComponentFactory<TermInfoComponent> {
    
    private final OntologyCoordinator coordinator;
    
    public TermInfoComponentFactory(OntologyCoordinator coordinator) {
        this.coordinator = coordinator;
    }
    
    @Override
    public TermInfoComponent doCreateComponent(String id) {
        return new TermInfoComponent(id, this.coordinator);
    }

    public FactoryCategory getCategory() {
        return FactoryCategory.ONTOLOGY;
    }

    public String getName() {
        return "Term Info";
    }

    public boolean isSingleton() {
        return true;
    }

    public String getID() {
        return "term-info";
    }

}

