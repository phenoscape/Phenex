package org.phenoscape.bridge;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import org.obo.datamodel.Dbxref;
import org.obo.datamodel.IdentifiedObject;
import org.obo.datamodel.Namespace;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOSession;
import org.obo.datamodel.Type;
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

    public static final String GENOTYPE_PHENOTYPE_REL_ID = "PHENOSCAPE:exhibits";
    public static final String GENE_GENOTYPE_REL_ID = "PHENOSCAPE:has_allele";
    public static final String PUBLICATION_TYPE_ID = "cdao:Pub";
    public static final String HAS_PUB_REL_ID = "cdao:hasPub";
    public static final String GENOTYPE_TYPE_ID = "SO:0001027";
    public static final String GENE_TYPE_ID = "SO:0000704";
    private static final String TELEOST_ANATOMY = "teleost_anatomy";
    private static final RelationVocabulary relationVocabulary = new RelationVocabulary();

    private Shard shard;
    private Graph graph;
    private OBOSession oboSession;
    
    public ZfinObdBridge() throws SQLException, ClassNotFoundException {
        super();
        this.shard = this.initializeShard();
        this.graph = new Graph();
        this.setOboSession(this.loadOBOSession());
    }

    public OBOSession getOboSession() {
        return oboSession;
    }

    public void setOboSession(OBOSession oboSession) {
        this.oboSession = oboSession;
    }

    public void loadZfinData() throws MalformedURLException, IOException {
        String phenoFileLine, genoFileLine, missingMarkersFileLine;
        LinkStatement geneAnnotLink;

        URL phenotypeURL = new URL(System.getProperty(PHENOTYPE_URL));
        URL missingMarkersURL = new URL(System.getProperty(MISSING_MARKERS_URL));
        URL genotypeURL = new URL(System.getProperty(GENOTYPE_URL));

        BufferedReader br1 = new BufferedReader(new InputStreamReader(phenotypeURL.openStream()));
        while ((phenoFileLine = br1.readLine()) != null) {
            String[] pComps = phenoFileLine.split("\\t");
            String pub = pComps[8];
            String qualID = pComps[5];
            String ab = pComps[7];
            String entity1Id = pComps[4];
            String genotypeId = pComps[0];
            String genotypeName = pComps[1];
            String towardsId = pComps[6];

            Node genotypeNode = createInstanceNode(genotypeId, GENOTYPE_TYPE_ID);
            genotypeNode.setLabel(genotypeName);
            graph.addNode(genotypeNode);

            Node publicationNode = createInstanceNode(pub, PUBLICATION_TYPE_ID);
            graph.addNode(publicationNode);

            Set<LinkStatement> diffs = new HashSet<LinkStatement>();
            if (entity1Id != null) {
                String taoId = getEquivalentTAOID(entity1Id);
                String target = taoId != null?taoId : entity1Id;
                LinkStatement d = new LinkStatement();
                d.setRelationId(relationVocabulary.inheres_in());
                d.setTargetId(target);
                diffs.add(d);
            }
            if (towardsId != null) {
                String taoId = getEquivalentTAOID(towardsId);
                String target = taoId != null?taoId : towardsId;
                if(target != null && target.trim().length() > 0 && target.matches("[A-Z]+:[0-9]+")){
                    LinkStatement d = new LinkStatement();
                    d.setRelationId(relationVocabulary.towards());
                    d.setTargetId(target);
                    diffs.add(d);
                }
            }
            String quality = qualID;
            if(ab != null){
                for (String qual : ab.split("/")) {
                    String patoId;
                    if(qualID.equals("PATO:0000001")){
                        if (qual.equals("normal"))
                            patoId = "PATO:0000461";
                        else if (qual.equals("absent"))
                            patoId = "PATO:0000462";
                        else if (qual.equals("present"))
                            patoId = "PATO:0000467";
                        else
                            patoId = "PATO:0000460"; // abnormal
                        quality = patoId;
                    }
                }
            }

            CompositionalDescription desc = new CompositionalDescription(quality, diffs);
            String phenoId = desc.generateId();
            desc.setId(phenoId);
            graph.addStatements(desc);

            LinkStatement annot = new LinkStatement();
            annot.setNodeId(genotypeId);
            annot.setRelationId(GENOTYPE_PHENOTYPE_REL_ID);

            if (!pub.equals("")) {
                LinkStatement pubLink = new LinkStatement();
                pubLink.setRelationId("posited_by");
                pubLink.setTargetId(pub);
                annot.addSubStatement(pubLink);
            }

            annot.setTargetId(phenoId);
            graph.addStatement(annot);

            LinkStatement ls = new LinkStatement();
            ls.setNodeId(phenoId);
            ls.setRelationId(relationVocabulary.is_a());
            ls.setTargetId(quality);

            graph.addStatement(ls);
        }
        
        BufferedReader br2 = new BufferedReader(new InputStreamReader(
                genotypeURL.openStream()));
        BufferedReader br3 = new BufferedReader(new InputStreamReader(
                missingMarkersURL.openStream()));

        while ((genoFileLine = br2.readLine()) != null) {
            String geneID = "", geneName = "";
            String[] gComps = genoFileLine.split("\\t");
            String genotypeID = normalizetoZfin(gComps[0]);
            if (gComps.length > 9) {
                geneID = normalizetoZfin(gComps[9]);
                geneName = gComps[8];
            }

            if (geneID != null && geneID.trim().length() > 0) {
                Node geneNode = createInstanceNode(geneID, GENE_TYPE_ID);
                geneNode.setLabel(geneName);
                graph.addNode(geneNode);
                geneAnnotLink = new LinkStatement();
                geneAnnotLink.setNodeId(geneID);
                geneAnnotLink.setRelationId(GENE_GENOTYPE_REL_ID);
                geneAnnotLink.setTargetId(genotypeID);
                graph.addStatement(geneAnnotLink);
            } else {
                while ((missingMarkersFileLine = br3.readLine()) != null) {
                    String[] mmComps = missingMarkersFileLine
                    .split("\\t");
                    if (mmComps[0].equals(gComps[0])) {
                        if (mmComps[4] != null
                                && mmComps[4].trim().length() > 0) {
                            geneID = normalizetoZfin(mmComps[4]);
                            Node geneNode = createInstanceNode(geneID,
                                    GENE_TYPE_ID);
                            geneNode.setLabel(mmComps[3]);
                            graph.addNode(geneNode);
                            geneAnnotLink = new LinkStatement();
                            geneAnnotLink.setNodeId(geneID);
                            geneAnnotLink
                            .setRelationId(GENE_GENOTYPE_REL_ID);
                            geneAnnotLink.setTargetId(genotypeID);
                            graph.addLinkStatement(geneNode, GENE_GENOTYPE_REL_ID, genotypeID);
                        }
                    }
                }
            }
        }
        this.shard.putGraph(graph);
    }

    public String getEquivalentTAOID(String entityId) {
        for(IdentifiedObject io : oboSession.getObjects()){
            Namespace ns = io.getNamespace();
            Type<OBOClass> type = io.getType();
            if(ns != null && ns.toString().equals(TELEOST_ANATOMY) &&
                    type.toString().equals("obo:TERM")){
                OBOClass oClass = (OBOClass)io;
                for(Dbxref dbx : oClass.getDbxrefs()){
                    String zfaId = dbx.getDatabase().toString() + ":" + dbx.getDatabaseID().toString();
                    if(zfaId.equals(entityId)){
                        return oClass.getID();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Another helper method to deal with the prefix inconsistencies in ZFIN
     * Gene to Genotype files lack the ZFIN prefix Genotype to Phenotype files
     * have the prefix
     * 
     * @param string
     * @return
     */
    private String normalizetoZfin(String string) {
        return "ZFIN:" + string;
    }

    protected Node createInstanceNode(String id, String typeId) {
        Node n = new Node(id);
        n.setMetatype(Metatype.CLASS);
        n.addStatement(new LinkStatement(id, relationVocabulary.instance_of(),
                typeId));
        graph.addNode(n);
        return n;
    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException, MalformedURLException, IOException {
        ZfinObdBridge zob = new ZfinObdBridge();
        zob.loadZfinData();
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
            if (f.getAbsolutePath().contains("zebrafish_anatomy") || f.getName().contains("teleost_anatomy")) 
                paths.add(f.getAbsolutePath());
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
    
}
