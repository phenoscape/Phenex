package org.phenoscape.bridge;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.bbop.dataadapter.DataAdapterException;
import org.obd.model.Graph;
import org.obd.query.Shard;
import org.obd.query.impl.OBDSQLShard;
import org.obo.dataadapter.OBOAdapter;
import org.obo.dataadapter.OBOFileAdapter;
import org.obo.datamodel.OBOSession;
import org.phenoscape.io.NeXMLReader;
import org.phenoscape.io.nexml_1_0.NeXMLReader_1_0;
import org.phenoscape.model.DataSet;

public class PhenoscapeDataLoader {

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
    /** The file-loc system property should contain the path to where the error log will be stored. */
    public static final String FILE_LOC = "file-loc";
    
    private Shard shard;
    private OBOSession session;
    
    private String file = System.getProperty(FILE_LOC) + "/problemLog.txt";

    public PhenoscapeDataLoader() throws SQLException, ClassNotFoundException {
        this.shard = this.initializeShard();
        this.session = this.loadOBOSession();
    }

    public static void main(String[] args) throws XmlException, IOException, SQLException, ClassNotFoundException {
        PhenoscapeDataLoader pdl = new PhenoscapeDataLoader();
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(pdl.file)));
        bw.write("PROBLEM LOG" + "\n\n\n");
        pdl.processDataFolder(new File(args[0]), bw);
        bw.flush();
        bw.close();
    }

    private void processDataFolder(File folder, BufferedWriter bw) {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                this.processDataFolder(file, bw);
            } else if (file.getName().endsWith(".xml")) {
                try {
                    this.processDataFile(file, bw);
                } catch (XmlException e) {
                    log().error("Failed parsing " + file, e);
                } catch (IOException e) {
                    log().error("Failed reading " + file, e);
                }catch (Exception e){ //Adding this so loading doesnt stop if one file has a problem; Cartik
                	log().error("Failed reading " + file, e);
                }
                
            }
        }
    }

    private void processDataFile(File file, BufferedWriter bw) throws IOException, XmlException {
        log().info("Started work with " + file.getAbsolutePath());
        DataSet ds ;
        try{
        	NeXMLReader_1_0 reader = new NeXMLReader_1_0(file, this.session);
        	ds = reader.getDataSet();
        }
        catch(XmlException xmle){
        	NeXMLReader reader = new NeXMLReader(file, this.session);
        	ds = reader.getDataSet();
        }
        OBDModelBridge bridge = new OBDModelBridge();
        Graph g = bridge.translate(ds, file, bw);
        this.shard.putGraph(g);
        log().info(g.getStatements().size() + " records added");
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
