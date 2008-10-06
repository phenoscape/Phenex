package org.phenoscape.io;

import java.io.File;
import java.io.IOException;

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
    final TaxonTabReader reader = new TaxonTabReader(new File("test/testfiles/TaxonTabReaderTestFile1.tab"), session, termset);
    reader.getTaxa();
  }

}
