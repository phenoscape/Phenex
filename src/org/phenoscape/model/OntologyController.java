package org.phenoscape.model;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.bbop.dataadapter.DataAdapterException;
import org.bbop.framework.GUIManager;
import org.obo.dataadapter.OBOAdapter;
import org.obo.dataadapter.OBOFileAdapter;
import org.obo.dataadapter.OBOMetaData;
import org.obo.datamodel.Namespace;
import org.obo.datamodel.OBOSession;
import org.oboedit.controller.SessionManager;
import org.phenoscape.io.URLProxy;

/**
 * This class is very much a work in progress and temporarily hardcodes much of ontology loading.
 * @author Jim Balhoff
 */
public class OntologyController {

    private final OBOFileAdapter fileAdapter;
    private final OBOMetaData metadata;

    private String TTO = "";
    private String COLLECTION = "";
    private String TAO = "";
    private String PATO = "";
    private String SPATIAL = "";
    private String UNIT = "";
    private String REL = "";
    private String REL_PROPOSED = "";
    private String GO = "";
    private String CHARACTER_SLIM = "";

    private TermSet entityTermSet = null;
    private TermSet qualityTermSet = null;
    private TermSet taxonTermSet = null;
    private TermSet collectionTermSet = null;
    private TermSet unitTermSet = null;
    private TermSet relationsTermSet = null;

    public OntologyController() {
        this.fileAdapter = new OBOFileAdapter();
        OBOFileAdapter.OBOAdapterConfiguration config = new OBOFileAdapter.OBOAdapterConfiguration();
        config.setReadPaths(Arrays.asList(this.getPaths()));
        config.setBasicSave(false);
        config.setAllowDangling(true);
        config.setFollowImports(false); // this is required because OBO currently fails if it tries to follow an import and there is no network connection
        try {
            SessionManager.getManager().setSession(this.fileAdapter.doOperation(OBOAdapter.READ_ONTOLOGY, config, null));
        } catch (DataAdapterException e) {
            log().fatal("Failed to load ontologies", e);
        }
        this.metadata = this.fileAdapter.getMetaData();
        this.prefetchTermSets();
    }

    private String[] getPaths () {
        URLProxy proxy = new URLProxy(new File(GUIManager.getPrefsDir(),"Ontology Cache"));
        try {
            this.TTO = proxy.get(new URL("http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/taxonomy/teleost_taxonomy.obo")).toURI().toString();
            this.COLLECTION = proxy.get(new URL("http://phenoscape.svn.sourceforge.net/viewvc/*checkout*/phenoscape/trunk/vocab/fish_collection_abbreviation.obo")).toURI().toString();
            this.TAO = proxy.get(new URL("http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/anatomy/gross_anatomy/animal_gross_anatomy/fish/teleost_anatomy.obo")).toURI().toString();
            //this.TAO = proxy.get(new URL("http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/anatomy/gross_anatomy/animal_gross_anatomy/amphibian/amphibian_anatomy.obo")).toURI().toString();
            this.PATO = proxy.get(new URL("http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/phenotype/quality.obo")).toURI().toString();
            this.SPATIAL = proxy.get(new URL("http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/anatomy/caro/spatial.obo")).toURI().toString();
            this.UNIT = proxy.get(new URL("http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/phenotype/unit.obo")).toURI().toString();
            this.REL = proxy.get(new URL("http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/OBO_REL/ro.obo")).toURI().toString();
            this.REL_PROPOSED = proxy.get(new URL("http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/OBO_REL/ro_proposed.obo")).toURI().toString();
            this.GO = proxy.get(new URL("http://www.geneontology.org/ontology/obo_format_1_2/gene_ontology.1_2.obo")).toURI().toString();
            this.CHARACTER_SLIM = proxy.get(new URL("http://phenoscape.svn.sourceforge.net/viewvc/phenoscape/trunk/vocab/character_slims.obo")).toURI().toString();
            String[] paths = { TTO, COLLECTION, TAO, PATO, SPATIAL, UNIT, REL, REL_PROPOSED, GO, CHARACTER_SLIM };
            return paths;
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            //TODO alert user somehow
            log().fatal("Unable to read one or more ontologies", e);
        }
        return new String[] {};
    }

    public OBOSession getOBOSession() {
        return SessionManager.getManager().getSession();
    }

    public TermSet getTaxonTermSet() {
        if (this.taxonTermSet == null) {
            this.taxonTermSet = this.getTermSet(TTO); 
        }
        return this.taxonTermSet;
    }

    public TermSet getCollectionTermSet() {
        if (this.collectionTermSet == null) {
            this.collectionTermSet = this.getTermSet(COLLECTION); 
        }
        return this.collectionTermSet;
    }

    public TermSet getEntityTermSet() {
        if (this.entityTermSet == null) {
            final TermSet terms = this.getTermSet(TAO, SPATIAL, PATO, GO);
            //terms.setTermFilter(new AnatomyTermFilter(this.getOBOSession()));
            this.entityTermSet = terms;
        }
        
        return this.entityTermSet;
    }

    public TermSet getQualityTermSet() {
        if (this.qualityTermSet == null) {
            final TermSet terms = this.getTermSet(PATO);
            //terms.setTermFilter(new AnatomyTermFilter(this.getOBOSession()));
            this.qualityTermSet = terms;
        }
        return this.qualityTermSet;
    }

    public TermSet getRelatedEntityTermSet() {
        return this.getEntityTermSet();
    }

    public TermSet getUnitTermSet() {
        if (this.unitTermSet == null) {
            this.unitTermSet = this.getTermSet(UNIT);
        }
        return this.unitTermSet;
    }

    public TermSet getRelationsTermSet() {
        if (this.relationsTermSet == null) {
            final TermSet set = this.getTermSet();
            set.setTermFilter(new RelationTermFilter(this.getOBOSession()));
            set.setIncludesProperties(true);
            this.relationsTermSet = set;
        }
        return this.relationsTermSet;
    }

    private TermSet getTermSet(String... urls) {
        final Collection<Namespace> namespaces = new ArrayList<Namespace>();
        for (String url : urls) {
            namespaces.addAll(this.metadata.getNamespaces(url));
        }
        final TermSet terms =  new TermSet();
        terms.setOBOSession(this.getOBOSession());
        terms.setNamespaces(namespaces);
        return terms;
    }
    
    /**
     * This is just a startup "optimization" - it makes the term searches
     * happen while the ontology loading panel is displayed.  This reduces
     * the blank time between that panel disappearing and the interface being
     * displayed.
     */
    private void prefetchTermSets() {
        this.getEntityTermSet().getTerms();
        this.getTaxonTermSet().getTerms();
        this.getCollectionTermSet().getTerms();
        this.getUnitTermSet().getTerms();
        this.getRelationsTermSet().getTerms();
    }

    private Logger log() {
        return Logger.getLogger(this.getClass());
    }

}
