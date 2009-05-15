package org.phenoscape.bridge;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.obd.model.CompositionalDescription;
import org.obd.model.Graph;
import org.obd.model.LinkStatement;
import org.obd.model.LiteralStatement;
import org.obd.model.Node;
import org.obd.model.Statement;
import org.obd.model.CompositionalDescription.Predicate;
import org.obd.model.Node.Metatype;
import org.obd.model.vocabulary.TermVocabulary;
import org.obo.datamodel.Link;
import org.obo.datamodel.OBOClass;
import org.obo.util.ReasonerUtil;
import org.obo.util.TermUtil;
import org.phenoscape.model.Character;
import org.phenoscape.model.DataSet;
import org.phenoscape.model.Phenotype;
import org.phenoscape.model.Specimen;
import org.phenoscape.model.State;
import org.phenoscape.model.Taxon;
import org.purl.obo.vocab.RelationVocabulary;

/**
 * Bridges between phenoscape objects and a model expressed using OBO
 * primitives.
 * 
 * OBD Instances and the relations between them generally reflect java instances
 * of the corresponding phenoscape model. We model cells, states and
 * matrixes/datasets using instances of CDAO owl classes. We go beyond the
 * phenoscape model and CDAO in positing an annotation link between the
 * phenotype and the taxon. The annotation is linked to the cell.
 * 
 * TODO: reverse mapping TODO: finalize relations and classes; both from OBO_REL
 * and CDAO TODO: make this subclass a generic bridge framework
 * 
 * @author cjm
 * 
 */
public class OBDModelBridge {

	protected Graph graph;
	
	public static String DATASET_TYPE_ID = "cdao:CharacterStateDataMatrix";
	public static String STATE_TYPE_ID = "cdao:CharacterStateDomain";
	public static String CELL_TYPE_ID = "cdao:CharacterStateDatum";
	public static String CHARACTER_TYPE_ID = "cdao:Character";
	public static String PUBLICATION_TYPE_ID = "PHENOSCAPE:Publication"; 
	public static String OTU_TYPE_ID = "cdao:TU";
	public static String SPECIMEN_TYPE_ID = "PHENOSCAPE:Specimen";

	public static String HAS_PUB_REL_ID = "PHENOSCAPE:has_publication";
	public static String HAS_SPECIMEN_REL_ID = "PHENOSCAPE:has_specimen";
	public static String HAS_STATE_REL_ID = "cdao:has_Datum";
	public static String REFERS_TO_TAXON_REL_ID = "PHENOSCAPE:has_taxon"; 
	public static String HAS_TU_REL_ID = "cdao:has_TU";
	
	public static String HAS_CHARACTER_REL_ID = "cdao:has_Character"; 
	public static String HAS_PHENOTYPE_REL_ID = "cdao:has_Phenotype"; 
	public static String TAXON_PHENOTYPE_REL_ID = "PHENOSCAPE:exhibits"; 
	public static String CELL_TO_STATE_REL_ID = "cdao:has_State"; 
	public static String ANNOT_TO_CELL_REL_ID = "PHENOSCAPE:has_source"; 
	public static String SPECIMEN_TO_COLLECTION_REL_ID = "PHENOSCAPE:belongs_to_collection";
	public static String SPECIMEN_TO_CATALOG_ID_REL_ID = "PHENOSCAPE:has_catalog_id";
	
	public static String HAS_CURATORS_REL_ID = "PHENOSCAPE:has_curators";
	
	private static TermVocabulary vocab = new TermVocabulary();
	private static RelationVocabulary relationVocabulary = new RelationVocabulary();
	private Map<Character, String> characterIdMap;
	private Map<State, String> stateIdMap;
	private Map<Taxon, String> taxonIdMap;
	private Map<Phenotype, String> phenotypeIdMap;
	protected BufferedWriter problemLog;
	
	protected Set<LinkStatement> phenotypes;
	
	public Graph getGraph() {
		return graph;
	}

	public void setGraph(Graph graph) {
		this.graph = graph;
	}

	public Graph translate(DataSet ds, File dataFile) {
		String dsId = UUID.randomUUID().toString();
		graph = new Graph();
		phenotypes = new HashSet<LinkStatement>();
		characterIdMap = new HashMap<Character, String>();
		stateIdMap = new HashMap<State, String>();
		taxonIdMap = new HashMap<Taxon, String>();
		phenotypeIdMap = new HashMap<Phenotype, String>();
		// Dataset metadata
		Node dsNode = createInstanceNode(dsId, DATASET_TYPE_ID);
		String curators = ds.getCurators();
		Node pubNode = createInstanceNode(ds.getPublication(),
				PUBLICATION_TYPE_ID);
		LinkStatement ds2p = new LinkStatement(dsId, HAS_PUB_REL_ID, pubNode.getId());
		graph.addStatement(ds2p);
		
		LiteralStatement ds2curators = new LiteralStatement(dsId, HAS_CURATORS_REL_ID, curators);
		graph.addStatement(ds2curators);
		
		for (Taxon t : ds.getTaxa()) {
			// avoid uploading taxa without names; Cartik1.0
			if (t.getValidName() != null
					&& t.getValidName().getName().length() > 0) {
				Node tn = translate(t);
				if (tn.getId() != null) {
					taxonIdMap.put(t, tn.getId());
					String otuId = UUID.randomUUID().toString();
					Node otuNode = createInstanceNode(otuId, OTU_TYPE_ID);
					otuNode.setLabel(t.getValidName().getName());
					// link dataset to taxa
					LinkStatement ds2otu = new LinkStatement(dsId, HAS_TU_REL_ID, otuNode.getId());
					graph.addStatement(ds2otu);
					//link otu to taxon
					LinkStatement otu2t = new LinkStatement(otuId,
							REFERS_TO_TAXON_REL_ID, tn.getId());
					graph.addStatement(otu2t);
					//link otu to specimens
					for(Specimen s : t.getSpecimens()){
						//System.out.println(s.toString());
						if(s.getCollectionCode() != null){
							Node specimenNode = createInstanceNode(s.toString(), SPECIMEN_TYPE_ID);
							LinkStatement otu2specimen = new LinkStatement(otuId, HAS_SPECIMEN_REL_ID, specimenNode.getId());
							graph.addStatement(otu2specimen);
						//link speciment to collection 
							LinkStatement specimen2collection = new LinkStatement(s.toString(), SPECIMEN_TO_COLLECTION_REL_ID, 
									s.getCollectionCode().getID());
							graph.addStatement(specimen2collection);
						//link specimen to catalog id
							LinkStatement specimen2catalogId = new LinkStatement(s.toString(), SPECIMEN_TO_CATALOG_ID_REL_ID, s.getCatalogID());
							graph.addStatement(specimen2catalogId);
						}
					}
				}
			}
		}

		// link dataset to characters used in that dataset
		for (Character character : ds.getCharacters()) {
			// if (character.toString().length() > 0) {
			String cid = UUID.randomUUID().toString();
			Node characterNode = createInstanceNode(cid, CHARACTER_TYPE_ID);
			characterNode.setLabel(character.getLabel());
			characterIdMap.put(character, cid);
			LinkStatement ds2c = new LinkStatement(dsId, HAS_CHARACTER_REL_ID,
					cid);
			graph.addStatement(ds2c);

			for (State state : character.getStates()) {
				String sid = UUID.randomUUID().toString();
				Node stateNode = createInstanceNode(sid, STATE_TYPE_ID);
				stateNode.setLabel(state.getLabel());
				stateIdMap.put(state, sid);
				LinkStatement c2s = new LinkStatement(cid, HAS_STATE_REL_ID,
						sid);
				graph.addStatement(c2s);
				for (Phenotype p : state.getPhenotypes()) {
					// a minimal check: Cartik1.0
					if (p.getEntity() != null && p.getQuality() != null) {
						CompositionalDescription cd = translate(p);
						if (cd.getId() != null && cd.getId().length() > 0) {
							phenotypeIdMap.put(p, cd.getId());
							LinkStatement s2p = new LinkStatement(sid,
									HAS_PHENOTYPE_REL_ID, cd.getId());
							graph.addStatement(s2p);
						}
					}
				}
			}
		}

		// Matrix -> annotations
		for (Taxon t : ds.getTaxa()) {
			for (Character c : ds.getCharacters()) {
				State state = ds.getStateForTaxon(t, c);
				if (state == null) {
					// System.err.println("no state for t:"+t+" char:"+c);
					continue;
				}
				for (Phenotype p : state.getPhenotypes()) {
					// taxon to phenotype
					LinkStatement annotLink = new LinkStatement();
					if (phenotypeIdMap.get(p) != null
							&& taxonIdMap.get(t) != null) {
						annotLink.setNodeId(taxonIdMap.get(t));
						annotLink.setTargetId(phenotypeIdMap.get(p));
						annotLink.setRelationId(TAXON_PHENOTYPE_REL_ID);
						annotLink.addSubLinkStatement("posited_by", dsId);
						
						// link description of biology back to data
						Node cellNode = createInstanceNode(UUID.randomUUID()
								.toString(), CELL_TYPE_ID);
						annotLink.addSubLinkStatement(CELL_TO_STATE_REL_ID,
								cellNode.getId());
						phenotypes.add(annotLink);
						// cell to state
						LinkStatement cell2s = new LinkStatement(cellNode.getId(),
								CELL_TO_STATE_REL_ID, stateIdMap.get(state)); 
						graph.addStatement(cell2s);
					}
				}
			}
		}
		for(Statement stmt : phenotypes){
			graph.addStatement(stmt);
		}
		return graph;
	}

	public CompositionalDescription translate(Phenotype p) {
		OBOClass e = p.getEntity();
		OBOClass q = p.getQuality();
		OBOClass e2 = p.getRelatedEntity();
		OBOClass u = p.getUnit();
		Integer count = p.getCount();
		Float m = p.getMeasurement();

		CompositionalDescription cd = new CompositionalDescription(
				Predicate.INTERSECTION);
		cd.addArgument(q.getID());
		// check to avoid a NullPointerException
		if (e.getParents() != null) {
			cd.addArgument(relationVocabulary.inheres_in(),
					translateOBOClass(e));
		}
		if (e2 != null)
			cd.addArgument(relationVocabulary.towards(), translateOBOClass(e2));
		if (false) {
			if (u == null && m != null) {
				// TODO : throw
			}
			if (m != null) {
				cd.addArgument("has_unit", u.getID());
				// cd.addArgument("has_measurement",m);
			}
		}
		cd.setId(cd.generateId());
		getGraph().addStatements(cd);
		return cd;
	}

	public CompositionalDescription translateOBOClass(OBOClass c) {
		if (TermUtil.isIntersection(c)) {
			CompositionalDescription cd = new CompositionalDescription(
					Predicate.INTERSECTION);
			cd.setId(c.getID());
			OBOClass g = ReasonerUtil.getGenus(c);
			cd.addArgument(translateOBOClass(g));
			Collection<Link> diffs = ReasonerUtil.getDifferentia(c);
			for (Link diff : diffs) {
				cd.addArgument(diff.getType().getID(),
						translateOBOClass((OBOClass) diff.getParent()));
			}
			return cd;
		} else {
			CompositionalDescription d = new CompositionalDescription(
					Predicate.ATOM);
			d.setNodeId(c.getID());
			return d;
		}
	}

	public Node translate(Taxon taxon) {
		Node n = new Node(taxon.getValidName().getID());
		n.setLabel(taxon.getValidName().getName());
		graph.addNode(n);
		return n;
	}

	protected Node createInstanceNode(String id, String typeId) {
		Node n = new Node(id);
		n.setMetatype(Metatype.CLASS);
		n.addStatement(new LinkStatement(id, relationVocabulary.instance_of(),
				typeId));
		graph.addNode(n);
		return n;
	}
}
