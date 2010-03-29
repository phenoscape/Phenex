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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.bbop.dataadapter.DataAdapterException;
import org.obd.model.CompositionalDescription;
import org.obd.model.Graph;
import org.obd.model.LinkStatement;
import org.obd.model.Node;
import org.obd.model.Node.Metatype;
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
    public static String HOMOLOG_TO_ID = "OBO_REL:homologous_to";
    public static String HAS_EVID_CODE_ID = "PHENOSCAPE:has_evidence_code";
    
    public static final String PUBLICATION_TYPE_ID = "PHENOSCAPE:Publication";
    
    private static final RelationVocabulary relationVocabulary = new RelationVocabulary();
    
    private Shard shard;
    private Graph graph;
    private OBOSession oboSession;
    
    /*
     * This map has been created to keep track of main IDs and their mapping to alternate IDs
     * These are stored in key=ALTERNATE-ID value=ID format 
     */
    private Map<String, String> id2AlternateIdMap;
    
    /**
     * This constructor initializes the Shard, the Graph and the OBOSession instance
     * parameters of the class. Also all the alternate IDs from the loaded ontologies
     * are mapped to their main IDs
     * @throws SQLException
     * @throws ClassNotFoundException
     */
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
    
    /**
     * @author cartik
     * @PURPOSE: This method adds the contents of the homology file 
     * to the OBD database
     * @PROCEDURE: Each line of the homology file is read and the tab delimited
     * columns are converted into a compositional description (post-composition)
     * of the type 
     * <AnatomicalEntity>^PHENOSCAPE:in_taxon(<Taxon>) OBO_REL:homologous_to <AnatomicalEntity>^PHENOSCAPE:in_taxon(<Taxon>) 
     * @throws MalformedURLException
     * @throws IOException
     */
    public void loadHomologyData() throws MalformedURLException, IOException {
        String homologyFileLine;
        String anatId1, anatId2, taxonId1, taxonId2, pub, evidCode;
        
        URL homologyURL = new URL(System.getProperty(HOMOLOGY_URL));
        
        BufferedReader br = new BufferedReader(new InputStreamReader(homologyURL.openStream()));
        
        int j = 0;
        
        /*
         * Read in the homology file
         */
        while((homologyFileLine = br.readLine()) != null){
        	String hComps[] = homologyFileLine.split("\\t");
        	pub = hComps[0];
        	anatId1 = hComps[1];
        	taxonId1 = hComps[3];
        	anatId2 = hComps[5];
        	taxonId2 = hComps[7];
        	evidCode = hComps[9];
        	/*
        	 * This IF block has been added to eliminate the title row which is
        	 * Entity1Id     Entity1    Entity2Id .....
        	 */
        	if(anatId1.contains("TAO") && anatId2.contains("TAO") 
        			&& taxonId1.contains("TTO") && taxonId2.contains("TTO")){
        		log().trace(++j + ". " + pub + " specifies " + anatId1 + " in " + 
        				taxonId1 + " is homologous to " + anatId2 + " in " + taxonId2 + 
        				" as per evidence " + evidCode);
        		
        		/*
        		 * Substitute main IDs for alternate IDs
        		 */
        		anatId1 = id2AlternateIdMap.containsKey(anatId1)?id2AlternateIdMap.get(anatId1):anatId1;
        		anatId2 = id2AlternateIdMap.containsKey(anatId2)?id2AlternateIdMap.get(anatId2):anatId2;
        		taxonId1 = id2AlternateIdMap.containsKey(taxonId1)?id2AlternateIdMap.get(taxonId1):taxonId1;
        		taxonId2 = id2AlternateIdMap.containsKey(taxonId2)?id2AlternateIdMap.get(taxonId2):taxonId2;

        		Set<LinkStatement> diff1 = new HashSet<LinkStatement>();
        		Set<LinkStatement> diff2 = new HashSet<LinkStatement>();

        		LinkStatement inTaxonStmt1 = new LinkStatement();
        		inTaxonStmt1.setRelationId(IN_TAXON_REL_ID);
        		inTaxonStmt1.setTargetId(taxonId1);
        		diff1.add(inTaxonStmt1);

        		CompositionalDescription lhHomologCd = new CompositionalDescription(anatId1, diff1);
        		String lhId = lhHomologCd.generateId();
        		lhHomologCd.setId(lhId);
        		graph.addStatements(lhHomologCd);

        		LinkStatement inTaxonStmt2 = new LinkStatement();
        		inTaxonStmt2.setRelationId(IN_TAXON_REL_ID);
        		inTaxonStmt2.setTargetId(taxonId2);
        		diff2.add(inTaxonStmt2);

        		/*
        		 * This will create a statement of the form
* <AnatomicalEntity>^PHENOSCAPE:in_taxon(<Taxon>)   OBO_REL:homologous_to    <AnatomicalEntity>^PHENOSCAPE:in_taxon(<Taxon>)
        		 */
        		CompositionalDescription rhHomologCd = new CompositionalDescription(anatId2, diff2);
        		String rhId = rhHomologCd.generateId();
        		rhHomologCd.setId(rhId);
        		graph.addStatements(rhHomologCd);

        		LinkStatement homologStmt = new LinkStatement();
        		homologStmt.setNodeId(lhId);
        		homologStmt.setRelationId(HOMOLOG_TO_ID);
        		homologStmt.setTargetId(rhId);

        		//reification link for publication
        		if(pub != null && pub.trim().length() > 0){
        			Node publicationNode = createInstanceNode(pub, PUBLICATION_TYPE_ID);
        			graph.addNode(publicationNode);
        			homologStmt.addSubLinkStatement(HAS_PUB_REL_ID, publicationNode.getId());
        		}
        		
        		//reification link for evidence code
        		if(evidCode != null && evidCode.trim().length() > 0){
        			homologStmt.addSubLinkStatement(HAS_EVID_CODE_ID, evidCode);
        		}

        		graph.addStatement(homologStmt);
        	}
        }
        shard.putGraph(graph);
    }
    
    /**
     * This method loads in all the ontologies in a specific location
     * and creates an OBOSession instance containing all the extracted
     * terms
     * @return
     */
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

    /**
     * A helper method to load in all the locations where
     * the ontologies are stored
     * @return
     */
    private List<String> getOntologyPaths() {
        List<String> paths = new ArrayList<String>();
        File ontCache = new File(System.getProperty(ONTOLOGY_DIR));
        for (File f : ontCache.listFiles()) {
            paths.add(f.getAbsolutePath());
            log().trace(paths.toString());
        }
        return paths;
    }
    
    /**
     * This method connects the shard to the database given the systems
     * parameters for DB location, name, DB username and DB password
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    private Shard initializeShard() throws SQLException, ClassNotFoundException {
        OBDSQLShard obdsql = new OBDSQLShard();
        obdsql.connect("jdbc:postgresql://" + System.getProperty(DB_HOST) + "/" + System.getProperty(DB_NAME), System.getProperty(DB_USER), System.getProperty(DB_PASSWORD));
        return obdsql;
    }

    private Logger log() {
        return Logger.getLogger(this.getClass());
    }
    
    
    /**
     * This method uses the OBOSession argument to find all
     * alternate IDs in the loaded ontologies and map them
     * to their resp main IDs in the altID map
     * @param session
     * @return
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
    
    /**
     * This method creates a Node for an instance of the {@param typeId} and 
     * adds the Node to the graph
     * @param id
     * @param typeId
     * @return
     */
    protected Node createInstanceNode(String id, String typeId) {
        Node n = new Node(id);
        n.setMetatype(Metatype.CLASS);
        n.addStatement(new LinkStatement(id, relationVocabulary.instance_of(),
                typeId));
        graph.addNode(n);
        return n;
    }
    
    /**
     * MAin method creates an instance of this class and calls the method
     * to load the homology data
     * @param args
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws MalformedURLException
     * @throws IOException
     */
    public static void main(String[] args) throws SQLException, ClassNotFoundException, MalformedURLException, IOException {
        HomologyObdBridge hob = new HomologyObdBridge();
        hob.loadHomologyData();
    }
}
