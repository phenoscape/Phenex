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
import org.nexml.schema_2009.AbstractMapping;
import org.nexml.schema_2009.AbstractObs;
import org.nexml.schema_2009.AbstractObsMatrix;
import org.nexml.schema_2009.AbstractObsRow;
import org.nexml.schema_2009.AbstractState;
import org.nexml.schema_2009.AbstractStates;
import org.nexml.schema_2009.AbstractUncertainStateSet;
import org.nexml.schema_2009.Annotated;
import org.nexml.schema_2009.Nexml;
import org.nexml.schema_2009.NexmlDocument;
import org.nexml.schema_2009.StandardCells;
import org.nexml.schema_2009.StandardChar;
import org.nexml.schema_2009.StandardFormat;
import org.nexml.schema_2009.StandardStates;
import org.nexml.schema_2009.Taxa;
import org.obo.datamodel.DanglingObject;
import org.obo.datamodel.IdentifiedObject;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOSession;
import org.obo.datamodel.ObsoletableObject;
import org.obo.datamodel.impl.DanglingClassImpl;
import org.phenoscape.io.NeXMLUtil.LiteralContents;
import org.phenoscape.io.NeXMLUtil.OBOURISyntaxException;
import org.phenoscape.model.Association;
import org.phenoscape.model.AssociationSupport;
import org.phenoscape.model.Character;
import org.phenoscape.model.DataSet;
import org.phenoscape.model.MultipleState;
import org.phenoscape.model.MultipleState.MODE;
import org.phenoscape.model.Phenotype;
import org.phenoscape.model.Specimen;
import org.phenoscape.model.State;
import org.phenoscape.model.Taxon;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class NeXMLReader {

	private final DataSet data = new DataSet();
	private final NexmlDocument xmlDoc;
	private final OBOSession session;
	private final Set<String> danglers = new HashSet<String>();
	private final Set<String> secondaryIDs = new HashSet<String>();
	private final Set<String> replacedIDs = new HashSet<String>();
	private String charactersBlockID = UUID.randomUUID().toString();
	private final Map<String, State> allStates = new HashMap<String, State>();

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

	public boolean didReplaceObsoleteTerms() {
		return !this.replacedIDs.isEmpty();
	}

	public Collection<String> getReplacedIDsList() {
		return this.replacedIDs;
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
			newCharacter.setDiscussion(this.getDiscussion(standardChar));
			newCharacter.setDenotes(this.getDenotes(standardChar));
			final AbstractStates states = NeXMLUtil.findOrCreateStates(format, newCharacter.getStatesNexmlID());
			if (states instanceof StandardStates) {
				for (AbstractState abstractState : states.getStateArray()) {
					final State newState = new State(abstractState.getId());
					newState.setSymbol(this.readSymbol(abstractState));
					newState.setLabel(abstractState.getLabel());
					newState.setComment(this.getComment(abstractState));
					newState.setFigure(this.getFigure(abstractState));
					this.allStates.put(newState.getNexmlID(), newState);
					final Object phenotypeObj = NeXMLUtil.getFirstMetadataValue(abstractState, NeXMLUtil.PHENOTYPE_PREDICATE);
					if (phenotypeObj instanceof LiteralContents) {
						final LiteralContents literal = (LiteralContents)phenotypeObj;
						// we need to get the last PhenoXML element that is different from the first, due to a now fixed bug which caused 
						// phenotypes to be appended in files, instead of replaced
						// this will allow it to read in the latest work before destroying the unnecessary elements upon save
						final NodeList phenotypeElements = literal.getElement().getElementsByTagNameNS(NeXMLUtil.PHENOXML_NAMESPACE, "phenotype");
						boolean first = true;
						List<Phenotype> firstPhenotypeList = null;
						List<Phenotype> newestPhenotypeList = null;
						for (int i = 0; i < phenotypeElements.getLength(); i++) {
							final Element phenoXML = (Element)(phenotypeElements.item(i));
							try {
								final PhenotypeDocument xmlPhen = org.bioontologies.obd.schema.pheno.PhenotypeDocument.Factory.parse(phenoXML);
								final PhenoXMLAdapter adapter = new PhenoXMLAdapter(this.session);
								final List<Phenotype> phenotypes = adapter.parsePhenotype(xmlPhen.getPhenotype());
								if (first) {
									first = false;
									firstPhenotypeList = phenotypes;
									newestPhenotypeList = phenotypes;
								} else {
									if (!phenotypes.equals(firstPhenotypeList)) {
										newestPhenotypeList = phenotypes;
									}
								}
								this.danglers.addAll(adapter.getDanglersList());
								this.secondaryIDs.addAll(adapter.getMigratedSecondaryIDsList());
								this.replacedIDs.addAll(adapter.getReplacedIDsList());
							} catch (XmlException e) {
								log().error("Failed to parse OBO phenotype", e);
							}
						}
						if (newestPhenotypeList != null) {
							for (Phenotype phenotype : newestPhenotypeList) {
								newState.addPhenotype(phenotype);
							}
						}   
					}
					newCharacter.addState(newState);
				}
				for (AbstractUncertainStateSet set : states.getUncertainStateSetArray()) {
					this.createMultiState(set, MODE.UNCERTAIN);
				}
				for (AbstractUncertainStateSet set : states.getPolymorphicStateSetArray()) {
					this.createMultiState(set, MODE.POLYMORPHIC);
				}
			}
			this.data.addCharacter(newCharacter);
		}
	}

	private MultipleState createMultiState(AbstractUncertainStateSet set, MODE mode) {
		log().debug("Creating multistate: " + set);
		final Set<State> memberStates = new HashSet<State>();
		for (AbstractMapping mapping : set.getMemberArray()) {
			memberStates.add(this.allStates.get(mapping.getState()));
		}
		final MultipleState state = new MultipleState(set.getId(), memberStates, mode);
		this.allStates.put(state.getNexmlID(), state);
		return state;
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
						final List<Object> suppportMetas = NeXMLUtil.getMetadataValues(cell, NeXMLUtil.ENTAILED_BY_PREDICATE);
						for (Object supportMeta : suppportMetas) {
							if (supportMeta instanceof Map<?,?>) {
								@SuppressWarnings("unchecked")
								final Map<QName, List<Object>> map = (Map<QName, List<Object>>)supportMeta;
								if (map.containsKey(NeXMLUtil.DC_IDENTIFIER) && map.containsKey(NeXMLUtil.DC_DESCRIPTION_PREDICATE) && map.containsKey(NeXMLUtil.DC_SOURCE_PREDICATE)) {
									final String identifier = stringOrNull(NeXMLUtil.first(map.get(NeXMLUtil.DC_IDENTIFIER)));
									final String description = stringOrNull(NeXMLUtil.first(map.get(NeXMLUtil.DC_DESCRIPTION_PREDICATE)));
									final String source = stringOrNull(NeXMLUtil.first(map.get(NeXMLUtil.DC_SOURCE_PREDICATE)));
									final boolean direct = Boolean.parseBoolean(stringOrNull(NeXMLUtil.first(map.get(NeXMLUtil.IS_DIRECT_PREDICATE))));
									final AssociationSupport associationSupport = new AssociationSupport(description, source, direct);
									final Association association = new Association(otuID, characterID, identifier);
									final Set<AssociationSupport> supports;
									if (this.data.getAssociationSupport().containsKey(association)) {
										supports = this.data.getAssociationSupport().get(association);
									} else {
										supports = new HashSet<AssociationSupport>();
										this.data.getAssociationSupport().put(association, supports);
									}
									supports.add(associationSupport);
								}
							}
						}
					}
				}
			}
		}
		this.data.setMatrixData(matrixMap);
	}

	private void parseMetadata(Nexml nexml) {
		final Object curatorsObj = NeXMLUtil.getFirstMetadataValue(nexml, NeXMLUtil.CURATORS_PREDICATE);
		this.data.setCurators(stringOrNull(curatorsObj));
		final Object pubSourceObj = NeXMLUtil.getFirstMetadataValue(nexml, NeXMLUtil.PUBLICATION_SOURCE_PREDICATE);
		if (pubSourceObj instanceof Map<?,?>) {
			@SuppressWarnings("unchecked")
			final Map<QName, List<Object>> map = (Map<QName, List<Object>>)pubSourceObj;
			if (map.containsKey(NeXMLUtil.PUBLICATION_LABEL_PREDICATE)) {
				final List<Object> labelList = map.get(NeXMLUtil.PUBLICATION_LABEL_PREDICATE);
				this.data.setPublicationLabel(stringOrNull(NeXMLUtil.first(labelList)));
			}
			if (map.containsKey(NeXMLUtil.PUBLICATION_CITATION_PREDICATE)) {
				final List<Object> citationList = map.get(NeXMLUtil.PUBLICATION_CITATION_PREDICATE);
				this.data.setPublicationCitation(stringOrNull(NeXMLUtil.first(citationList)));
			}
			if (map.containsKey(NeXMLUtil.PUBLICATION_URI_PREDICATE)) {
				final List<Object> uriList = map.get(NeXMLUtil.PUBLICATION_URI_PREDICATE);
				this.data.setPublicationURI(stringOrNull(NeXMLUtil.first(uriList)));
			}
		}
		final Object pubNotesObj = NeXMLUtil.getFirstMetadataValue(nexml, NeXMLUtil.DC_DESCRIPTION_PREDICATE);
		this.data.setPublicationNotes(stringOrNull(pubNotesObj));
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
					if (map.containsKey(NeXMLUtil.COMMENT_PREDICATE)) {
						final List<Object> commentList = map.get(NeXMLUtil.COMMENT_PREDICATE);
						newSpecimen.setComment(stringOrNull(NeXMLUtil.first(commentList)));
					}
				}
			}
			this.data.addTaxon(newTaxon);
		}
	}

	private String getComment(Annotated node) {
		final Object comment = NeXMLUtil.getFirstMetadataValue(node, NeXMLUtil.COMMENT_PREDICATE);
		return stringOrNull(comment);
	}

	private String getDiscussion(Annotated node) {
		final Object comment = NeXMLUtil.getFirstMetadataValue(node, NeXMLUtil.DISCUSSION_PREDICATE);
		return stringOrNull(comment);
	}
	
	private URI getDenotes(Annotated node) {
		final String term = stringOrNull(NeXMLUtil.getFirstMetadataValue(node, NeXMLUtil.DENOTES_PREDICATE));
		if (term != null) {
			return URI.create(term);
		} else {
			return null;
		}
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
		log().debug("Term id: " + id);
		if (id.equals("http://purl.bioontology.org/ontology/provisional/d0267b99-ff52-4a4c-bd31-ff6dbf4dafcc")) {
			log().debug("Provisional term");
		}
		final IdentifiedObject term = this.session.getObject(id);
		if (term instanceof OBOClass) {
			final OBOClass oboClass = (OBOClass)term;
			if (oboClass.isObsolete()) {
				if (!oboClass.getReplacedBy().isEmpty()) {
					final ObsoletableObject replacement = oboClass.getReplacedBy().iterator().next();
					if ((replacement instanceof OBOClass) && (!(replacement instanceof DanglingObject))) {
						this.replacedIDs.add(id);
						return (OBOClass)replacement;
					} else {
						return oboClass;
					}
				} else {
					return oboClass;
				}
			} else {
				return oboClass;	
			}
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
