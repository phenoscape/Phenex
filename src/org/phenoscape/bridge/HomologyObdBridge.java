package org.phenoscape.bridge;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.bbop.dataadapter.DataAdapterException;
import org.obd.model.Graph;
import org.obd.model.LinkStatement;
import org.obd.query.Shard;
import org.obd.query.impl.OBDSQLShard;
import org.obo.dataadapter.OBOAdapter;
import org.obo.dataadapter.OBOFileAdapter;
import org.obo.datamodel.IdentifiedObject;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOSession;
import org.purl.obo.vocab.RelationVocabulary;

public class HomologyObdBridge {
/**
 * This class loads homology assertions into the database
 */
	/** The db-host system property should contain the name of the database server. */
    public static final String DB_HOST = "db-host";
    /** The db-name system property should contain the name of the database. */
    public static final String DB_NAME = "db-name";
    /** The db-user system property should contain the database username. */
    public static final String DB_USER = "db-user";
    /** The db-password system property should contain the database password. */
    public static final String DB_PASSWORD = "db-password";
    /** The ontology-dir system property should contain the path to a folder with ontologies to be loaded. */
    public static final String ONTOLOGY_DIR = "ontology-dir";
    /** The homology-url system property should contain the URL of the homology file. */
    public static final String HOMOLOGY_URL = "homology-url";
    
    
    public static String HAS_PUB_REL_ID = "PHENOSCAPE:has_publication";
    public static String IN_TAXON_REL_ID = "PHENOSCAPE:in_taxon";
        
    private static RelationVocabulary relationVocabulary = new RelationVocabulary();
    
    private Shard shard;
    private Graph graph;
    private OBOSession oboSession;
    
    /*
     * This map has been created to keep track of main IDs and their mapping to alternate IDs
     * These are stored in key=ALTERNATE-ID value=ID format 
     */
    private Map<String, String> id2AlternateIdMap;
    
    public HomologyObdBridge() throws SQLException, ClassNotFoundException {
        super();
        this.shard = this.initializeShard();
        this.graph = new Graph();
        this.setOboSession(this.loadOBOSession());
        this.id2AlternateIdMap = createAltIdMappings(this.getOboSession());
    }
    
    public OBOSession getOboSession() {
        return oboSession;
    }

    public void setOboSession(OBOSession oboSession) {
        this.oboSession = oboSession;
    }
    
    public void loadHomologyData() throws MalformedURLException, IOException {
        String homologyFileLine;
        LinkStatement homologyLink;
        String anatId1, anatId2, taxonId1, taxonId2, pub, evidCode;
        
        URL homologyURL = new URL(System.getProperty(HOMOLOGY_URL));
        
        BufferedReader br = new BufferedReader(new InputStreamReader(homologyURL.openStream()));
        
        int j = 0;
        
        while((homologyFileLine = br.readLine()) != null){
        	String hComps[] = homologyFileLine.split("\\t");
        	pub = hComps[0];
        	anatId1 = hComps[1];
        	taxonId1 = hComps[3];
        	anatId2 = hComps[5];
        	taxonId2 = hComps[7];
        	evidCode = hComps[9];
        	System.out.println(++j + ". " + pub + " specifies " + anatId1 + " in " + taxonId1 + " is homologous to " +
        				anatId2 + " in " + taxonId2 + " as per evidence " + evidCode);
        }
    }
    
    public static void main(String[] args) throws SQLException, ClassNotFoundException, MalformedURLException, IOException {
        HomologyObdBridge hob = new HomologyObdBridge();
        hob.loadHomologyData();
    }
    
    private OBOSession loadOBOSession() {
        final OBOFileAdapter fileAdapter = new OBOFileAdapter();
        OBOFileAdapter.OBOAdapterConfiguration config = new OBOFileAdapter.OBOAdapterConfiguration();
        config.setReadPaths(this.getOntologyPaths());
        config.setBasicSave(false);
        config.setAllowDangling(true);
        config.setFollowImports(false);
        try {
            return fileAdapter.doOperation(OBOAdapter.READ_ONTOLOGY, config, null);
        } catch (DataAdapterException e) {
            log().fatal("Failed to load ontologies", e);
            return null;
        }
    }

    private List<String> getOntologyPaths() {
        List<String> paths = new ArrayList<String>();
        File ontCache = new File(System.getProperty(ONTOLOGY_DIR));
        for (File f : ontCache.listFiles()) {
            paths.add(f.getAbsolutePath());
            log().trace(paths.toString());
        }
        return paths;
    }
    
    private Shard initializeShard() throws SQLException, ClassNotFoundException {
        OBDSQLShard obdsql = new OBDSQLShard();
        obdsql.connect("jdbc:postgresql://" + System.getProperty(DB_HOST) + "/" + System.getProperty(DB_NAME), System.getProperty(DB_USER), System.getProperty(DB_PASSWORD));
        return obdsql;
    }

    private Logger log() {
        return Logger.getLogger(this.getClass());
    }
    
    
    /**
     * This method populates the alternate id to id map
     */
    
    private Map<String, String> createAltIdMappings(OBOSession session) {
    	Map<String, String> altIDMappings = new HashMap<String, String>();
        log().trace("Called alt_id search");
        final Collection<IdentifiedObject> terms = session.getObjects();
        for (IdentifiedObject object : terms) {
            if (object instanceof OBOClass) {
                final OBOClass term = (OBOClass)object;
                if (term.getSecondaryIDs() != null && term.getSecondaryIDs().size() > 0) {
                	for(String altID : term.getSecondaryIDs())
                		altIDMappings.put(altID, term.getID());
                }
            }
        }
        return altIDMappings;
    }
}
