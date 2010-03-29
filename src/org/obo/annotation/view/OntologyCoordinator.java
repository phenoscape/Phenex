package org.obo.annotation.view;

import org.obo.datamodel.OBOSession;

/**
 * OntologyCoordinator implementations are to be used to pass shared objects such as ontology sessions and global term selection handlers to 
 * interface components. This helps to eliminate usage of global variables in the form of singletons.
 * @author Jim Balhoff
 */
public interface OntologyCoordinator {
    
    public OBOSession getOBOSession();
    
    public SelectionManager getSelectionManager();

}
