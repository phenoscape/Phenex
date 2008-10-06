package org.phenoscape.io;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.bbop.dataadapter.DataAdapterException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obo.dataadapter.OBOAdapter;
import org.obo.dataadapter.OBOFileAdapter;
import org.obo.datamodel.OBOSession;
import org.phenoscape.model.Character;

public class CharacterTabReaderTest {
  
  private static OBOSession session;
  
  @BeforeClass
  public static void initialize() throws DataAdapterException {
    OBOFileAdapter fileAdapter = new OBOFileAdapter();
    OBOFileAdapter.OBOAdapterConfiguration config = new OBOFileAdapter.OBOAdapterConfiguration();
    config.setReadPaths(Arrays.asList(new String[] {"test/testfiles/normal_test_ontology.obo"}));
    config.setBasicSave(false);
    config.setAllowDangling(true);
    config.setFollowImports(false);
    session = fileAdapter.doOperation(OBOAdapter.READ_ONTOLOGY, config, null);
  }

  @Test
  public void readNormalFile() throws IOException {
    final CharacterTabReader reader = new CharacterTabReader(new File("test/testfiles/CharacterTabReaderTestFile1.tab"), session);
    Assert.assertEquals("Reader should create same number of distinct characters as labeled in file", 72, reader.getCharacters().size());
    Assert.assertEquals("Reader should create same number of distinct states for a character as labeled in file", 2, reader.getCharacters().get(55).getStates().size());
    final Character character3 = reader.getCharacters().get(3);
    Assert.assertEquals("Symbol of first state of character 3 should be '0'", "0", character3.getStates().get(0).getSymbol());
    // TAO:0000178 is in the test ontology
    Assert.assertNotNull("A term ID in the OBOSession should have been found", character3.getStates().get(0).getPhenotypes().get(0).getEntity());
    // PATO:0001653 is not in the test ontology
    Assert.assertNull("A term ID not in the OBOSession should result in a null value", character3.getStates().get(0).getPhenotypes().get(0).getQuality());
    final Character character22 = reader.getCharacters().get(22);
    Assert.assertEquals("A parsable integer should be put in the Count field of the phenotype", 1, character22.getStates().get(0).getPhenotypes().get(0).getCount());
    Assert.assertNull("An unparsable count value should be null in the Count field of the phenotype", character22.getStates().get(1).getPhenotypes().get(0).getCount());
    Assert.assertEquals("An unparsable count value should be appended prefixed with a semicolon and 'count=' to the Note field of the phenotype", "need to complete; count=2 >/= 5", character22.getStates().get(1).getPhenotypes().get(0).getComment());
    final Character character41 = reader.getCharacters().get(41);
    Assert.assertEquals("An unparsable count value prefixed by 'count=' should be the entire note when no other note is present", "count=13, 14=/>17,18", character41.getStates().get(2).getPhenotypes().get(0).getComment());
  }
  
  @Test
  public void testReadLineLackingSomeTabs() throws IOException {
    // if this doesn't work an ArrayIndexOutOfBoundsException will be thrown
    final CharacterTabReader reader = new CharacterTabReader(new File("test/testfiles/CharacterTabReaderTestFile2.tab"), session);
    reader.getCharacters();
  }
  
}
