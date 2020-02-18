package org.phenoscape.io;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import org.apache.xmlbeans.XmlException;
import org.junit.Assert;
import org.junit.Test;
import org.obo.datamodel.OBOSession;
import org.obo.datamodel.impl.OBOClassImpl;
import org.obo.datamodel.impl.OBOSessionImpl;
import org.phenoscape.model.Phenotype;
import org.phenoscape.model.Taxon;

public class NeXMLReaderTest {
    
    @Test
    public void handleDanglingTerms() throws XmlException, IOException {
        final OBOSession session = new OBOSessionImpl();
        final NeXMLReader reader = new NeXMLReader(new File("testfiles/NeXMLReaderTestFile2.xml"), session);
        Assert.assertEquals("There should be 4 dangling terms", 4, reader.getDanglersList().size());
        final Taxon firstTaxon = reader.getDataSet().getTaxa().get(0);
        Assert.assertEquals("Taxon should have correct ID", "TTO:1052189", firstTaxon.getValidName().getID());
        Assert.assertEquals("Specimen should have correct ID", "COLLECTION:0000194", firstTaxon.getSpecimens().get(0).getCollectionCode().getID());
        final Phenotype phenotype = reader.getDataSet().getCharacters().get(0).getStates().get(0).getPhenotypes().get(0);
        Assert.assertEquals("Entity should have correct ID", "TAO:0001173", phenotype.getEntity().getID());
        Assert.assertEquals("Quality should have correct ID", "PATO:0000318", phenotype.getQuality().getID());
        final NeXMLWriter writer = new NeXMLWriter("dummy");
        writer.setDataSet(reader.getDataSet());
        final StringWriter stringWriter = new StringWriter();
        writer.write(stringWriter);
        final String xml = stringWriter.toString();
        Assert.assertTrue("Quick check that IDs of danglers can be successfully written", xml.contains(NeXMLUtil.oboURI(new OBOClassImpl("TTO:1052189")).toString()));
        Assert.assertTrue("Quick check that IDs of danglers can be successfully written", xml.contains(NeXMLUtil.oboURI(new OBOClassImpl("COLLECTION:0000194")).toString()));
        Assert.assertTrue("Quick check that IDs of danglers can be successfully written", xml.contains("TAO:0001173"));
        Assert.assertTrue("Quick check that IDs of danglers can be successfully written", xml.contains("PATO:0000318"));
    }

}
