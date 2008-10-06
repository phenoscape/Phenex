package org.phenoscape.bridge;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.obd.model.CompositionalDescription;
import org.obd.model.Graph;
import org.obd.model.LinkStatement;
import org.obd.model.Node;
import org.obd.model.CompositionalDescription.Predicate;
import org.obd.model.Node.Metatype;
import org.obd.model.vocabulary.TermVocabulary;
import org.obo.datamodel.Link;
import org.obo.datamodel.LinkedObject;
import org.obo.datamodel.OBOClass;
import org.phenoscape.model.Character;
import org.phenoscape.model.DataSet;
import org.phenoscape.model.Phenotype;
import org.phenoscape.model.State;
import org.phenoscape.model.Taxon;
import org.purl.obo.vocab.RelationVocabulary;

import phenote.datamodel.OboUtil;


public class OBDModelBridge2 {

	protected Graph graph;
	// CDAO vacab : TODO
	public static String DATASET_TYPE_ID = "cdao:CharacterStateDataMatrix";
	public static String STATE_TYPE_ID = "cdao:CharacterStateDomain";
	public static String CELL_TYPE_ID = "cdao:CharacterStateDatum";
	public static String CHARACTER_TYPE_ID = "cdao:Character";
	public static String PUBLICATION_TYPE_ID = "cdao:Pub"; // TODO
	
	public static String HAS_PUB_REL_ID = "cdao:hasPub";
	public static String HAS_STATE_REL_ID = "cdao:has_Datum";
	public static String REFERS_TO_TAXON_REL_ID = "cdao:hasTaxon"; // has_TU? TODO
	public static String HAS_CHARACTER_REL_ID = "cdao:has_Character"; // 
	public static String HAS_PHENOTYPE_REL_ID = "cdao:has_Phenotype"; // TODO
	public static String TAXON_PHENOTYPE_REL_ID = "exhibits"; // TODO
	public static String CELL_TO_STATE_REL_ID = "cdao:has_State"; // TODO
	public static String ANNOT_TO_CELL_REL_ID = "has_source"; // TODO
	private static TermVocabulary vocab = new TermVocabulary();
	private static RelationVocabulary relationVocabulary = new RelationVocabulary();
	private Map<Character,String> characterIdMap = new HashMap<Character,String>();
	private Map<State,String> stateIdMap = new HashMap<State,String>();
	private Map<Taxon,String> taxonIdMap = new HashMap<Taxon,String>();
	private Map<Phenotype,String> phenotypeIdMap = new HashMap<Phenotype,String>();
	
	
	public OBDModelBridge2() {
		super();
		graph = new Graph();
	}

	
	
	public Graph getGraph() {
		return graph;
	}

	public void setGraph(Graph graph) {
		this.graph = graph;
	}

	public Graph translate(DataSet ds) {
		String dsId = UUID.randomUUID().toString();
		
		// Dataset metadata
		Node dsNode = createInstanceNode(dsId,DATASET_TYPE_ID);
		
		// link publication to dataset
		Node pubNode = createInstanceNode(ds.getPublication(),PUBLICATION_TYPE_ID);
		LinkStatement ds2p = new LinkStatement(dsId, HAS_PUB_REL_ID, pubNode.getId());
		graph.addStatement(ds2p);

		// link dataset to taxa
		for (Taxon t : ds.getTaxa()) {
			Node tn = translate(t);
			taxonIdMap.put(t,tn.getId());
			LinkStatement ds2t = new LinkStatement(dsId, REFERS_TO_TAXON_REL_ID, tn.getId());
			graph.addStatement(ds2t);
		}

		// link dataset to characters used in that dataset
		for (Character character : ds.getCharacters()) {
			String cid = UUID.randomUUID().toString();
			Node characterNode = createInstanceNode(cid,CHARACTER_TYPE_ID);
			characterNode.setLabel(character.getLabel());
			characterIdMap.put(character, cid);
			LinkStatement ds2c = new LinkStatement(dsId, HAS_CHARACTER_REL_ID, cid);
			graph.addStatement(ds2c);

			for (State state : character.getStates()) {
				String sid = UUID.randomUUID().toString();
				Node stateNode = createInstanceNode(sid,STATE_TYPE_ID);
				stateNode.setLabel(state.getLabel());
				stateIdMap.put(state, sid);
				LinkStatement c2s = new LinkStatement(cid, HAS_STATE_REL_ID, sid);
				graph.addStatement(c2s);
				for (Phenotype p : state.getPhenotypes()) {
					CompositionalDescription cd = translate(p);
					phenotypeIdMap.put(p, cd.getId());
					LinkStatement s2p = new LinkStatement(sid, HAS_PHENOTYPE_REL_ID, cd.getId());
					graph.addStatement(s2p);
				}
			}
			
		}

		// Matrix -> annotations
		for (Taxon t : ds.getTaxa()) {
			for (Character c : ds.getCharacters()) {
				State state = ds.getStateForTaxon(t, c);
				if (state == null) {
					System.err.println("no state for t:"+t+" char:"+c);
					continue;
				}
				for (Phenotype p : state.getPhenotypes()) {
					// taxon to phenotype
					LinkStatement annotLink = new LinkStatement();
					annotLink.setNodeId(taxonIdMap.get(t));
					annotLink.setTargetId(phenotypeIdMap.get(p));
					annotLink.setRelationId(TAXON_PHENOTYPE_REL_ID);
					annotLink.addSubLinkStatement("posited_by", dsId);
					graph.addStatement(annotLink);
					
					// link description of biology back to data
					Node cellNode = createInstanceNode(UUID.randomUUID().toString(),CELL_TYPE_ID);
					annotLink.addSubLinkStatement(CELL_TO_STATE_REL_ID,cellNode.getId());
					
					// cell to state
					LinkStatement cell2s = new LinkStatement(cellNode.getId(), CELL_TO_STATE_REL_ID, stateIdMap.get(state)); // TODO
					graph.addStatement(cell2s);
				}
			}
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

		CompositionalDescription cd = new CompositionalDescription(Predicate.INTERSECTION);
		this.addArgumentToCompositionalDescription(cd, q);
		this.addArgumentToCompositionalDescription(cd, relationVocabulary.inheres_in(), e);
		if (e2 != null) {
		  this.addArgumentToCompositionalDescription(cd, relationVocabulary.towards(), e2);
		}
		if (false) {
			if (u == null && m != null) {
				// TODO : throw
			}
			if (m != null) {
				cd.addArgument("has_unit",u.getID());
				//cd.addArgument("has_measurement",m);
			}
		}
		cd.setId(cd.generateId());
		getGraph().addStatements(cd);
		return cd;
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
		n.addStatement(new LinkStatement(id,relationVocabulary.instance_of(), typeId));
		graph.addNode(n);
		return n;
	}
	
	private void addArgumentToCompositionalDescription(CompositionalDescription cd, OBOClass target) {
    if (OboUtil.isPostCompTerm(target)) {
      cd.addArgument(this.makeCompositionalDescription(target));
    } else {
      cd.addArgument(target.getID());
    }
	}
	
	private void addArgumentToCompositionalDescription(CompositionalDescription cd, String relation, OBOClass target) {
	  if (OboUtil.isPostCompTerm(target)) {
      cd.addArgument(relation, this.makeCompositionalDescription(target));
    } else {
      cd.addArgument(relation, target.getID());
    }
	}
	
	private CompositionalDescription makeCompositionalDescription(OBOClass postComposition) {
	  CompositionalDescription cd = new CompositionalDescription(Predicate.INTERSECTION);
	  final OBOClass genus = OboUtil.getGenusTerm(postComposition);
	  if (OboUtil.isPostCompTerm(genus)) {
	    cd.addArgument(this.makeCompositionalDescription(genus));
	  } else {
	    cd.addArgument(genus.getID());
	  }
	  for (Link link : OboUtil.getAllDifferentia(postComposition)) {
	    final String rel = link.getType().getID();
	    final LinkedObject parent = link.getParent();
	    this.addArgumentToCompositionalDescription(cd, rel, (OBOClass)parent);
	  }
	  cd.setId(cd.generateId());
	  return cd;
	}
	
}
