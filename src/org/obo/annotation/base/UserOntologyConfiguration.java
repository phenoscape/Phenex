package org.obo.annotation.base;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;
import org.obo.app.util.PrefObj;

/**
 * An implementation of OntologyConfiguration which contains default ontology sources, and can also read and write a persistent 
 * list of ontology sources to the user's preferences.
 * @author Jim Balhoff
 */
public class UserOntologyConfiguration implements OntologyConfiguration {

    public static final String SOURCES_STORAGE_KEY = "sources";
    private static final List<OntologySource> defaultSources = new ArrayList<OntologySource>();
    static {
        try {
            defaultSources.add(new OntologySource("Teleost Anatomy", new URL("http://phenoscape.svn.sourceforge.net/viewvc/phenoscape/trunk/vocab/teleost_anatomy_VAO.obo")));
            defaultSources.add(new OntologySource("Teleost Taxonomy", new URL("http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/taxonomy/teleost_taxonomy.obo")));
            defaultSources.add(new OntologySource("Vertebrate Taxonomy", new URL("http://phenoscape.svn.sourceforge.net/viewvc/phenoscape/trunk/vocab/vertebrate_taxonomy.obo")));
            defaultSources.add(new OntologySource("Museum Collections", new URL("http://phenoscape.svn.sourceforge.net/viewvc/*checkout*/phenoscape/trunk/vocab/fish_collection_abbreviation.obo")));
            defaultSources.add(new OntologySource("PATO Qualities", new URL("http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/phenotype/quality.obo")));
            defaultSources.add(new OntologySource("Spatial Ontology", new URL("http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/anatomy/caro/spatial.obo")));
            defaultSources.add(new OntologySource("Unit Ontology", new URL("http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/phenotype/unit.obo")));
            defaultSources.add(new OntologySource("OBO Relations", new URL("http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/OBO_REL/ro.obo")));
            defaultSources.add(new OntologySource("OBO Relations (proposed)", new URL("http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/OBO_REL/ro_proposed.obo")));
            defaultSources.add(new OntologySource("Gene Ontology", new URL("http://www.geneontology.org/ontology/obo_format_1_2/gene_ontology.1_2.obo")));
            defaultSources.add(new OntologySource("PATO Character slim", new URL("http://phenoscape.svn.sourceforge.net/viewvc/phenoscape/trunk/vocab/character_slims.obo")));
            defaultSources.add(new OntologySource("Amniote Anatomy", new URL("http://phenoscape.svn.sourceforge.net/viewvc/phenoscape/trunk/vocab/amniote_chicagoedit.obo?revision=3433")));
            defaultSources.add(new OntologySource("Vertebrate Anatomy", new URL("http://phenoscape.svn.sourceforge.net/viewvc/phenoscape/trunk/vocab/skeletal/obo/vertebrate_anatomy.obo?revision=3395")));
        } catch (MalformedURLException e) {
            log().error("One of the default URLs is malformed", e);
        }
    }
    
    public UserOntologyConfiguration() {
        //TODO create a constructor which accepts the default sources as a list instead of hardcoding above
        super();
        final List<OntologySource> storedSources = this.getStoredSources();
        if ((storedSources == null) || (storedSources.isEmpty())) {
            log().info("Storing default sources to preferences");
            this.storeSources(defaultSources);
        }
    }
    
    public List<OntologySource> getSources() {
        return this.getStoredSources();
    }

    /**
     * Retrieve the list of ontology sources from the user's preferences.
     */
    @SuppressWarnings("unchecked")
    public List<OntologySource> getStoredSources() {
        try {
            if (this.getPrefsRoot().nodeExists(SOURCES_STORAGE_KEY)) {
                final Object data = PrefObj.getObject(this.getPrefsRoot(), SOURCES_STORAGE_KEY);
                if (data instanceof List<?>) {
                    return (List<OntologySource>)data;
                } else {
                    log().error("Data stored for ontology sources should be a list, but is not");
                }
            }
        } catch (IOException e) {
            log().error("Couldn't access ontology sources in preferences", e);
        } catch (BackingStoreException e) {
            log().error("Couldn't access ontology sources in preferences", e);
        } catch (ClassNotFoundException e) {
            log().error("Couldn't access ontology sources in preferences", e);
        }
        return null;
    }

    /**
     * Store a given list of ontology sources to the user's preferences.
     */
    public void storeSources(List<OntologySource> theSources) {
        //TODO would be nice to communicate these errors to the user
        try {
            PrefObj.putObject(this.getPrefsRoot(), SOURCES_STORAGE_KEY, theSources);
        } catch (IOException e) {
            log().error("Couldn't store ontology sources in preferences", e);
        } catch (BackingStoreException e) {
            log().error("Couldn't store ontology sources in preferences", e);
        } catch (ClassNotFoundException e) {
            log().error("Couldn't store ontology sources in preferences", e);
        }
    }

    private Preferences getPrefsRoot() {
        return Preferences.userNodeForPackage(this.getClass()).node("ontologyConfig");
    }

    private static Logger log() {
        return Logger.getLogger(UserOntologyConfiguration.class);
    }


}
