package org.phenoscape.io;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlException;
import org.bioontologies.obd.schema.pheno.PhenotypeDocument;
import org.nexml.schema_2009.AbstractBlock;
import org.nexml.schema_2009.AbstractChar;
import org.nexml.schema_2009.AbstractObs;
import org.nexml.schema_2009.AbstractObsMatrix;
import org.nexml.schema_2009.AbstractObsRow;
import org.nexml.schema_2009.AbstractState;
import org.nexml.schema_2009.AbstractStates;
import org.nexml.schema_2009.Annotated;
import org.nexml.schema_2009.Nexml;
import org.nexml.schema_2009.NexmlDocument;
import org.nexml.schema_2009.StandardCells;
import org.nexml.schema_2009.StandardChar;
import org.nexml.schema_2009.StandardFormat;
import org.nexml.schema_2009.StandardStates;
import org.nexml.schema_2009.Taxa;
import org.obo.datamodel.IdentifiedObject;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOSession;
import org.obo.datamodel.impl.DanglingClassImpl;
import org.phenoscape.io.NeXMLUtil.LiteralContents;
import org.phenoscape.io.NeXMLUtil.OBOURISyntaxException;
import org.phenoscape.model.Character;
import org.phenoscape.model.DataSet;
import org.phenoscape.model.Phenotype;
import org.phenoscape.model.Specimen;
import org.phenoscape.model.State;
import org.phenoscape.model.Taxon;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

public class NeXMLReader {

    private final DataSet data = new DataSet();
    private final NexmlDocument xmlDoc;
    private final OBOSession session;
    private final Set<String> danglers = new HashSet<String>();
    private final Set<String> secondaryIDs = new HashSet<String>();
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
        this.parseMetadata(this.xmlDoc.getNexml());
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
            newCharacter.setFigure(this.getFigure(standardChar));
            final AbstractStates states = NeXMLUtil.findOrCreateStates(format, newCharacter.getStatesNexmlID());
            if (states instanceof StandardStates) {
                for (AbstractState abstractState : states.getStateArray()) {
                    final State newState = new State(abstractState.getId());
                    newState.setSymbol(this.readSymbol(abstractState));
                    newState.setLabel(abstractState.getLabel());
                    newState.setComment(this.getComment(abstractState));
                    newState.setFigure(this.getFigure(abstractState));
                    final Object phenotypeObj = NeXMLUtil.getFirstMetadataValue(abstractState, NeXMLUtil.PHENOTYPE_PREDICATE);
                    if (phenotypeObj instanceof LiteralContents) {
                        final LiteralContents literal = (LiteralContents)phenotypeObj;
                        // we need to get the last element, due to a now fixed bug which caused phenotypes to be appended in files, instead of replaced
                        // this will allow it to read in the latest work before destroying the unnecessary elements upon save
                        final Element phenoXML = NeXMLUtil.getLastChildWithTagNameNS(literal.getElement(), NeXMLUtil.PHENOXML_NAMESPACE, "phenotype");
                        if (phenoXML != null) {
                            try {
                                PhenotypeDocument xmlPhen = org.bioontologies.obd.schema.pheno.PhenotypeDocument.Factory.parse(phenoXML);
                                final PhenoXMLAdapter adapter = new PhenoXMLAdapter(this.session);
                                final List<Phenotype> phenotypes = adapter.parsePhenotype(xmlPhen.getPhenotype());
                                for (Phenotype phenotype : phenotypes) {
                                    newState.addPhenotype(phenotype);
                                }
                                this.danglers.addAll(adapter.getDanglersList());
                                this.secondaryIDs.addAll(adapter.getMigratedSecondaryIDsList());
                            } catch (XmlException e) {
                                log().error("Failed to parse OBO phenotype", e);
                            }
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

    @SuppressWarnings("unchecked")
    private void parseTaxa(Taxa taxa) {
        for (org.nexml.schema_2009.Taxon xmlTaxon : taxa.getOtuArray()) {
            final Taxon newTaxon = new Taxon(xmlTaxon.getId());
            newTaxon.setPublicationName((xmlTaxon.getLabel() == null || xmlTaxon.getLabel().equals("")) ? null : xmlTaxon.getLabel());
            final Object validNameObj = NeXMLUtil.getFirstMetadataValue(xmlTaxon, NeXMLUtil.VALID_NAME_PREDICATE);
            if (validNameObj != null) {
                try {
                    final String validNameID = NeXMLUtil.oboID(new URI(validNameObj.toString()));
                    newTaxon.setValidName(this.getTerm(validNameID));
                } catch (OBOURISyntaxException e) {
                    log().error("Value for taxon ID is not a valid OBO URI", e);
                } catch (URISyntaxException e) {
                    log().error("Value for taxon ID is not a valid URI", e);
                }
            }
            newTaxon.setComment(this.getComment(xmlTaxon));
            newTaxon.setFigure(this.getFigure(xmlTaxon));
            newTaxon.setMatrixTaxonName(this.getMatrixTaxon(xmlTaxon));
            final List<Object> specimens = NeXMLUtil.getMetadataValues(xmlTaxon, NeXMLUtil.SPECIMEN_PREDICATE);
            for (Object specimenData : specimens) {
                if (specimenData instanceof Map<?,?>) {
                    final Map<QName, List<Object>> map = (Map<QName, List<Object>>)specimenData;
                    final Specimen newSpecimen = newTaxon.newSpecimen();
                    if (map.containsKey(NeXMLUtil.COLLECTION_PREDICATE)) {
                        final List<Object> collectionIDList = map.get(NeXMLUtil.COLLECTION_PREDICATE);
                        try {
                            final String collectionIDURI = this.stringOrNull(NeXMLUtil.first(collectionIDList));
                            if (collectionIDURI != null) {
                                final String collectionID = NeXMLUtil.oboID(new URI(collectionIDURI));
                                newSpecimen.setCollectionCode(this.getTerm(collectionID));
                            }
                        } catch (OBOURISyntaxException e) {
                            log().error("Value for collection ID is not a valid OBO URI", e);
                        } catch (URISyntaxException e) {
                            log().error("Value for collection ID is not a valid URI", e);
                        }
                    }
                    if (map.containsKey(NeXMLUtil.ACCESSION_PREDICATE)) {
                        final List<Object> accessionList = map.get(NeXMLUtil.ACCESSION_PREDICATE);
                        newSpecimen.setCatalogID(stringOrNull(NeXMLUtil.first(accessionList)));
                    }
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

    private void parseMetadata(Nexml nexml) {
        final Object curatorsObj = NeXMLUtil.getFirstMetadataValue(nexml, NeXMLUtil.CURATORS_PREDICATE);
        this.data.setCurators(stringOrNull(curatorsObj));
        final Object publicationObj = NeXMLUtil.getFirstMetadataValue(nexml, NeXMLUtil.PUBLICATION_PREDICATE);
        this.data.setPublication(stringOrNull(publicationObj));
        final Object pubNotesObj = NeXMLUtil.getFirstMetadataValue(nexml, NeXMLUtil.PUBLICATION_NOTES_PREDICATE);
        this.data.setPublicationNotes(stringOrNull(pubNotesObj));
    }

    private String getComment(Annotated node) {
        final Object comment = NeXMLUtil.getFirstMetadataValue(node, NeXMLUtil.COMMENT_PREDICATE);
        return stringOrNull(comment);
    }

    private String getFigure(Annotated node) {
        final Object figure = NeXMLUtil.getFirstMetadataValue(node, NeXMLUtil.FIGURE_PREDICATE);
        return stringOrNull(figure);
    }

    private String getMatrixTaxon(Annotated node) {
        final Object matrixName = NeXMLUtil.getFirstMetadataValue(node, NeXMLUtil.MATRIX_NAME_PREDICATE);
        return stringOrNull(matrixName);
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

    private String stringOrNull(Object obj) {
        if (obj == null) {
            return null;
        } else {
            final String string = obj.toString();
            if (string.trim().length() < 1) {
                return null;
            } else {
                return string;
            }
        }
    }

    private Logger log() {
        return Logger.getLogger(this.getClass());
    }

}
