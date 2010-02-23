package org.phenoscape.main;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.nexml.schema_2009.NexmlDocument;
import org.obo.annotation.base.UserOntologyConfiguration;
import org.obo.datamodel.OBOSession;
import org.phenoscape.controller.OntologyController;
import org.phenoscape.io.NeXMLReader;
import org.phenoscape.io.NeXMLWriter;
import org.phenoscape.io.nexml_1_0.NeXMLReader_1_0;
import org.phenoscape.model.DataSet;

/**
 * This is a little program used to update an entire folder of data files to the latest NeXML schema.
 * @author Jim Balhoff
 *
 */
public class UpdateDataFiles {

    private static final String PATH_TO_DATA_FOLDER = "/Users/jim/Work/Phenoscape/phenex-files";
    private final OBOSession session = new OntologyController(new UserOntologyConfiguration()).getOBOSession();


    public static void main(String[] args) throws IOException, XmlException {
        final UpdateDataFiles app = new UpdateDataFiles();
        app.processDataFolder(new File(PATH_TO_DATA_FOLDER));
    }

    private void processDataFolder(File folder) throws IOException, XmlException {
        log().info("Processing folder: " + folder);
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                this.processDataFolder(file);
            } else if (file.getName().endsWith(".xml")) {
                this.processDataFile(file);
            }
        }
    }

    private void processDataFile(File file) throws IOException, XmlException {
        log().info("Processing file: " + file);
        DataSet dataset;
        String charactersBlockID;
        NexmlDocument xmlDoc = NexmlDocument.Factory.newInstance();
        try {
            final NeXMLReader reader = new NeXMLReader(file, this.session);
            dataset = reader.getDataSet();
            charactersBlockID = reader.getCharactersBlockID();
            xmlDoc = reader.getXMLDoc();
        } catch (XmlException e) {
            log().info("Trying old NeXML format for: " + file);
            final NeXMLReader_1_0 reader = new NeXMLReader_1_0(file, this.session);
            dataset = reader.getDataSet();
            charactersBlockID = reader.getCharactersBlockID();
        }
        final NeXMLWriter writer = new NeXMLWriter(charactersBlockID, xmlDoc);
        writer.setDataSet(dataset);
        writer.setGenerator("PhenexUpdateScript: " + DateFormat.getDateTimeInstance().format(new Date()));
        writer.write(file);
    }

    private Logger log() {
        return Logger.getLogger(this.getClass());
    }

}
