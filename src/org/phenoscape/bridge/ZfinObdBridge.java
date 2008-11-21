package org.phenoscape.bridge;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.obd.model.CompositionalDescription;
import org.obd.model.Graph;
import org.obd.model.LinkStatement;
import org.obd.model.Node;
import org.obd.model.CompositionalDescription.Predicate;
import org.obd.model.Node.Metatype;
import org.obd.query.Shard;
import org.obd.query.impl.AbstractSQLShard;
import org.obd.query.impl.OBDSQLShard;
import org.purl.obo.vocab.RelationVocabulary;

public class ZfinObdBridge {

	protected Graph graph;

	public static String GENOTYPE_PHENOTYPE_REL_ID = "PHENOSCAPE:exhibits";
	public static String GENE_GENOTYPE_REL_ID = "PHENOSCAPE:hasAllele";
	public static String PUBLICATION_TYPE_ID = "cdao:Pub";
	public static String HAS_PUB_REL_ID = "cdao:hasPub";
	public static String GENOTYPE_TYPE_ID = "SO:0001027";
	public static String GENE_TYPE_ID = "SO:0000704";

	Set<String> geneSet = new HashSet<String>();
	Set<String> genotypeSet = new HashSet<String>();
	Set<String> pubSet = new HashSet<String>();

	private static RelationVocabulary relationVocabulary = new RelationVocabulary();

	public ZfinObdBridge() {
		super();
		graph = new Graph();
	}

	public void loadZfinData(Shard obdsql) {
		try {
			File connParamFile = new File("testfiles/zfinLocations");
			BufferedReader br = new BufferedReader(
					new FileReader(connParamFile));
			URL phenotypeURL = null, genotypeURL = null;
			String param, phenoFileLine, genoFileLine;
			LinkStatement genotypeAnnotLink = new LinkStatement();
			LinkStatement geneAnnotLink = new LinkStatement();
			while ((param = br.readLine()) != null) {
				if (param.contains("pheno"))
					phenotypeURL = new URL(param);
				else
					genotypeURL = new URL(param);
			}

			if (phenotypeURL != null) {
				BufferedReader br1 = new BufferedReader(new InputStreamReader(
						phenotypeURL.openStream()));
				while ((phenoFileLine = br1.readLine()) != null) {
					String[] pComps = phenoFileLine.split("\\t");
					String pub = pComps[8];
					String qualID = pComps[5];
					String entity1ID = pComps[4];
					String genotypeId = pComps[0];
					String genotypeName = pComps[1];

					genotypeSet.add(genotypeId + "\t" + genotypeName);
					pubSet.add(pub);

					if (entity1ID != null && entity1ID.trim().length() > 0) {
						// Collection<Statement> stmts = obdsql
						// .getStatementsForTarget(entity1ID);
						// Iterator<Statement> it = stmts.iterator();

						// if (it.hasNext()) {
						// while (it.hasNext()) {
						// Statement stmt = it.next();
						// if (stmt.getRelationId().contains("DbXref")) {
						// String equivEntityID = stmt.getNodeId();
						// e1 = new OBOClassImpl(equivEntityID);
						// }
						// }
						// }
						if (qualID != null && qualID.trim().length() > 0) {
							CompositionalDescription cd = new CompositionalDescription(
									Predicate.INTERSECTION);
							cd.addArgument(qualID);
							cd.addArgument(relationVocabulary.inheres_in(),
									entity1ID);
							genotypeAnnotLink.setNodeId(genotypeId);
							genotypeAnnotLink
									.setRelationId(GENOTYPE_PHENOTYPE_REL_ID);
							genotypeAnnotLink.setTargetId(cd.toString());
							// genotypeAnnotLink.addSubLinkStatement(
							// HAS_PUB_REL_ID, pub);
							// System.err.println("Adding genotype statement "
							// + genotypeAnnotLink.toString());
							graph.addStatement(genotypeAnnotLink);
						}

					}
				}
				for (String gID : genotypeSet) {
					String[] comps = gID.split("\\t");
					Node genotypeNode = createInstanceNode(comps[0],
							GENOTYPE_TYPE_ID);
					genotypeNode.setLabel(comps[1]);
					// System.err.println("Adding node " + genotypeNode.getId()
					// + "of type " + GENOTYPE_TYPE_ID);
					graph.addNode(genotypeNode);
				}
				for (String pub : pubSet) {
					Node publicationNode = createInstanceNode(pub,
							PUBLICATION_TYPE_ID);
					graph.addNode(publicationNode);
				}
			} else {
				System.err.println("Uninitialized URL for phenotypic data");
			}

			if (genotypeURL != null) {
				BufferedReader br2 = new BufferedReader(new InputStreamReader(
						genotypeURL.openStream()));
				while ((genoFileLine = br2.readLine()) != null) {
					String[] gComps = genoFileLine.split("\\t");
					String geneID = gComps[4];
					String genotypeID = gComps[0];
					if (geneID != null && geneID.trim().length() > 0) {
						geneSet.add(geneID);
						geneAnnotLink.setNodeId(geneID);
						geneAnnotLink.setRelationId(GENE_GENOTYPE_REL_ID);
						geneAnnotLink.setTargetId(genotypeID);
						// System.err.println("Adding gene statement " +
						// geneAnnotLink);
						graph.addStatement(geneAnnotLink);
					}
				}
			} else {
				System.err.println("Uninitialized URL for genotypic data");
			}
			for(String gene : geneSet){
				Node geneNode = createInstanceNode(gene, GENE_TYPE_ID);
				graph.addNode(geneNode);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		obdsql.putGraph(graph);
	}

	protected Node createInstanceNode(String id, String typeId) {
		Node n = new Node(id);
		n.setMetatype(Metatype.CLASS);
		n.addStatement(new LinkStatement(id, relationVocabulary.instance_of(),
				typeId));
		graph.addNode(n);
		return n;
	}

	public static void main(String[] args) {
		File connParamFile = new File("testfiles/connectionParameters");
		try {
			BufferedReader br = new BufferedReader(
					new FileReader(connParamFile));
			String[] connParams = new String[3];
			String param;
			int j = 0;
			while ((param = br.readLine()) != null) {
				connParams[j++] = param;
			}

			Shard obdsql = new OBDSQLShard();
			((AbstractSQLShard) obdsql).connect(connParams[0], connParams[1],
					connParams[2]);
			ZfinObdBridge zob = new ZfinObdBridge();
			zob.loadZfinData(obdsql);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
