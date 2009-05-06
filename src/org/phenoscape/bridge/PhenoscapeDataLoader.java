package org.phenoscape.bridge;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
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
    
    private Shard shard;
    private OBOSession session;

    public PhenoscapeDataLoader() throws SQLException, ClassNotFoundException {
        this.shard = this.initializeShard();
        this.session = this.loadOBOSession();
    }

    public static void main(String[] args) throws XmlException, IOException, SQLException, ClassNotFoundException {
        BasicConfigurator.configure();
        PhenoscapeDataLoader pdl = new PhenoscapeDataLoader();
        pdl.processDataFolder(new File(args[0]));
    }

    private void processDataFolder(File folder) {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                this.processDataFolder(file);
            } else if (file.getName().endsWith(".xml")) {
                try {
                    this.processDataFile(file);
                } catch (XmlException e) {
                    log().error("Failed parsing " + file, e);
                } catch (IOException e) {
                    log().error("Failed reading " + file, e);
                }
            }
        }
    }

    private void processDataFile(File file) throws XmlException, IOException {
        log().info("Started work with " + file.getAbsolutePath());
        NeXMLReader reader = new NeXMLReader(file, this.session);
        DataSet ds = reader.getDataSet();
        OBDModelBridge bridge = new OBDModelBridge();
        Graph g = bridge.translate(ds, file);
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
