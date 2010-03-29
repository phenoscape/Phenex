package org.phenoscape.view;

import org.bbop.framework.AbstractComponentFactory;
import org.obo.annotation.base.UserOntologyConfiguration;

public class OntologyPreferencesComponentFactory extends AbstractComponentFactory<OntologyPreferencesComponent> {
    
    private final UserOntologyConfiguration config;
    
    public OntologyPreferencesComponentFactory(UserOntologyConfiguration configuration) {
        super();
        this.config = configuration;
    }

    @Override
    public OntologyPreferencesComponent doCreateComponent(String id) {
        return new OntologyPreferencesComponent(id, this.config);
    }

    public FactoryCategory getCategory() {
        return FactoryCategory.CONFIG;
    }

    public String getID() {
        return "phenex_ontology_sources";
    }

    public String getName() {
        return "Ontology Sources";
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public boolean getPreferSeparateWindow() {
        return true;
    }

}
