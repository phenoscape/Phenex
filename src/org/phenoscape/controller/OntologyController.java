package org.phenoscape.controller;

import java.beans.XMLDecoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.bbop.dataadapter.DataAdapterException;
import org.bbop.framework.GUIManager;
import org.obo.annotation.base.OntologyConfiguration;
import org.obo.annotation.base.OntologySource;
import org.obo.annotation.base.TermSet;
import org.obo.app.util.URLProxy;
import org.obo.dataadapter.OBOAdapter;
import org.obo.dataadapter.OBOFileAdapter;
import org.obo.datamodel.IdentifiedObject;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOSession;
import org.obo.datamodel.impl.OBOSessionImpl;
import org.obo.filters.Filter;
import org.oboedit.controller.SessionManager;
import org.phenoscape.util.ProvisionalTermUtil;
import org.xml.sax.SAXException;

/**
 * @author Jim Balhoff
 */
public class OntologyController {

	private static final String ENTITY_FILTER = "entities";
	private static final String QUALITY_FILTER = "qualities";
	private static final String RELATION_FILTER = "relations";
	private static final String UNIT_FILTER = "units";
	private static final String TAXON_FILTER = "taxa";
	private static final String MUSEUM_FILTER = "museums";
	private static final String PC_FILLERS_FILTER = "postcomposition";
	private static final String ALL_TERMS = "allterms";
	private File overridingFiltersFolder = new File(GUIManager.getPrefsDir(), "Filters");

	private TermSet entityTermSet = null;
	private TermSet qualityTermSet = null;
	private TermSet taxonTermSet = null;
	private TermSet collectionTermSet = null;
	private TermSet unitTermSet = null;
	private TermSet relationsTermSet = null;
	private TermSet pcFillersTermSet = null;
	private TermSet allTermsSet = null;

	private final OntologyConfiguration config;

	public OntologyController(OntologyConfiguration configuration) {
		this.config = configuration;
		final OBOFileAdapter fileAdapter = new OBOFileAdapter();
		OBOFileAdapter.OBOAdapterConfiguration adapterConfig = new OBOFileAdapter.OBOAdapterConfiguration();
		adapterConfig.setReadPaths(Arrays.asList(this.getPaths()));
		adapterConfig.setBasicSave(false);
		adapterConfig.setAllowDangling(true);
		adapterConfig.setFollowImports(false); // this is required because OBO currently fails if it tries to follow an import and there is no network connection
		try {
			SessionManager.getManager().setSession(fileAdapter.doOperation(OBOAdapter.READ_ONTOLOGY, adapterConfig, null));
		} catch (DataAdapterException e) {
			JOptionPane.showMessageDialog(null, "An error occurred while loading ontologies: " + e.getLocalizedMessage(), "Error Loading Ontologies", JOptionPane.ERROR_MESSAGE);
			log().fatal("Failed to load ontologies", e);
			SessionManager.getManager().setSession(new OBOSessionImpl());
		}
		this.loadProvisionalTerms();
		this.prefetchTermSets();
	}

	private String[] getPaths() {
		//TODO make proxy location configurable
		URLProxy proxy = new URLProxy(new File(GUIManager.getPrefsDir(),"Ontology Cache"));
		final List<String> urls = new ArrayList<String>();
		for (OntologySource source : this.config.getSources()) {
			try {
				final File localFile = proxy.get(source.getURL());
				urls.add(localFile.toURI().toString());
			} catch (IOException e) {
				//TODO alert user somehow
				log().error("Unable to read ontology at: " + source.getURL(), e);
			}
		}
		return urls.toArray(new String[] {});
	}

	public OBOSession getOBOSession() {
		return SessionManager.getManager().getSession();
	}

	public TermSet getTaxonTermSet() {
		if (this.taxonTermSet == null) {
			final TermSet terms =  this.makeTermSet();
			terms.setTermFilter(this.loadFilterWithName(TAXON_FILTER));
			this.taxonTermSet = terms;
		}
		return this.taxonTermSet;
	}

	public TermSet getCollectionTermSet() {
		if (this.collectionTermSet == null) {
			final TermSet terms =  this.makeTermSet();
			terms.setTermFilter(this.loadFilterWithName(MUSEUM_FILTER));
			this.collectionTermSet = terms;
		}
		return this.collectionTermSet;
	}

	public TermSet getEntityTermSet() {
		if (this.entityTermSet == null) {
			final TermSet terms =  this.makeTermSet();
			terms.setTermFilter(this.loadFilterWithName(ENTITY_FILTER));
			this.entityTermSet = terms;
		}
		return this.entityTermSet;
	}

	public TermSet getQualityTermSet() {
		if (this.qualityTermSet == null) {
			final TermSet terms =  this.makeTermSet();
			terms.setTermFilter(this.loadFilterWithName(QUALITY_FILTER));
			this.qualityTermSet = terms;
		}
		return this.qualityTermSet;
	}

	public TermSet getRelatedEntityTermSet() {
		return this.getEntityTermSet();
	}

	public TermSet getUnitTermSet() {
		if (this.unitTermSet == null) {
			final TermSet terms =  this.makeTermSet();
			terms.setTermFilter(this.loadFilterWithName(UNIT_FILTER));
			this.unitTermSet = terms;
		}
		return this.unitTermSet;
	}

	public TermSet getRelationsTermSet() {
		if (this.relationsTermSet == null) {
			final TermSet set =  this.makeTermSet();
			set.setTermFilter(this.loadFilterWithName(RELATION_FILTER));
			this.relationsTermSet = set;
		}
		return this.relationsTermSet;
	}

	public TermSet getPostCompositionFillersTermSet() {
		if (this.pcFillersTermSet == null) {
			final TermSet set =  this.makeTermSet();
			set.setTermFilter(this.loadFilterWithName(PC_FILLERS_FILTER));
			this.pcFillersTermSet = set;
		}
		return this.pcFillersTermSet;
	}

	public TermSet getAllTermsSet() {
		if (this.allTermsSet == null) {
			final TermSet set =  this.makeTermSet();
			set.setTermFilter(this.loadFilterWithName(ALL_TERMS));
			this.allTermsSet = set;
		}
		return this.allTermsSet;
	}

	public File getOverridingFiltersFolder() {
		return this.overridingFiltersFolder;
	}

	public void setOverridingFiltersFolder(File folder) {
		this.overridingFiltersFolder = folder;
	}

	private TermSet makeTermSet() {
		return new TermSet(this.getOBOSession(), SessionManager.getManager().getReasoner());
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
		 this.getQualityTermSet().getTerms();
		 this.getRelatedEntityTermSet().getTerms();
		 this.getPostCompositionFillersTermSet().getTerms();
		 this.getAllTermsSet().getTerms();
	 }

	 private Filter<IdentifiedObject> loadFilterWithName(String filterName) {
		 final String filename = filterName + ".xml";
		 final File filterFile = new File(this.getOverridingFiltersFolder(), filename);
		 if (filterFile.exists()) {
			 return this.loadFilter(filterFile);
		 } else {
			 return this.loadFilterFromResource("/org/phenoscape/filters/" + filename);
		 }
	 }

	 private Filter<IdentifiedObject> loadFilterFromResource(String resourcePath) {
		 return this.loadFilter(this.getClass().getResourceAsStream(resourcePath));
	 }

	 private Filter<IdentifiedObject> loadFilter(File xmlFile) {
		 try {
			 return this.loadFilter(new FileInputStream(xmlFile));
		 } catch (FileNotFoundException e) {
			 log().error("Could not find specified filter file", e);
		 }
		 return null;
	 }

	 @SuppressWarnings("unchecked")
	 private Filter<IdentifiedObject> loadFilter(InputStream stream) {
		 final XMLDecoder d = new XMLDecoder(stream);
		 final Filter<IdentifiedObject> result = (Filter<IdentifiedObject>) d.readObject();
		 d.close();
		 return result;
	 }
	 
	 private void loadProvisionalTerms() {
		 try {
			final List<OBOClass> terms = ProvisionalTermUtil.getProvisionalTerms(this.getOBOSession());
			for (OBOClass term : terms) {
				this.getOBOSession().addObject(term);	
			}
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 }

	 private Logger log() {
		 return Logger.getLogger(this.getClass());
	 }

	 public void invalidateAllTermSets() {
		 this.getEntityTermSet().invalidateTerms();
		 this.getTaxonTermSet().invalidateTerms();
		 this.getCollectionTermSet().invalidateTerms();
		 this.getUnitTermSet().invalidateTerms();
		 this.getRelationsTermSet().invalidateTerms();
		 this.getQualityTermSet().invalidateTerms();
		 this.getRelatedEntityTermSet().invalidateTerms();
		 this.getPostCompositionFillersTermSet().invalidateTerms();
		 this.getAllTermsSet().invalidateTerms();
	 }

}
