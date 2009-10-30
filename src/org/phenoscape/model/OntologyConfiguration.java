package org.phenoscape.model;

import java.util.List;

/**
 * An object which provides a list of ontology sources for the OntologyController to download.
 * @author Jim Balhoff
 */
public interface OntologyConfiguration {

    public List<OntologySource> getSources();
    
}
