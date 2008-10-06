package org.phenoscape.io;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.bioontologies.obd.schema.pheno.PhenotypeDocument;
import org.nexml.x10.AbstractBlock;
import org.nexml.x10.AbstractChar;
import org.nexml.x10.AbstractObs;
import org.nexml.x10.AbstractObsMatrix;
import org.nexml.x10.AbstractObsRow;
import org.nexml.x10.AbstractState;
import org.nexml.x10.AbstractStates;
import org.nexml.x10.Annotated;
import org.nexml.x10.Dict;
import org.nexml.x10.NexmlDocument;
import org.nexml.x10.StandardCells;
import org.nexml.x10.StandardChar;
import org.nexml.x10.StandardFormat;
import org.nexml.x10.StandardStates;
import org.nexml.x10.Taxa;
import org.obo.datamodel.IdentifiedObject;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOSession;
import org.phenoscape.io.PhenoXMLPhenotypeWrapper.PhenotypeWrapperFactory;
import org.phenoscape.model.Character;
import org.phenoscape.model.DataSet;
import org.phenoscape.model.Specimen;
import org.phenoscape.model.State;
import org.phenoscape.model.Taxon;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import phenote.dataadapter.phenoxml.PhenoXmlAdapter;
import phenote.datamodel.PhenotypeCharacterI;

public class NeXMLReader {
  
  private final DataSet data = new DataSet();
  private final NexmlDocument xmlDoc;
  private final OBOSession session;
  private String charactersBlockID = UUID.randomUUID().toString();

  public NeXMLReader(File aFile, OBOSession session) throws XmlException, IOException {
    this.session = session;
    this.xmlDoc = NexmlDocument.Factory.parse(aFile);
    this.parseNeXML();
  }
  
  public NeXMLReader(Reader aReader, OBOSession session) throws XmlException, IOException {
    this.session = session;
    this.xmlDoc = NexmlDocument.Factory.parse(aReader);
    this.parseNeXML();
  }
  
  public DataSet getDataSet() {
    return this.data;
  }
  
  public NexmlDocument getXMLDoc() {
    return this.xmlDoc;
  }
  
  public String getCharactersBlockID() {
    return this.charactersBlockID;
  }
  
  private void parseNeXML() {
    final Dict metadata = NeXMLUtil.findOrCreateMetadataDict(this.xmlDoc);
    this.parseMetadata(metadata);
    for (AbstractBlock block : this.xmlDoc.getNexml().getCharactersArray()) {
      if (block instanceof StandardCells) {
        this.charactersBlockID = block.getId();
        final StandardCells cells = (StandardCells)block;
        this.parseStandardCells(cells);
        final Taxa taxa = NeXMLUtil.findOrCreateTaxa(this.xmlDoc, cells.getOtus());
        this.parseTaxa(taxa);
        final AbstractObsMatrix abstractMatrix = cells.getMatrix();
        if (abstractMatrix != null) {
          this.parseMatrix(abstractMatrix);
        }
        break;
      }
    }
  }
  
  private void parseStandardCells(StandardCells standardCells) {
    if (!(standardCells.getFormat() instanceof StandardFormat)) return;
    final StandardFormat format = (StandardFormat)(standardCells.getFormat());
    for (AbstractChar abstractChar : format.getCharArray()) {
      if (!(abstractChar instanceof StandardChar)) continue;
      final StandardChar standardChar = (StandardChar)abstractChar;
      final Character newCharacter;
      if (standardChar.getStates() != null) {
        newCharacter = new Character(standardChar.getId(), standardChar.getStates());
      } else {
        newCharacter = new Character(standardChar.getId());
      }
      newCharacter.setLabel(standardChar.getLabel());
      newCharacter.setComment(this.getComment(standardChar));
      final AbstractStates states = NeXMLUtil.findOrCreateStates(format, newCharacter.getStatesNexmlID());
      if (states instanceof StandardStates) {
        for (AbstractState abstractState : states.getStateArray()) {
          final State newState = new State(abstractState.getId());
          newState.setSymbol(this.readSymbol(abstractState));
          newState.setLabel(abstractState.getLabel());
          newState.setComment(this.getComment(abstractState));
          final Dict phenotypeDict = NeXMLUtil.findOrCreateDict(abstractState, "OBO_phenotype", abstractState.getDomNode().getOwnerDocument().createElement("any"));
          final Element any = NeXMLUtil.getFirstChildWithTagName((Element)(phenotypeDict.getDomNode()), "any");
          final Element phenoXML = NeXMLUtil.getFirstChildWithTagNameNS(any, "http://www.bioontologies.org/obd/schema/pheno", "phenotype");
          if (phenoXML != null) {
            try {
              PhenotypeDocument xmlPhen = org.bioontologies.obd.schema.pheno.PhenotypeDocument.Factory.parse(phenoXML);
              PhenoXmlAdapter adapter = new PhenoXmlAdapter(this.session);
              List<PhenotypeCharacterI> phenotypes = adapter.parsePhenotype(xmlPhen.getPhenotype(), new PhenotypeWrapperFactory());
              for (PhenotypeCharacterI phenotype : phenotypes) {
                newState.addPhenotype(((PhenoXMLPhenotypeWrapper)phenotype).getPhenotype());
              }
            } catch (XmlException e) {
              log().error("Failed to parse OBO phenotype", e);
            }
          }
          newCharacter.addState(newState);
        }
      }
      this.data.addCharacter(newCharacter);
    }
  }
  
  /**
   * The NeXML schema forces symbols to be an integer, but I 
   * believe this is incorrect.  This method should allow us 
   * to read any kind of symbol.
   */
  private String readSymbol(AbstractState state) {
    final XmlAnySimpleType symbol = state.getSymbol();
    final Attr attribute = (Attr)(symbol.getDomNode());
    return attribute.getValue();
  }
  
  private void parseTaxa(Taxa taxa) {
    for (org.nexml.x10.Taxon xmlTaxon : taxa.getOtuArray()) {
      final Taxon newTaxon = new Taxon(xmlTaxon.getId());
      newTaxon.setPublicationName(xmlTaxon.getLabel());
      final Dict oboIDDict = NeXMLUtil.findOrCreateDict(xmlTaxon, "OBO_ID", xmlTaxon.getDomNode().getOwnerDocument().createElement("string"));
      for (String id : oboIDDict.getStringArray()) {
        final IdentifiedObject term = this.session.getObject(id.trim());
        if (term instanceof OBOClass) {
          newTaxon.setValidName((OBOClass)term);
        } else {
          log().error("ID for " + newTaxon.getPublicationName() + " is not an OBOClass: " + term);
        }
        break; // there should only be one String element anyway
      }
      newTaxon.setComment(this.getComment(xmlTaxon));
      final Dict specimensDict = NeXMLUtil.findOrCreateDict(xmlTaxon, "OBO_specimens", xmlTaxon.getDomNode().getOwnerDocument().createElement("any"));
      for (XmlObject xmlObj : specimensDict.getAnyArray()) {
        final NodeList nodes = ((Element)(xmlObj.getDomNode())).getElementsByTagName("specimen");
        for (int i = 0; i < nodes.getLength(); i++) {
          final Specimen newSpecimen = newTaxon.newSpecimen();
          final Element specimenXML = (Element)(nodes.item(i));
          final IdentifiedObject term = this.session.getObject(specimenXML.getAttribute("collection"));
          if (term instanceof OBOClass) {
            newSpecimen.setCollectionCode((OBOClass)term);
          } else {
            log().error("ID for collection code is not an OBOClass: " + term);
          }
          newSpecimen.setCatalogID(specimenXML.getAttribute("accession"));
        }
      }
      this.data.addTaxon(newTaxon);
    }
  }
  
  private void parseMatrix(AbstractObsMatrix matrix) {
    final Map<String, Map<String, String>> matrixMap = new HashMap<String, Map<String, String>>();
    for (AbstractObsRow row : matrix.getRowArray()) {
      final String otuID = row.getOtu();
      if (otuID != null) {
        final Map<String, String> currentTaxonMap = new HashMap<String, String>();
        matrixMap.put(otuID, currentTaxonMap);
        for (AbstractObs cell : row.getCellArray()) {
          final String characterID = cell.getChar() != null ? cell.getChar().getStringValue() : null;
          final String stateID = cell.getState() != null ? cell.getState().getStringValue() : null;
          if (characterID != null && stateID != null) {
            currentTaxonMap.put(characterID, stateID);
          }
        }
      }
    }
    this.data.setMatrixData(matrixMap);
  }
  
  private void parseMetadata(Dict metadataDict) {
    final Element any = NeXMLUtil.getFirstChildWithTagName(((Element)(metadataDict.getDomNode())), "any");
    if (any != null) {
      final Element curators = NeXMLUtil.getFirstChildWithTagName(any, "curators");
      this.data.setCurators(curators != null ? NeXMLUtil.getTextContent(curators) : null);
      final Element publication = NeXMLUtil.getFirstChildWithTagName(any, "publication");
      this.data.setPublication(publication != null ? NeXMLUtil.getTextContent(publication) : null);
      final Element pubNotes = NeXMLUtil.getFirstChildWithTagName(any, "publicationNotes");
      this.data.setPublicationNotes(pubNotes != null ? NeXMLUtil.getTextContent(pubNotes) : null);
    }
  }
  
  private String getComment(Annotated node) {
    final Dict commentDict = NeXMLUtil.findOrCreateDict(node, NeXMLUtil.COMMENT_KEY, node.getDomNode().getOwnerDocument().createElement("string"));
    for (String comment : commentDict.getStringArray()) {
      return comment;
    }
    return null;
  }
  
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }
  
}
