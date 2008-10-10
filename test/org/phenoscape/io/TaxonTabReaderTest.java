package org.phenoscape.io;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.obo.datamodel.OBOSession;
import org.obo.datamodel.impl.OBOSessionImpl;
import org.phenoscape.model.TermSet;

public class TaxonTabReaderTest {
  
  @Test
  public void testReadLineLackingSomeTabs() throws IOException {
    // if this doesn't work an ArrayIndexOutOfBoundsException will be thrown
    final OBOSession session = new OBOSessionImpl();
    final TermSet termset = new TermSet();
    termset.setOBOSession(session);
    final TaxonTabReader reader = new TaxonTabReader(new File("testfiles/TaxonTabReaderTestFile1.tab"), session, termset);
    Assert.assertEquals("Should have read 5 taxa", 5, reader.getDataSet().getTaxa().size());
  }
  
  @Test
  public void testNullForEmptyPubName() throws IOException {
      final OBOSession session = new OBOSessionImpl();
      final TermSet termset = new TermSet();
      termset.setOBOSession(session);
      final TaxonTabReader reader = new TaxonTabReader(new File("testfiles/TaxonTabReaderTestFile1.tab"), session, termset);
      Assert.assertNull("Fifth taxon should have null publication name, not empty string", reader.getDataSet().getTaxa().get(4).getPublicationName());
  }

}
