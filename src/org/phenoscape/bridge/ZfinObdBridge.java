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
import org.obd.model.CompositionalDescription;
import org.obd.model.Graph;
import org.obd.model.LinkStatement;
import org.obd.model.Node;
import org.obd.model.NodeAlias;
import org.obd.model.CompositionalDescription.Predicate;
import org.obd.model.Node.Metatype;
import org.obd.query.Shard;
import org.obd.query.impl.OBDSQLShard;
import org.obo.dataadapter.OBOAdapter;
import org.obo.dataadapter.OBOFileAdapter;
import org.obo.datamodel.Dbxref;
import org.obo.datamodel.IdentifiedObject;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOSession;
import org.obo.util.TermUtil;
import org.purl.obo.vocab.RelationVocabulary;

public class ZfinObdBridge {

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
    /** The phenotype-url system property should contain the URL of the ZFIN phenotypes file. */
    public static final String PHENOTYPE_URL = "phenotype-url";
    /** The missing-markers-url system property should contain the URL of the ZFIN missing markers file. */
    public static final String MISSING_MARKERS_URL = "missing-markers-url";
    /** The genotype-url system property should contain the URL of the ZFIN genotypes file. */
    public static final String GENOTYPE_URL = "genotype-url";
    /** The gene-name-url system property should contain the URL of the ZFIN genetic markers file. */
    public static final String GENE_NAME_URL = "gene-name-url"; 
    /** The morpholino-url system property should contain the URL of the ZFIN morpholinos file. */
    public static final String MORPHOLINO_URL = "morpholino-url";
    /** The pheno-environment-url system property should contain the URL of the ZFIN pheno environment file. */
    public static final String PHENO_ENVIRONMENT_URL = "pheno-environment-url";

    public static final String DATASET_TYPE_ID = "cdao:CharacterStateDataMatrix";
    public static final String GENOTYPE_PHENOTYPE_REL_ID = "PHENOSCAPE:exhibits";
    public static final String GENE_GENOTYPE_REL_ID = "PHENOSCAPE:has_allele";
    public static final String PUBLICATION_TYPE_ID = "PHENOSCAPE:Publication";
    public static final String HAS_PUB_REL_ID = "PHENOSCAPE:has_publication";
    public static final String GENOTYPE_TYPE_ID = "SO:0001027";
    public static final String GENE_TYPE_ID = "SO:0000704";
    public static final String POSITED_BY_REL_ID = "posited_by";
    
    private final String MORPHOLINO_STRING = "morpholino";
    private final String WILD_TYPE_STRING = "wild type (unspecified)";
    
    private static final RelationVocabulary relationVocabulary = new RelationVocabulary();

    private Shard shard;
    private Graph graph;
    private OBOSession oboSession;
    
    private Map<String, String> zfinGeneIdToNameMap;
    private Map<String, String> zfinGeneIdToSymbolMap;
    private Map<String, String> envToMorpholinoMap;
    private Map<String, String> morpholinoToGeneMap;
    private Map<String, String> genotypeToGeneMap;
    private Map<String, String> morpholinoIdToLabelMap;
    
    /*
     * This map has been created to keep track of main IDs and their mapping to alternate IDs
     * These are stored in key=ALTERNATE-ID value=ID format 
     */
    private Map<String, String> id2AlternateIdMap;
    
    public ZfinObdBridge() throws SQLException, ClassNotFoundException, IOException {
        super();
        this.shard = this.initializeShard();
        this.graph = new Graph();
        this.setOboSession(this.loadOBOSession());
        
        this.id2AlternateIdMap = createAltIdMappings(this.getOboSession());
        
        this.zfinGeneIdToNameMap = new HashMap<String, String>();
        this.zfinGeneIdToSymbolMap = new HashMap<String, String>();
        this.envToMorpholinoMap = new HashMap<String, String>();
        this.morpholinoToGeneMap = new HashMap<String, String>();
        this.genotypeToGeneMap = new HashMap<String, String>();
        this.morpholinoIdToLabelMap = new HashMap<String, String>();
        
        this.createZfinNameDirectory();
        this.mapEnvToMorpholino();
        this.mapMorpholinoToGene();
        this.mapGenotypeToGene();
        this.mapGenotypeToGeneViaMissingMarkers();
    }

    public OBOSession getOboSession() {
        return oboSession;
    }

    public void setOboSession(OBOSession oboSession) {
        this.oboSession = oboSession;
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
        
    private void createZfinNameDirectory() throws IOException{
    	URL geneticMarkersURL = new URL(System.getProperty(GENE_NAME_URL));
    	BufferedReader reader = new BufferedReader(new InputStreamReader(geneticMarkersURL.openStream()));
    	String line, zfinId, zfinName, zfinAlias; 
    	while((line = reader.readLine()) != null){
    		if(line.startsWith("ZDB-GENE")){
    			String[] lineComps = line.split("\\t");
    			zfinId = normalizetoZfin(lineComps[0]);
    			zfinAlias = lineComps[1];
    			zfinName = lineComps[2];
    			this.zfinGeneIdToNameMap.put(zfinId, zfinName);
    			this.zfinGeneIdToSymbolMap.put(zfinId, zfinAlias);
    		}
    	}
    	reader.close();
    }
    
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
    
    private void mapEnvToMorpholino() throws IOException{
    	String line, environmentId, morpholinoId;
    	
    	URL phenoEnvironmentURL = new URL(System.getProperty(PHENO_ENVIRONMENT_URL));
    	BufferedReader reader = new BufferedReader(new InputStreamReader(phenoEnvironmentURL.openStream()));
    	
    	while((line = reader.readLine()) != null){
    		String[] lComps = line.split("\\t");
    		if(lComps[1].equals(MORPHOLINO_STRING)){
    			environmentId = normalizetoZfin(lComps[0]);
    			morpholinoId = lComps[2];
    			this.envToMorpholinoMap.put(environmentId, morpholinoId);
    		}
    	}
    	
    	reader.close();
    }
    
    private void mapMorpholinoToGene() throws IOException{
    	String line, geneId, morpholinoId, morpholinoLabel;
    	
    	URL morpholinoURL = new URL(System.getProperty(MORPHOLINO_URL));
    	BufferedReader reader = new BufferedReader(new InputStreamReader(morpholinoURL.openStream()));
    	
    	while((line = reader.readLine()) != null){
    		String[] lComps = line.split("\\t");
   			geneId = this.normalizetoZfin(lComps[0]);
   			morpholinoId = lComps[2];
   			morpholinoLabel = lComps[3];
    		this.morpholinoToGeneMap.put(morpholinoId, geneId);
    		this.morpholinoIdToLabelMap.put(morpholinoId, morpholinoLabel);
    	}
    	
    	reader.close();
    }
    
    /**
     * Finds the equivalent TAO term for the given ZFA term
     * @param entityId
     * @return
     */
    private String getEquivalentTAOID(String entityId) {
    	for(OBOClass oboClass : TermUtil.getTerms(oboSession)){
         	if(oboClass.getID().equals(entityId)){
         		for(Dbxref dbx : oboClass.getDbxrefs()){
         			if(dbx.getDatabase().toString().equals("TAO")){
         				return dbx.getDatabase().toString() + ":" + dbx.getDatabaseID().toString();
         			}
         		}
         	}
        }
        return "Not found";
    }

    private CompositionalDescription postComposeTerms(String[] comps){
    	String aggregateEntityId, qualityId, componentEntityId, ab;
    	CompositionalDescription componentAggregateDesc = null;
    	
    	aggregateEntityId = comps[4];
    	qualityId = comps[6];
    	componentEntityId = comps[5];
    	ab = comps[7];
    	
    	if(aggregateEntityId != null){
    		aggregateEntityId = replaceZfinEntityWithTaoEntity(aggregateEntityId);
    		aggregateEntityId = replaceAlternateId(aggregateEntityId);
    	}
    	
    	if(componentEntityId != null && componentEntityId.trim().length() > 0 && componentEntityId.matches("[A-Z]+:[0-9]+")){
    		componentEntityId = replaceZfinEntityWithTaoEntity(componentEntityId);
    		componentEntityId = replaceAlternateId(componentEntityId);
    		
    		componentAggregateDesc = new CompositionalDescription(Predicate.INTERSECTION);
    		componentAggregateDesc.addArgument(componentEntityId);
    		componentAggregateDesc.addArgument(relationVocabulary.part_of(), aggregateEntityId);
    	}
    	
    	qualityId = replaceAlternateId(qualityId);
    	
    	if(ab != null){
    		for (String qual : ab.split("/")) {
    			if(qualityId.equals("PATO:0000001")){
    				String patoId = replaceDefaultQualityIdWithPatoId(qual);
    				qualityId = patoId;
    			}
    		}
    	}
    	
   		CompositionalDescription desc = new CompositionalDescription(Predicate.INTERSECTION);    	
   		desc.addArgument(qualityId);
   		if(componentAggregateDesc != null)
   			desc.addArgument(relationVocabulary.inheres_in(), componentAggregateDesc);
   		else
   			desc.addArgument(relationVocabulary.inheres_in(), aggregateEntityId);
   			
    	desc.setId(desc.generateId());
    	
   		return desc;
    }
    
    private String replaceZfinEntityWithTaoEntity(String zfinEntity){
    	String taoEntity = getEquivalentTAOID(zfinEntity);
    	String target = taoEntity.equals("Not found")? zfinEntity : taoEntity;
    	return target;
    }
    
    private String replaceAlternateId(String alternateId){
    	String id = alternateId;
    	if(id2AlternateIdMap.containsKey(alternateId)){
    		log().info("Replacing alternate ID: " + alternateId);
    		id = id2AlternateIdMap.get(alternateId);
    	}
    	return id;
    }
    
    private String replaceDefaultQualityIdWithPatoId(String qual){
    	String patoId;
		if (qual.equals("normal"))
			patoId = "PATO:0000461";
		else if (qual.equals("absent"))
			patoId = "PATO:0000462";
		else if (qual.equals("present"))
			patoId = "PATO:0000467";
		else
			patoId = "PATO:0000460"; // abnormal
    	return patoId;
    }

    private void mapGenotypeToGene() throws IOException{
    	String lineFromGenotypeToPhenotypeFile;
    	String genotypeId, geneId = null;
    	URL genotypeURL = new URL(System.getProperty(GENOTYPE_URL));
    
    	BufferedReader reader = new BufferedReader(new InputStreamReader(genotypeURL.openStream()));
    	while((lineFromGenotypeToPhenotypeFile = reader.readLine()) != null){
    		String[] comps = lineFromGenotypeToPhenotypeFile.split("\\t");
    		genotypeId = normalizetoZfin(comps[0]);
    		if(comps.length > 9){
    			geneId = normalizetoZfin(comps[9]);
    			this.genotypeToGeneMap.put(genotypeId, geneId);
    		}
    	}
    	
    	reader.close();
    }
    
    private void mapGenotypeToGeneViaMissingMarkers() throws IOException{
    	String lineFromFile;
    	String genotypeId, geneId;
    	URL missingMarkersURL = new URL(System.getProperty(MISSING_MARKERS_URL));
    	
    	BufferedReader reader = new BufferedReader(new InputStreamReader(missingMarkersURL.openStream()));
    	while((lineFromFile = reader.readLine()) != null){
    		String[] comps = lineFromFile.split("\\t");
			genotypeId = normalizetoZfin(comps[0]);
    		if(comps[4] != null && comps[4].trim().length() > 0){
    			geneId = normalizetoZfin(comps[4]);
    			this.genotypeToGeneMap.put(genotypeId, geneId);
    		}
    	}
    	
    	reader.close();
    }
    
    public void loadZfinData() throws MalformedURLException, IOException {
        String phenoFileLine;
        String genotypeId, genotype, pub, qualityId, environmentId, geneId = null; 
        LinkStatement genotypeToPhenotypeLink;
        
        URL phenotypeURL = new URL(System.getProperty(PHENOTYPE_URL));
        
        BufferedReader br1 = new BufferedReader(new InputStreamReader(phenotypeURL.openStream()));
        while ((phenoFileLine = br1.readLine()) != null) {
            String[] pComps = phenoFileLine.split("\\t");
            genotypeId = pComps[0];
            genotype = pComps[1];
            qualityId = pComps[6];
            pub = pComps[8];
            environmentId = pComps[9];
            
            if(genotype.equals(this.WILD_TYPE_STRING)){
            	genotypeId = this.envToMorpholinoMap.get(environmentId);
            	genotype = this.morpholinoIdToLabelMap.get(genotypeId);
            	if(genotypeId != null)
            		geneId = this.morpholinoToGeneMap.get(genotypeId);
            }
            else{
            	geneId = this.genotypeToGeneMap.get(genotypeId);
            }
            	
            if(geneId != null && genotypeId != null){
                CompositionalDescription cd = this.postComposeTerms(pComps);
            	String phenoId = cd.generateId();
            	cd.setId(phenoId);
            	graph.addStatements(cd);
            	
            	Node geneNode = createInstanceNode(geneId, GENE_TYPE_ID);
            	String geneName = this.zfinGeneIdToNameMap.get(geneId);
            	String geneSymbol = this.zfinGeneIdToSymbolMap.get(geneId);
            	if(geneName != null){
            		geneNode.setLabel(geneName);
            		NodeAlias na = new NodeAlias();
                	na.setNodeId(geneId);
                	na.setTargetId(geneSymbol);
                	graph.addStatement(na);
            	}
            	else{
            		geneNode.setLabel(geneSymbol);
            	}
            	graph.addNode(geneNode);
            	
           		Node genotypeNode = createInstanceNode(genotypeId, GENOTYPE_TYPE_ID);
           		if(genotype != null)
           			genotypeNode.setLabel(genotype);
           		graph.addNode(genotypeNode);
           		this.createLinkStatementAndAddToGraph(geneId, GENE_GENOTYPE_REL_ID, genotypeId);
           		genotypeToPhenotypeLink =
           			this.createLinkStatementAndAddToGraph(genotypeId, GENOTYPE_PHENOTYPE_REL_ID, phenoId);
            	
                /*//TODO This may have to be refined for future implementation Cartik 10/27/09
            	if (!pub.equals("")) {
            		Node publicationNode = createInstanceNode(pub, PUBLICATION_TYPE_ID);
            		graph.addNode(publicationNode);

            		String dsId = UUID.randomUUID().toString();
            		Node dsNode = createInstanceNode(dsId, DATASET_TYPE_ID);
            		graph.addNode(dsNode);

            		LinkStatement dsLink = new LinkStatement();
            		dsLink.setRelationId(POSITED_BY_REL_ID);
            		dsLink.setTargetId(dsId);
            		genotypeToPhenotypeLink.addSubStatement(dsLink);
            		
            		this.createLinkStatementAndAddToGraph(dsId, HAS_PUB_REL_ID, pub);
            	}
            	*/

            	this.createLinkStatementAndAddToGraph(phenoId, relationVocabulary.is_a(), qualityId);
            }
        }
        
        this.shard.putGraph(graph);
    }

    private String normalizetoZfin(String string) {
        return "ZFIN:" + string;
    }

    protected Node createInstanceNode(String id, String typeId) {
        Node n = new Node(id);
        n.setMetatype(Metatype.CLASS);
        n.addStatement(new LinkStatement(id, relationVocabulary.instance_of(),
                typeId));
        return n;
    }
    
    private LinkStatement createLinkStatementAndAddToGraph(String subject, String predicate, String object){
    	LinkStatement ls = new LinkStatement();
    	ls.setNodeId(subject);
    	ls.setRelationId(predicate);
    	ls.setTargetId(object);
    	
    	graph.addStatement(ls);
    	return ls;
    }
    
    public static void main(String[] args) throws SQLException, ClassNotFoundException, MalformedURLException, IOException {
        ZfinObdBridge zob = new ZfinObdBridge();
        zob.loadZfinData();
    }

}
