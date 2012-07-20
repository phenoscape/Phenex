package org.phenoscape.io.nexml_1_0;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.obo.datamodel.impl.DanglingClassImpl;
import org.phenoscape.io.PhenoXMLAdapter;
import org.phenoscape.model.Character;
import org.phenoscape.model.DataSet;
import org.phenoscape.model.Phenotype;
import org.phenoscape.model.Specimen;
import org.phenoscape.model.State;
import org.phenoscape.model.Taxon;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class NeXMLReader_1_0 {

    private final DataSet data = new DataSet();
    private final NexmlDocument xmlDoc;
    private final OBOSession session;
    private final Set<String> danglers = new HashSet<String>();
    private final Set<String> secondaryIDs = new HashSet<String>();
    private String charactersBlockID = UUID.randomUUID().toString();
    private final Map<String, State> allStates = new HashMap<String, State>();

    public NeXMLReader_1_0(File aFile, OBOSession session) throws XmlException, IOException {
        this.session = session;
        this.xmlDoc = NexmlDocument.Factory.parse(aFile);
        this.parseNeXML();
    }

    public NeXMLReader_1_0(Reader aReader, OBOSession session) throws XmlException, IOException {
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

    /**
     * Returns true if the reader had to create dangling terms for referenced IDs not found in the OBOSession.
     */
    public boolean didCreateDanglers() {
        return !this.danglers.isEmpty();
    }

    /**
     * Returns the list of IDs referenced in the file that were not found in the OBOSession.
     */
    public Collection<String> getDanglersList() {
        return this.danglers;
    }

    /**
     * Returns true if the reader had to find any referenced terms via a secondary ID.
     */
    public boolean didMigrateSecondaryIDs() {
        return !this.secondaryIDs.isEmpty();
    }

    /**
     * Returns the list of IDs referenced in the file that were found to be secondary IDs.
     */
    public Collection<String> getMigratedSecondaryIDsList() {
        return this.secondaryIDs;
    }

    private void parseNeXML() {
        final Dict metadata = NeXMLUtil_1_0.findOrCreateMetadataDict(this.xmlDoc);
        this.parseMetadata(metadata);
        for (AbstractBlock block : this.xmlDoc.getNexml().getCharactersArray()) {
            if (block instanceof StandardCells) {
                this.charactersBlockID = block.getId();
                final StandardCells cells = (StandardCells)block;
                this.parseStandardCells(cells);
                final Taxa taxa = NeXMLUtil_1_0.findOrCreateTaxa(this.xmlDoc, cells.getOtus());
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
            newCharacter.setFigure(this.getFigure(standardChar));
            final AbstractStates states = NeXMLUtil_1_0.findOrCreateStates(format, newCharacter.getStatesNexmlID());
            if (states instanceof StandardStates) {
                for (AbstractState abstractState : states.getStateArray()) {
                    final State newState = new State(abstractState.getId());
                    newState.setSymbol(this.readSymbol(abstractState));
                    newState.setLabel(abstractState.getLabel());
                    newState.setComment(this.getComment(abstractState));
                    newState.setFigure(this.getFigure(abstractState));
                    this.allStates.put(newState.getNexmlID(), newState);
                    final Dict phenotypeDict = NeXMLUtil_1_0.findOrCreateDict(abstractState, "OBO_phenotype", abstractState.getDomNode().getOwnerDocument().createElement("any"));
                    final Element any = NeXMLUtil_1_0.getFirstChildWithTagName((Element)(phenotypeDict.getDomNode()), "any");
                    final Element phenoXML = NeXMLUtil_1_0.getFirstChildWithTagNameNS(any, "http://www.bioontologies.org/obd/schema/pheno", "phenotype");
                    if (phenoXML != null) {
                        try {
                            PhenotypeDocument xmlPhen = org.bioontologies.obd.schema.pheno.PhenotypeDocument.Factory.parse(phenoXML);
                            PhenoXMLAdapter adapter = new PhenoXMLAdapter(this.session);
                            List<Phenotype> phenotypes = adapter.parsePhenotype(xmlPhen.getPhenotype());
                            for (Phenotype phenotype : phenotypes) {
                                newState.addPhenotype(phenotype);
                            }
                            this.danglers.addAll(adapter.getDanglersList());
                            this.secondaryIDs.addAll(adapter.getMigratedSecondaryIDsList());
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
            newTaxon.setPublicationName((xmlTaxon.getLabel() == null || xmlTaxon.getLabel().equals("")) ? null : xmlTaxon.getLabel());
            final Dict oboIDDict = NeXMLUtil_1_0.findOrCreateDict(xmlTaxon, "OBO_ID", xmlTaxon.getDomNode().getOwnerDocument().createElement("string"));
            for (String id : oboIDDict.getStringArray()) {
                final String taxonID = id.trim();
                if ((taxonID != null) && (!taxonID.equals(""))) {
                    newTaxon.setValidName(this.getTerm(taxonID));
                }
                break; // there should only be one String element anyway
            }
            newTaxon.setComment(this.getComment(xmlTaxon));
            newTaxon.setFigure(this.getFigure(xmlTaxon));
            newTaxon.setMatrixTaxonName(this.getMatrixTaxon(xmlTaxon));
            final Dict specimensDict = NeXMLUtil_1_0.findOrCreateDict(xmlTaxon, "OBO_specimens", xmlTaxon.getDomNode().getOwnerDocument().createElement("any"));
            for (XmlObject xmlObj : specimensDict.getAnyArray()) {
                final NodeList nodes = ((Element)(xmlObj.getDomNode())).getElementsByTagName("specimen");
                for (int i = 0; i < nodes.getLength(); i++) {
                    final Specimen newSpecimen = newTaxon.newSpecimen();
                    final Element specimenXML = (Element)(nodes.item(i));
                    final String collectionID = specimenXML.getAttribute("collection");
                    if ((collectionID != null) && (!collectionID.equals(""))) {
                        newSpecimen.setCollectionCode(this.getTerm(collectionID));
                    }
                    newSpecimen.setCatalogID(specimenXML.getAttribute("accession"));
                }
            }
            this.data.addTaxon(newTaxon);
        }
    }

    private void parseMatrix(AbstractObsMatrix matrix) {
        final Map<String, Map<String, State>> matrixMap = new HashMap<String, Map<String, State>>();
        for (AbstractObsRow row : matrix.getRowArray()) {
            final String otuID = row.getOtu();
            if (otuID != null) {
                final Map<String, State> currentTaxonMap = new HashMap<String, State>();
                matrixMap.put(otuID, currentTaxonMap);
                for (AbstractObs cell : row.getCellArray()) {
                    final String characterID = cell.getChar() != null ? cell.getChar().getStringValue() : null;
                    final String stateID = cell.getState() != null ? cell.getState().getStringValue() : null;
                    final State state = this.allStates.get(stateID);
                    if (characterID != null && state != null) {
                        currentTaxonMap.put(characterID, state);
                    }
                }
            }
        }
        this.data.setMatrixData(matrixMap);
    }

    private void parseMetadata(Dict metadataDict) {
        final Element any = NeXMLUtil_1_0.getFirstChildWithTagName(((Element)(metadataDict.getDomNode())), "any");
        if (any != null) {
            final Element curators = NeXMLUtil_1_0.getFirstChildWithTagName(any, "curators");
            this.data.setCurators(curators != null ? NeXMLUtil_1_0.getTextContent(curators) : null);
            final Element publication = NeXMLUtil_1_0.getFirstChildWithTagName(any, "publication");
            this.data.setPublication(publication != null ? NeXMLUtil_1_0.getTextContent(publication) : null);
            final Element pubNotes = NeXMLUtil_1_0.getFirstChildWithTagName(any, "publicationNotes");
            this.data.setPublicationNotes(pubNotes != null ? NeXMLUtil_1_0.getTextContent(pubNotes) : null);
        }
    }

    private String getComment(Annotated node) {
        final Dict commentDict = NeXMLUtil_1_0.findOrCreateDict(node, NeXMLUtil_1_0.COMMENT_KEY, node.getDomNode().getOwnerDocument().createElement("string"));
        for (String comment : commentDict.getStringArray()) {
            return comment;
        }
        return null;
    }

    private String getFigure(Annotated node) {
        final Dict figureDict = NeXMLUtil_1_0.findOrCreateDict(node, NeXMLUtil_1_0.FIGURE_KEY, node.getDomNode().getOwnerDocument().createElement("string"));
        for (String figure : figureDict.getStringArray()) {
            return figure;
        }
        return null;
    }

    private String getMatrixTaxon(Annotated node) {
        final Dict matrixTaxonDict = NeXMLUtil_1_0.findOrCreateDict(node, NeXMLUtil_1_0.MATRIX_TAXON_KEY, node.getDomNode().getOwnerDocument().createElement("string"));
        for (String matrixTaxon : matrixTaxonDict.getStringArray()) {
            return matrixTaxon;
        }
        return null;
    }

    private OBOClass getTerm(String id) {
        final IdentifiedObject term = this.session.getObject(id);
        if (term instanceof OBOClass) {
            return (OBOClass)term;
        } else {
            final OBOClass altTerm = this.findTermByAltID(id);
            if (altTerm != null) {
                return altTerm;
            } else {
                log().warn("Term not found; creating dangler for " + id);
                this.danglers.add(id);
                final OBOClass dangler = new DanglingClassImpl(id.trim());
                return dangler;
            }
        }
    }

    private OBOClass findTermByAltID(String id) {
        log().debug("Called alt_id search");
        final Collection<IdentifiedObject> terms = this.session.getObjects();
        for (IdentifiedObject object : terms) {
            if (object instanceof OBOClass) {
                final OBOClass term = (OBOClass)object;
                if (term.getSecondaryIDs().contains(id)) {
                    this.secondaryIDs.add(id);
                    return term;
                }
            }
        }
        return null;
    }

    private Logger log() {
        return Logger.getLogger(this.getClass());
    }

}
