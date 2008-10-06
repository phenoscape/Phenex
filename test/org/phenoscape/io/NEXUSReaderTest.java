package org.phenoscape.io;

import java.io.File;
import java.io.IOException;

import org.biojava.bio.seq.io.ParseException;
import org.junit.Assert;
import org.junit.Test;
import org.phenoscape.model.DataSet;
import org.phenoscape.model.State;

public class NEXUSReaderTest {
  
  @Test
  public void readNormalFile() throws ParseException, IOException {
    final int expectedCharacterCount = 5; // this comes from the test input file
    final NEXUSReader reader = new NEXUSReader(new File("test/testfiles/NEXUSReaderTestFile3.nex"));
    final DataSet data = reader.getDataSet();
    Assert.assertEquals("Should read correct number of characters", expectedCharacterCount, data.getCharacters().size());
    Assert.assertEquals("Should read character label", "char2", data.getCharacters().get(1).getLabel());
    Assert.assertNull("Missing data (?) should become null state", this.getCellValue(data, 0, 3));
  }
  
  @Test
  public void readMixOfLabeledAndUnlabeledCharacters() throws ParseException, IOException {
    final int expectedCharacterCount = 147; // this comes from the test input file
    final NEXUSReader reader = new NEXUSReader(new File("test/testfiles/NEXUSReaderTestFile1.nex"));
    Assert.assertEquals("The reader should create the number of characters present in the matrix, even if some don't have labels", expectedCharacterCount, reader.getCharacters().size());
    Assert.assertEquals("The character labels should be assigned to the correct character", "12.6", (reader.getCharacters().get(17 - 1)).getLabel());
  }
  
  @Test
  public void readFileWithNoCharacterOrStateLabels() throws ParseException, IOException {
    final int expectedCharacterCount = 3; // this comes from the test input file
    final NEXUSReader reader = new NEXUSReader(new File("test/testfiles/NEXUSReaderTestFile2.nex"));
    Assert.assertEquals("The reader should create the number of characters present in the matrix", expectedCharacterCount, reader.getCharacters().size());
    Assert.assertEquals("The second character should have 3 states used in the matrix", 3, reader.getCharacters().get(1).getStates().size());
  }
  
  private State getCellValue(DataSet data, int row, int column) {
    return data.getStateForTaxon(data.getTaxa().get(row), data.getCharacters().get(column));
  }

}
