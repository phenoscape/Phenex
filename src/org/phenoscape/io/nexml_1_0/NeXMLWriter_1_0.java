package org.phenoscape.io.nexml_1_0;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlOptions;
import org.bioontologies.obd.schema.pheno.PhenotypeCharacterDocument.PhenotypeCharacter;
import org.bioontologies.obd.schema.pheno.PhenotypeManifestationDocument.PhenotypeManifestation;
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
import org.nexml.x10.StandardMatrixObsRow;
import org.nexml.x10.StandardObs;
import org.nexml.x10.StandardState;
import org.nexml.x10.StandardStates;
import org.nexml.x10.Taxa;
import org.phenoscape.io.PhenoXMLPhenotypeWrapper;
import org.phenoscape.model.Character;
import org.phenoscape.model.DataSet;
import org.phenoscape.model.Phenotype;
import org.phenoscape.model.Specimen;
import org.phenoscape.model.State;
import org.phenoscape.model.Taxon;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import phenote.dataadapter.phenoxml.PhenoXmlAdapter;

public class NeXMLWriter_1_0 {

    private final NexmlDocument xmlDoc;
    private final String charactersBlockID;
    private DataSet data;
    private String generator;
    private final XmlOptions options = new XmlOptions();

    public NeXMLWriter_1_0(String charactersBlockID) {
        this(charactersBlockID, NexmlDocument.Factory.newInstance());
    }

    public NeXMLWriter_1_0(String charactersBlockID, NexmlDocument startingDoc) {
        this.charactersBlockID = charactersBlockID;
        this.xmlDoc = startingDoc;
        this.options.setSavePrettyPrint();
        Map<String, String> suggestedPrefixes = new HashMap<String, String>();
        suggestedPrefixes.put("http://www.nexml.org/1.0", "nex");
        this.options.setSaveAggressiveNamespaces();
        this.options.setSaveSuggestedPrefixes(suggestedPrefixes);
        this.options.setSaveNamespacesFirst();
    }

    public void setDataSet(DataSet data) {
        this.data = data;
    }

    public void setGenerator(String appName) {
        this.generator = appName;
    }

    public void write(File aFile) throws IOException {
        this.constructXMLDoc().save(aFile, this.options);
    }

    public void write(OutputStream aStream) throws IOException {
        this.constructXMLDoc().save(aStream, this.options);
    }

    public void write(Writer aWriter) throws IOException {
        this.constructXMLDoc().save(aWriter, this.options);
    }

    private NexmlDocument constructXMLDoc() {
        final NexmlDocument newDoc = (NexmlDocument)(xmlDoc.copy());
        if (newDoc.getNexml() == null) { newDoc.addNewNexml(); }
        if (this.generator != null) { newDoc.getNexml().setGenerator(this.generator); }
        newDoc.getNexml().setVersion(BigDecimal.valueOf(1.0));
        Dict metadata = NeXMLUtil_1_0.findOrCreateMetadataDict(newDoc);
        this.writeToMetadata(metadata, this.data.getCurators(), this.data.getPublication(), this.data.getPublicationNotes());
        final AbstractBlock charBlock = NeXMLUtil_1_0.findOrCreateCharactersBlock(newDoc, this.charactersBlockID);
        this.writeCharacters(charBlock);
        final String taxaID;
        if ((charBlock.getOtus() == null) || (charBlock.getOtus().equals(""))) {
            taxaID = UUID.randomUUID().toString();
            charBlock.setOtus(taxaID);
        } else {
            taxaID = charBlock.getOtus();
        }
        final Taxa taxaBlock = NeXMLUtil_1_0.findOrCreateTaxa(newDoc, taxaID);
        this.writeTaxa(taxaBlock);
        // move taxa ahead of characters
        final XmlCursor firstCharCursor = newDoc.getNexml().getCharactersArray()[0].newCursor();
        final XmlCursor taxaCursor = taxaBlock.newCursor();
        taxaCursor.moveXml(firstCharCursor);
        return newDoc;
    }

    private void writeCharacters(AbstractBlock charBlock) {
        final List<AbstractChar> existingChars = Arrays.asList(charBlock.getFormat().getCharArray());
        final List<AbstractStates> existingStatesList = Arrays.asList(charBlock.getFormat().getStatesArray());
        final List<AbstractChar> newCharacters = new ArrayList<AbstractChar>();
        final List<AbstractStates> newStatesBlocks = new ArrayList<AbstractStates>();
        final Set<String> usedStatesIDs = new HashSet<String>();
        for (Character character : this.data.getCharacters()) {
            final AbstractChar xmlChar = this.findOrCreateCharWithID(existingChars, character.getNexmlID());
            newCharacters.add(xmlChar);
            xmlChar.setLabel(character.getLabel());
            this.writeComment(xmlChar, character.getComment());
            this.writeFigure(xmlChar, character.getFigure());
            final AbstractStates statesBlock = this.findOrCreateStatesBlockWithID(existingStatesList, character.getStatesNexmlID());
            final AbstractStates usableStatesBlock;
            if (usedStatesIDs.contains(statesBlock.getId())) {
                usableStatesBlock = (AbstractStates)(statesBlock.copy());
                usableStatesBlock.setId(UUID.randomUUID().toString());
            } else {
                usableStatesBlock = statesBlock;
            }
            newStatesBlocks.add(usableStatesBlock);
            usedStatesIDs.add(usableStatesBlock.getId());
            xmlChar.setStates(usableStatesBlock.getId());
            final List<AbstractState> existingStates = Arrays.asList(usableStatesBlock.getStateArray());
            final List<AbstractState> newStates = new ArrayList<AbstractState>();
            for (State state : character.getStates()) {
                final AbstractState xmlState = this.findOrCreateStateWithID(existingStates, state.getNexmlID());
                newStates.add(xmlState);
                xmlState.setLabel(state.getLabel());
                this.writeComment(xmlState, state.getComment());
                this.writeFigure(xmlState, state.getFigure());
                this.writeSymbol(xmlState, state.getSymbol() != null ? state.getSymbol() : "0");
                this.writePhenotypes(xmlState, state);
            }
            usableStatesBlock.setStateArray(newStates.toArray(new AbstractState[] {}));
        }
        charBlock.getFormat().setCharArray(newCharacters.toArray(new AbstractChar[] {}));
        charBlock.getFormat().setStatesArray(newStatesBlocks.toArray(new AbstractStates[] {}));
        if (charBlock instanceof StandardCells) {
            final StandardCells cells = (StandardCells)charBlock;
            final AbstractObsMatrix matrix = cells.getMatrix() != null ? cells.getMatrix() : cells.addNewMatrix();
            this.writeMatrix(matrix);
        }
    }

    private void writeMatrix(AbstractObsMatrix matrix) {
        final List<AbstractObsRow> existingRows = Arrays.asList(matrix.getRowArray());
        final List<AbstractObsRow> newRows = new ArrayList<AbstractObsRow>();
        for (Taxon taxon : this.data.getTaxa()) {
            final AbstractObsRow xmlRow = this.findOrCreateRowForTaxon(existingRows, taxon.getNexmlID());
            newRows.add(xmlRow);
            final List<AbstractObs> existingCells = Arrays.asList(xmlRow.getCellArray());
            final List<AbstractObs> newCells = new ArrayList<AbstractObs>();
            for (Character character : this.data.getCharacters()) {
                final State state = this.data.getStateForTaxon(taxon, character);
                if (state != null) {
                    final AbstractObs xmlCell = this.findOrCreateCellForCharacter(existingCells, character.getNexmlID());
                    final XmlAnySimpleType xmlState = XmlAnySimpleType.Factory.newInstance();
                    xmlState.setStringValue(state.getNexmlID());
                    xmlCell.setState(xmlState);
                    newCells.add(xmlCell);
                }
            }
            xmlRow.setCellArray(newCells.toArray(new AbstractObs[] {}));
        }
        matrix.setRowArray(newRows.toArray(new AbstractObsRow[] {}));
    }

    private void writeTaxa(Taxa taxaBlock) {
        final List<org.nexml.x10.Taxon> existingOTUs = Arrays.asList(taxaBlock.getOtuArray());
        final List<org.nexml.x10.Taxon> newOTUs = new ArrayList<org.nexml.x10.Taxon>();
        for (Taxon taxon : this.data.getTaxa()) {
            final org.nexml.x10.Taxon otu = this.findOrCreateOTUWithID(existingOTUs, taxon.getNexmlID());
            newOTUs.add(otu);
            otu.setLabel(taxon.getPublicationName());
            this.writeOBOID(otu, taxon);
            this.writeSpecimens(otu, taxon);
            this.writeComment(otu, taxon.getComment());
            this.writeFigure(otu, taxon.getFigure());
            this.writeMatrixTaxon(otu, taxon.getMatrixTaxonName());
        }
        taxaBlock.setOtuArray(newOTUs.toArray(new org.nexml.x10.Taxon[] {}));
    }

    private AbstractObsRow findOrCreateRowForTaxon(List<AbstractObsRow> list, String id) {
        for (AbstractObsRow row : list) {
            if (id.equals(row.getOtu())) { return row; }
        }
        final AbstractObsRow newRow = StandardMatrixObsRow.Factory.newInstance();
        newRow.setId(UUID.randomUUID().toString());
        newRow.setOtu(id);
        return newRow;
    }

    private AbstractObs findOrCreateCellForCharacter(List<AbstractObs> list, String id) {
        for (AbstractObs cell : list) {
            if (id.equals(cell.getChar().getStringValue())) { return cell; }
        }
        final AbstractObs newCell = StandardObs.Factory.newInstance();
        final XmlAnySimpleType charXML = XmlAnySimpleType.Factory.newInstance();
        charXML.setStringValue(id);
        newCell.setChar(charXML);
        return newCell;
    }

    private org.nexml.x10.Taxon findOrCreateOTUWithID(List<org.nexml.x10.Taxon> list, String id) {
        for (org.nexml.x10.Taxon otu : list) {
            if (otu.getId().equals(id)) { return otu; }
        }
        final org.nexml.x10.Taxon newOTU = org.nexml.x10.Taxon.Factory.newInstance();
        newOTU.setId(id);
        return newOTU;
    }

    private AbstractChar findOrCreateCharWithID(List<AbstractChar> list, String id) {
        for (AbstractChar character : list) {
            if (character.getId().equals(id)) { return character; }
        }
        final AbstractChar newCharacter = StandardChar.Factory.newInstance();
        newCharacter.setId(id);
        return newCharacter;
    }

    private AbstractStates findOrCreateStatesBlockWithID(List<AbstractStates> list, String id) {
        for (AbstractStates statesBlock : list) {
            if (statesBlock.getId().equals(id)) { return statesBlock; }
        }
        final AbstractStates newStatesBlock = StandardStates.Factory.newInstance();
        newStatesBlock.setId(id);
        return newStatesBlock;
    }

    private AbstractState findOrCreateStateWithID(List<AbstractState> list, String id) {
        for (AbstractState state : list) {
            if (state.getId().equals(id)) { return state; }
        }
        final AbstractState newState = StandardState.Factory.newInstance();
        newState.setId(id);
        return newState;
    }

    private void writeToMetadata(Dict metadata, String curatorsText, String publicationText, String pubNotesText) {
        final Element any = NeXMLUtil_1_0.getFirstChildWithTagName(((Element)(metadata.getDomNode())), "any");
        final Element curators = NeXMLUtil_1_0.getFirstChildWithTagName(any, "curators");
        NeXMLUtil_1_0.setTextContent(curators, curatorsText);
        final Element publication = NeXMLUtil_1_0.getFirstChildWithTagName(any, "publication");
        NeXMLUtil_1_0.setTextContent(publication, publicationText);
        final Element pubNotes = NeXMLUtil_1_0.getFirstChildWithTagName(any, "publicationNotes");
        NeXMLUtil_1_0.setTextContent(pubNotes, pubNotesText);
    }

    private void writeOBOID(org.nexml.x10.Taxon otu, Taxon taxon) {
        final Dict oboIDDict = NeXMLUtil_1_0.findOrCreateDict(otu, "OBO_ID", otu.getDomNode().getOwnerDocument().createElement("string"));
        if (taxon.getValidName() == null) {
            NeXMLUtil_1_0.removeDict(otu, oboIDDict);
        } else {
            final Element stringNode = NeXMLUtil_1_0.getFirstChildWithTagName((Element)(oboIDDict.getDomNode()), "string");
            NeXMLUtil_1_0.setTextContent(stringNode, taxon.getValidName().getID());
        }
    }

    private void writeSpecimens(org.nexml.x10.Taxon otu, Taxon taxon) {
        final Dict specimensDict = NeXMLUtil_1_0.findOrCreateDict(otu, "OBO_specimens", otu.getDomNode().getOwnerDocument().createElement("any"));
        final Element any = NeXMLUtil_1_0.getFirstChildWithTagName((Element)(specimensDict.getDomNode()), "any");
        NeXMLUtil_1_0.clearChildren(any);
        if (taxon.getSpecimens().isEmpty()) {
            NeXMLUtil_1_0.removeDict(otu, specimensDict);
        } else {
            for (Specimen specimen : taxon.getSpecimens()) {
                final Element specimenXML = any.getOwnerDocument().createElement("specimen");
                specimenXML.setAttribute("collection", specimen.getCollectionCode() != null ? specimen.getCollectionCode().getID() : null);
                specimenXML.setAttribute("accession", specimen.getCatalogID());
                any.appendChild(specimenXML);
            }
        }
    }

    private void writeComment(Annotated node, String comment) {
        final Dict commentDict = NeXMLUtil_1_0.findOrCreateDict(node, NeXMLUtil_1_0.COMMENT_KEY, node.getDomNode().getOwnerDocument().createElement("string"));
        if ((comment == null) || (comment.equals(""))) {
            NeXMLUtil_1_0.removeDict(node, commentDict);
        } else {
            final Element stringNode = NeXMLUtil_1_0.getFirstChildWithTagName((Element)(commentDict.getDomNode()), "string");
            NeXMLUtil_1_0.setTextContent(stringNode, comment);
        }
    }

    private void writeFigure(Annotated node, String figure) {
        final Dict figureDict = NeXMLUtil_1_0.findOrCreateDict(node, NeXMLUtil_1_0.FIGURE_KEY, node.getDomNode().getOwnerDocument().createElement("string"));
        if ((figure == null) || (figure.equals(""))) {
            NeXMLUtil_1_0.removeDict(node, figureDict);
        } else {
            final Element stringNode = NeXMLUtil_1_0.getFirstChildWithTagName((Element)(figureDict.getDomNode()), "string");
            NeXMLUtil_1_0.setTextContent(stringNode, figure);
        }
    }

    private void writeMatrixTaxon(Annotated node, String matrixTaxon) {
        final Dict matrixTaxonDict = NeXMLUtil_1_0.findOrCreateDict(node, NeXMLUtil_1_0.MATRIX_TAXON_KEY, node.getDomNode().getOwnerDocument().createElement("string"));
        if ((matrixTaxon == null) || (matrixTaxon.equals(""))) {
            NeXMLUtil_1_0.removeDict(node, matrixTaxonDict);
        } else {
            final Element stringNode = NeXMLUtil_1_0.getFirstChildWithTagName((Element)(matrixTaxonDict.getDomNode()), "string");
            NeXMLUtil_1_0.setTextContent(stringNode, matrixTaxon);
        }
    }

    private void writePhenotypes(AbstractState xmlState, State state) {
        final Dict phenotypeDict = NeXMLUtil_1_0.findOrCreateDict(xmlState, "OBO_phenotype", xmlState.getDomNode().getOwnerDocument().createElement("any"));
        if (state.getPhenotypes().isEmpty()) {
            NeXMLUtil_1_0.removeDict(xmlState, phenotypeDict);
            return;
        }
        final Element any = NeXMLUtil_1_0.getFirstChildWithTagName((Element)(phenotypeDict.getDomNode()), "any");
        NeXMLUtil_1_0.clearChildren(any);
        final List<PhenotypeCharacter> pcs = new ArrayList<PhenotypeCharacter>();
        for (Phenotype phenotype : state.getPhenotypes()) {
            final PhenotypeCharacter pc = PhenoXmlAdapter.createPhenotypeCharacter(new PhenoXMLPhenotypeWrapper(phenotype));
            if (pc != null) { pcs.add(pc); }
        }
        final org.bioontologies.obd.schema.pheno.PhenotypeDocument.Phenotype phenoXML = PhenoXmlAdapter.createPhenotype(pcs);
        // for some reason the PhenoXML Phenotype appears only as a DocumentFragment instead of Element until it's stuck into a PhenotypeManifestation
        final PhenotypeManifestation scratchPM = PhenotypeManifestation.Factory.newInstance();
        scratchPM.setPhenotype(phenoXML);
        final Node importedPhenoXML = any.getOwnerDocument().importNode(scratchPM.getPhenotype().getDomNode(), true);
        any.appendChild(importedPhenoXML);
    }

    /**
     * The NeXML schema forces symbols to be an integer, but I 
     * believe this is incorrect.  This method should allow us 
     * to write any kind of symbol.
     */
    private void writeSymbol(AbstractState state, String symbolValue) {
        final XmlAnySimpleType symbol = state.getSymbol() != null ? state.getSymbol() : state.addNewSymbol();
        final Attr attribute = (Attr)(symbol.getDomNode());
        attribute.setValue(symbolValue);
    }

    @SuppressWarnings("unused")
    private Logger log() {
        return Logger.getLogger(this.getClass());
    }

}
