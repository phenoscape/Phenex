package org.phenoscape.io;

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
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlOptions;
import org.bioontologies.obd.schema.pheno.PhenotypeCharacterDocument.PhenotypeCharacter;
import org.bioontologies.obd.schema.pheno.PhenotypeManifestationDocument.PhenotypeManifestation;
import org.nexml.schema_2009.AbstractBlock;
import org.nexml.schema_2009.AbstractChar;
import org.nexml.schema_2009.AbstractMapping;
import org.nexml.schema_2009.AbstractObs;
import org.nexml.schema_2009.AbstractObsMatrix;
import org.nexml.schema_2009.AbstractObsRow;
import org.nexml.schema_2009.AbstractPolymorphicStateSet;
import org.nexml.schema_2009.AbstractState;
import org.nexml.schema_2009.AbstractStates;
import org.nexml.schema_2009.AbstractTree;
import org.nexml.schema_2009.AbstractUncertainStateSet;
import org.nexml.schema_2009.Annotated;
import org.nexml.schema_2009.IntTree;
import org.nexml.schema_2009.NexmlDocument;
import org.nexml.schema_2009.StandardCells;
import org.nexml.schema_2009.StandardChar;
import org.nexml.schema_2009.StandardMapping;
import org.nexml.schema_2009.StandardMatrixObsRow;
import org.nexml.schema_2009.StandardObs;
import org.nexml.schema_2009.StandardPolymorphicStateSet;
import org.nexml.schema_2009.StandardState;
import org.nexml.schema_2009.StandardStates;
import org.nexml.schema_2009.StandardUncertainStateSet;
import org.nexml.schema_2009.Taxa;
import org.nexml.schema_2009.TreeIntEdge;
import org.nexml.schema_2009.TreeNode;
import org.nexml.schema_2009.Trees;
import org.obo.datamodel.LinkedObject;
import org.obo.datamodel.impl.OBOClassImpl;
import org.phenoscape.io.NeXMLUtil.Annotatable;
import org.phenoscape.model.Character;
import org.phenoscape.model.DataSet;
import org.phenoscape.model.MultipleState;
import org.phenoscape.model.MultipleState.MODE;
import org.phenoscape.model.Phenotype;
import org.phenoscape.model.Specimen;
import org.phenoscape.model.State;
import org.phenoscape.model.Taxon;
import org.phenoscape.model.Tree;
import org.w3c.dom.Attr;

public class NeXMLWriter {

	private final NexmlDocument xmlDoc;
	private final String charactersBlockID;
	private DataSet data;
	private String generator;
	private final XmlOptions options = new XmlOptions();
	private final Map<Character, AbstractStates> statesBlocksByCharacter = new HashMap<Character, AbstractStates>();
	private final Map<Taxon, String> otuIDsByTaxon = new HashMap<Taxon, String>();
	private final Set<String> usedUncertainStateSets = new HashSet<String>();

	public NeXMLWriter(String charactersBlockID) {
		this(charactersBlockID, NexmlDocument.Factory.newInstance());
	}

	public NeXMLWriter(String charactersBlockID, NexmlDocument startingDoc) {
		this.charactersBlockID = charactersBlockID;
		this.xmlDoc = startingDoc;
		this.options.setSavePrettyPrint();
		final Map<String, String> suggestedPrefixes = new HashMap<String, String>();
		suggestedPrefixes.put("http://www.nexml.org/2009", "");
		suggestedPrefixes.put(NeXMLUtil.DARWIN_CORE_NAMESPACE, NeXMLUtil.DARWIN_CORE_PREFIX);
		suggestedPrefixes.put(NeXMLUtil.DUBLIN_CORE_NAMESPACE, NeXMLUtil.DUBLIN_CORE_PREFIX);
		suggestedPrefixes.put(NeXMLUtil.PHENOSCAPE_NAMESPACE, NeXMLUtil.PHENOSCAPE_PREFIX);
		suggestedPrefixes.put(NeXMLUtil.RDFS_NAMESPACE, NeXMLUtil.RDFS_PREFIX);
		this.options.setSaveAggressiveNamespaces();
		this.options.setSaveSuggestedPrefixes(suggestedPrefixes);
		this.options.setSaveNamespacesFirst();
		this.options.setUseDefaultNamespace();
		final Map<String, String> implicitNamespaces = new HashMap<String, String>();
		implicitNamespaces.put("http://www.nexml.org/2009", "nex");
	}

	public void setDataSet(DataSet data) {
		this.data = data;
		this.statesBlocksByCharacter.clear();
		this.otuIDsByTaxon.clear();
		this.usedUncertainStateSets.clear();
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
		newDoc.getNexml().setVersion(BigDecimal.valueOf(0.9));
		final Annotatable annotatableNexml = new Annotatable(newDoc.getNexml());
		NeXMLUtil.setMetadata(annotatableNexml, NeXMLUtil.CURATORS_PREDICATE, this.data.getCurators());
		NeXMLUtil.setMetadata(annotatableNexml, NeXMLUtil.PUBLICATION_PREDICATE, this.data.getPublication());
		NeXMLUtil.setMetadata(annotatableNexml, NeXMLUtil.PUBLICATION_NOTES_PREDICATE, this.data.getPublicationNotes());
		final AbstractBlock charBlock = NeXMLUtil.findOrCreateCharactersBlock(newDoc, this.charactersBlockID);
		this.writeCharacters(charBlock);
		final String taxaID;
		if ((charBlock.getOtus() == null) || (charBlock.getOtus().equals(""))) {
			taxaID = "t" + UUID.randomUUID().toString();
			charBlock.setOtus(taxaID);
		} else {
			taxaID = charBlock.getOtus();
		}
		final Taxa taxaBlock = NeXMLUtil.findOrCreateTaxa(newDoc, taxaID);
		this.writeTaxa(taxaBlock);
		// move taxa ahead of characters
		final XmlCursor firstCharCursor = newDoc.getNexml().getCharactersArray()[0].newCursor();
		final XmlCursor taxaCursor = taxaBlock.newCursor();
		taxaCursor.moveXml(firstCharCursor);
		for (AbstractStates statesBlock : charBlock.getFormat().getStatesArray()) {
			final AbstractPolymorphicStateSet[] existingSets = statesBlock.getPolymorphicStateSetArray();
			final List<AbstractPolymorphicStateSet> usedSets = new ArrayList<AbstractPolymorphicStateSet>();
			for (AbstractPolymorphicStateSet stateSet : existingSets) {
				if (this.usedUncertainStateSets.contains(stateSet.getId())) {
					usedSets.add(stateSet);
				}
			}
			statesBlock.setPolymorphicStateSetArray(usedSets.toArray(new AbstractPolymorphicStateSet[] {}));
		}
		for (AbstractStates statesBlock : charBlock.getFormat().getStatesArray()) {
			final AbstractUncertainStateSet[] existingSets = statesBlock.getUncertainStateSetArray();
			final List<AbstractUncertainStateSet> usedSets = new ArrayList<AbstractUncertainStateSet>();
			for (AbstractUncertainStateSet stateSet : existingSets) {
				if (this.usedUncertainStateSets.contains(stateSet.getId())) {
					usedSets.add(stateSet);
				}
			}
			statesBlock.setUncertainStateSetArray(usedSets.toArray(new AbstractUncertainStateSet[] {}));
		}
		final Trees treesBlock = NeXMLUtil.findOrCreateTreesBlock(newDoc, "t" + UUID.randomUUID().toString());
		treesBlock.setOtus(taxaID);
		this.writeTrees(treesBlock);
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
			this.writeDiscussion(xmlChar, character.getDiscussion());
			final AbstractStates statesBlock = this.findOrCreateStatesBlockWithID(existingStatesList, character.getStatesNexmlID());
			final AbstractStates usableStatesBlock;
			if (usedStatesIDs.contains(statesBlock.getId())) {
				usableStatesBlock = (AbstractStates)(statesBlock.copy());
				usableStatesBlock.setId(UUID.randomUUID().toString());
			} else {
				usableStatesBlock = statesBlock;
			}
			this.statesBlocksByCharacter.put(character, usableStatesBlock);
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
		if (charBlock instanceof StandardCells) {
			final StandardCells cells = (StandardCells)charBlock;
			final AbstractObsMatrix matrix = cells.getMatrix() != null ? cells.getMatrix() : cells.addNewMatrix();
			this.writeMatrix(matrix);
		}
		charBlock.getFormat().setStatesArray(newStatesBlocks.toArray(new AbstractStates[] {}));
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
					if (state instanceof MultipleState) {
						xmlState.setStringValue(this.findOrCreateMultiValueState(character, (MultipleState)state).getId());
					} else {
						xmlState.setStringValue(state.getNexmlID());	
					}
					xmlCell.setState(xmlState);
					newCells.add(xmlCell);
				}
			}
			xmlRow.setCellArray(newCells.toArray(new AbstractObs[] {}));
		}
		matrix.setRowArray(newRows.toArray(new AbstractObsRow[] {}));
	}

	private void writeTaxa(Taxa taxaBlock) {
		final List<org.nexml.schema_2009.Taxon> existingOTUs = Arrays.asList(taxaBlock.getOtuArray());
		final List<org.nexml.schema_2009.Taxon> newOTUs = new ArrayList<org.nexml.schema_2009.Taxon>();
		for (Taxon taxon : this.data.getTaxa()) {
			final org.nexml.schema_2009.Taxon otu = this.findOrCreateOTUWithID(existingOTUs, taxon.getNexmlID());
			newOTUs.add(otu);
			otu.setLabel(taxon.getPublicationName());
			this.writeOBOID(otu, taxon);
			this.writeSpecimens(otu, taxon);
			this.writeComment(otu, taxon.getComment());
			this.writeFigure(otu, taxon.getFigure());
			this.writeMatrixTaxon(otu, taxon.getMatrixTaxonName());
			this.otuIDsByTaxon.put(taxon, taxon.getNexmlID());
		}
		taxaBlock.setOtuArray(newOTUs.toArray(new org.nexml.schema_2009.Taxon[] {}));
	}

	private void writeTrees(Trees treesBlock) {
		final List<IntTree> treeList = new ArrayList<IntTree>();
		for (Tree tree : this.data.getTrees()) {
			final IntTree treeXML = IntTree.Factory.newInstance();
			treeList.add(treeXML);
			treeXML.setLabel(StringUtils.stripToNull(tree.getLabel()));
			treeXML.setId(tree.getNexmlID());
			final Map<LinkedObject, TreeNode> treeNodes = new HashMap<LinkedObject, TreeNode>();
			final Map<LinkedObject, LinkedObject> topology = tree.getTopology();
			final List<TreeIntEdge> edges = new ArrayList<TreeIntEdge>();
			for (Entry<LinkedObject, LinkedObject> entry : topology.entrySet()) {
				final LinkedObject childNode = entry.getKey();
				final LinkedObject parentNode = entry.getValue();
				final TreeNode sourceNode;
				if (treeNodes.containsKey(childNode)) {
					sourceNode = treeNodes.get(childNode);
				} else {
					sourceNode = TreeNode.Factory.newInstance();
					sourceNode.setLabel(childNode.getName());
					sourceNode.setId(childNode.getID() + "#" + UUID.randomUUID().toString());
					treeNodes.put(childNode, sourceNode);
				}
				final TreeNode targetNode;
				if (treeNodes.containsKey(parentNode)) {
					targetNode = treeNodes.get(parentNode);
				} else {
					targetNode = TreeNode.Factory.newInstance();
					targetNode.setLabel(parentNode.getName());
					targetNode.setId(parentNode.getID() + "#" + UUID.randomUUID().toString());
					treeNodes.put(parentNode, targetNode);
				}
				final TreeIntEdge newEdge = TreeIntEdge.Factory.newInstance();
				newEdge.setTarget(sourceNode.getId());
				newEdge.setSource(targetNode.getId());
				final XmlAnySimpleType oneXML = XmlAnySimpleType.Factory.newInstance();
				oneXML.setStringValue("1");
				newEdge.setLength(oneXML);
				edges.add(newEdge);
			}
			for (Taxon taxon : this.data.getTaxa()) {
				final String otu = this.otuIDsByTaxon.get(taxon);
				if (otu == null) {
					log().error("No otu for taxon: " + taxon);
				}
				if (taxon.getValidName() != null) {
					final TreeNode treeNode = treeNodes.get(taxon.getValidName());
					if (treeNode == null) {
						log().error("No tree node for taxon: " + taxon);
					}
					if (topology.containsValue(taxon.getValidName())) {
						// this taxon is an internal node
						final TreeNode extraNode = TreeNode.Factory.newInstance();
						extraNode.setLabel(taxon.getValidName().getName() + " species");
						final String uuid = UUID.randomUUID().toString();
						extraNode.setId(taxon.getValidName().getID() + "#" + uuid);
						treeNodes.put(new OBOClassImpl(uuid), extraNode);
						extraNode.setOtu(otu);
						final TreeIntEdge newEdge = TreeIntEdge.Factory.newInstance();
						newEdge.setTarget(extraNode.getId());
						newEdge.setSource(treeNode.getId());
						final XmlAnySimpleType oneXML = XmlAnySimpleType.Factory.newInstance();
						oneXML.setStringValue("1");
						newEdge.setLength(oneXML);
						edges.add(newEdge);
					} else {
						treeNode.setOtu(otu);
					}
				}
			}
			treeXML.setNodeArray(treeNodes.values().toArray(new TreeNode[] {}));
			treeXML.setEdgeArray(edges.toArray(new TreeIntEdge[] {}));
		}
		treesBlock.setTreeArray(treeList.toArray(new IntTree[] {}));
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

	private org.nexml.schema_2009.Taxon findOrCreateOTUWithID(List<org.nexml.schema_2009.Taxon> list, String id) {
		for (org.nexml.schema_2009.Taxon otu : list) {
			if (otu.getId().equals(id)) { return otu; }
		}
		final org.nexml.schema_2009.Taxon newOTU = org.nexml.schema_2009.Taxon.Factory.newInstance();
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

	private AbstractState findOrCreateMultiValueState(Character character, MultipleState state) {
		final AbstractStates block = this.statesBlocksByCharacter.get(character);
		final AbstractUncertainStateSet[] sets = (state.getMode() == MODE.POLYMORPHIC) ? block.getPolymorphicStateSetArray() : block.getUncertainStateSetArray(); 
		for (AbstractUncertainStateSet set : sets) {
			if (this.multipleStateSetMatches(state, set)) {
				this.usedUncertainStateSets.add(set.getId());
				return set;
			}
		}
		final AbstractUncertainStateSet set;
		if (state.getMode() == MODE.POLYMORPHIC) {
			final StandardPolymorphicStateSet polymorphicSet = StandardPolymorphicStateSet.Factory.newInstance(); 
			set = polymorphicSet;
			polymorphicSet.setId(UUID.randomUUID().toString());
			final AbstractPolymorphicStateSet[] oldStates = block.getPolymorphicStateSetArray();
			final List<AbstractPolymorphicStateSet> newStates = new ArrayList<AbstractPolymorphicStateSet>();
			newStates.addAll(Arrays.asList(oldStates));

			final List<AbstractMapping> mappings = new ArrayList<AbstractMapping>();
			for (State substate : state.getStates()) {
				final StandardMapping mapping = StandardMapping.Factory.newInstance();
				mapping.setState(substate.getNexmlID());
				mappings.add(mapping);
			}
			set.setMemberArray(mappings.toArray(new AbstractMapping[] {}));
			newStates.add(polymorphicSet);
			block.setPolymorphicStateSetArray(newStates.toArray(new AbstractPolymorphicStateSet[] {}));
		} else {
			final StandardUncertainStateSet uncertainSet = StandardUncertainStateSet.Factory.newInstance(); 
			set = uncertainSet;
			uncertainSet.setId(UUID.randomUUID().toString());
			final AbstractUncertainStateSet[] oldStates = block.getUncertainStateSetArray();
			final List<AbstractUncertainStateSet> newStates = new ArrayList<AbstractUncertainStateSet>();
			newStates.addAll(Arrays.asList(oldStates));

			final List<AbstractMapping> mappings = new ArrayList<AbstractMapping>();
			for (State substate : state.getStates()) {
				final StandardMapping mapping = StandardMapping.Factory.newInstance();
				mapping.setState(substate.getNexmlID());
				mappings.add(mapping);
			}
			set.setMemberArray(mappings.toArray(new AbstractMapping[] {}));
			newStates.add(uncertainSet);
			block.setUncertainStateSetArray(newStates.toArray(new AbstractUncertainStateSet[] {}));
		}
		this.usedUncertainStateSets.add(set.getId());
		return set;
	}

	private boolean multipleStateSetMatches(MultipleState state, AbstractUncertainStateSet set) {
		final Set<String> stateIDs = new HashSet<String>();
		for (State substate : state.getStates()) {
			stateIDs.add(substate.getNexmlID());
		}
		final Set<String> setStateIDs = new HashSet<String>();
		for (AbstractMapping mapping : set.getMemberArray()) {
			setStateIDs.add(mapping.getState());
		}
		return stateIDs.equals(setStateIDs);
	}

	private void writeOBOID(org.nexml.schema_2009.Taxon otu, Taxon taxon) {
		final Annotatable annotatableOTU = new Annotatable(otu);
		if (taxon.getValidName() == null) {
			NeXMLUtil.unsetMetadata(annotatableOTU, NeXMLUtil.VALID_NAME_PREDICATE);
		} else {
			NeXMLUtil.setMetadata(annotatableOTU, NeXMLUtil.VALID_NAME_PREDICATE, NeXMLUtil.oboURI(taxon.getValidName()));
		}
	}

	private void writeSpecimens(org.nexml.schema_2009.Taxon otu, Taxon taxon) {
		final Annotatable annotatableOTU = new Annotatable(otu);
		NeXMLUtil.unsetMetadata(annotatableOTU, NeXMLUtil.SPECIMEN_PREDICATE);
		for (Specimen specimen : taxon.getSpecimens()) {
			final Map<QName, Object> specimenData = new HashMap<QName, Object>();
			if (specimen.getCollectionCode() != null) {
				specimenData.put(NeXMLUtil.COLLECTION_PREDICATE, NeXMLUtil.oboURI(specimen.getCollectionCode()));    
			}
			if (specimen.getCatalogID() != null) {
				specimenData.put(NeXMLUtil.ACCESSION_PREDICATE, specimen.getCatalogID());    
			}
			if (specimen.getComment() != null) {
				specimenData.put(NeXMLUtil.COMMENT_PREDICATE, specimen.getComment());
			}
			NeXMLUtil.addMetadata(annotatableOTU, NeXMLUtil.SPECIMEN_PREDICATE, specimenData);
		}

	}

	private void writeComment(Annotated node, String comment) {
		final Annotatable annotatableNode = new Annotatable(node);
		if ((comment == null) || (comment.equals(""))) {
			NeXMLUtil.unsetMetadata(annotatableNode, NeXMLUtil.COMMENT_PREDICATE);
		} else {
			NeXMLUtil.setMetadata(annotatableNode, NeXMLUtil.COMMENT_PREDICATE, comment);
		}
	}

	private void writeFigure(Annotated node, String figure) {
		final Annotatable annotatableNode = new Annotatable(node);
		if ((figure == null) || (figure.equals(""))) {
			NeXMLUtil.unsetMetadata(annotatableNode, NeXMLUtil.FIGURE_PREDICATE);
		} else {
			NeXMLUtil.setMetadata(annotatableNode, NeXMLUtil.FIGURE_PREDICATE, figure);
		}
	}

	private void writeDiscussion(Annotated node, String discussion) {
		final Annotatable annotatableNode = new Annotatable(node);
		if ((discussion == null) || (discussion.equals(""))) {
			NeXMLUtil.unsetMetadata(annotatableNode, NeXMLUtil.DISCUSSION_PREDICATE);
		} else {
			NeXMLUtil.setMetadata(annotatableNode, NeXMLUtil.DISCUSSION_PREDICATE, discussion);
		}
	}

	private void writeMatrixTaxon(org.nexml.schema_2009.Taxon otu, String matrixTaxon) {
		final Annotatable annotatableOTU = new Annotatable(otu);
		if ((matrixTaxon == null) || (matrixTaxon.equals(""))) {
			NeXMLUtil.unsetMetadata(annotatableOTU, NeXMLUtil.MATRIX_NAME_PREDICATE);
		} else {
			NeXMLUtil.setMetadata(annotatableOTU, NeXMLUtil.MATRIX_NAME_PREDICATE, matrixTaxon);
		}
	}

	private void writePhenotypes(AbstractState xmlState, State state) {
		final Annotatable annotatableState = new Annotatable(xmlState);
		if (state.getPhenotypes().isEmpty()) {
			NeXMLUtil.unsetMetadata(annotatableState, NeXMLUtil.PHENOTYPE_PREDICATE);
			return;
		} else {
			final List<PhenotypeCharacter> pcs = new ArrayList<PhenotypeCharacter>();
			for (Phenotype phenotype : state.getPhenotypes()) {
				final PhenotypeCharacter pc = PhenoXMLAdapter.createPhenotypeCharacter(phenotype);
				if (pc != null) { pcs.add(pc); }
			}
			final org.bioontologies.obd.schema.pheno.PhenotypeDocument.Phenotype phenoXML = PhenoXMLAdapter.createPhenotype(pcs);
			// for some reason the PhenoXML Phenotype appears only as a DocumentFragment instead of Element until it's stuck into a PhenotypeManifestation
			final PhenotypeManifestation scratchPM = PhenotypeManifestation.Factory.newInstance();
			scratchPM.setPhenotype(phenoXML);
			NeXMLUtil.setMetadata(annotatableState, NeXMLUtil.PHENOTYPE_PREDICATE, scratchPM.getPhenotype().getDomNode());
		}
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

	private Logger log() {
		return Logger.getLogger(this.getClass());
	}

}
