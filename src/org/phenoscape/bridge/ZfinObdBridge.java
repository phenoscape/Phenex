package org.phenoscape.bridge;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URL;

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
	public static String GENE_GENOTYPE_REL_ID = "PHENOSCAPE:has_allele";
	public static String PUBLICATION_TYPE_ID = "cdao:Pub";
	public static String HAS_PUB_REL_ID = "cdao:hasPub";
	public static String GENOTYPE_TYPE_ID = "SO:0001027";
	public static String GENE_TYPE_ID = "SO:0000704";

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
			URL phenotypeURL = null, genotypeURL = null, missingMarkersURL = null;
			String param, phenoFileLine, genoFileLine, missingMarkersFileLine;
			LinkStatement genotypeAnnotLink;
			LinkStatement geneAnnotLink;
			while ((param = br.readLine()) != null) {
				if (param.contains("pheno"))
					phenotypeURL = new URL(param);
				else if (param.contains("missing_markers"))
					missingMarkersURL = new URL(param);
				else {
					genotypeURL = new URL(param);
				}
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

					Node genotypeNode = createInstanceNode(genotypeId,
							GENOTYPE_TYPE_ID);
					genotypeNode.setLabel(genotypeName);
					graph.addNode(genotypeNode);

					Node publicationNode = createInstanceNode(pub,
							PUBLICATION_TYPE_ID);
					graph.addNode(publicationNode);

					if (entity1ID != null && entity1ID.trim().length() > 0) {
						if (qualID != null && qualID.trim().length() > 0) {
							CompositionalDescription cd = new CompositionalDescription(
									Predicate.INTERSECTION);
							cd.addArgument(qualID);
							cd.addArgument(relationVocabulary.inheres_in(),
									entity1ID);
							genotypeAnnotLink = new LinkStatement();
							genotypeAnnotLink.setNodeId(genotypeId);
							genotypeAnnotLink
									.setRelationId(GENOTYPE_PHENOTYPE_REL_ID);
							genotypeAnnotLink.setTargetId(cd.toString());
							genotypeAnnotLink.addSubLinkStatement(HAS_PUB_REL_ID, pub);
							graph.addStatement(genotypeAnnotLink);
						}
					}
				}
			} else {
				System.err.println("Uninitialized URL for phenotypic data");
			}

			if (genotypeURL != null && missingMarkersURL != null) {
				BufferedReader br2 = new BufferedReader(new InputStreamReader(
						genotypeURL.openStream()));
				BufferedReader br3 = new BufferedReader(new InputStreamReader(
						missingMarkersURL.openStream()));

				while ((genoFileLine = br2.readLine()) != null) {
					String geneID = "", geneName = "";
					String[] gComps = genoFileLine.split("\\t");
					String genotypeID = normalizetoZfin(gComps[0]);
					if (gComps.length > 9) {
						geneID = normalizetoZfin(gComps[9]);
						geneName = gComps[8];
					}

					if (geneID != null && geneID.trim().length() > 0) {
						Node geneNode = createInstanceNode(geneID, GENE_TYPE_ID);
						geneNode.setLabel(geneName);
						graph.addNode(geneNode);
						geneAnnotLink = new LinkStatement();
						geneAnnotLink.setNodeId(geneID);
						geneAnnotLink.setRelationId(GENE_GENOTYPE_REL_ID);
						geneAnnotLink.setTargetId(genotypeID);
						graph.addStatement(geneAnnotLink);
					} else {
						while ((missingMarkersFileLine = br3.readLine()) != null) {
							String[] mmComps = missingMarkersFileLine
									.split("\\t");
							if (mmComps[0].equals(gComps[0])) {
								if (mmComps[4] != null
										&& mmComps[4].trim().length() > 0) {
									geneID = normalizetoZfin(mmComps[4]);
									Node geneNode = createInstanceNode(geneID,
											GENE_TYPE_ID);
									geneNode.setLabel(mmComps[3]);
									graph.addNode(geneNode);
									geneAnnotLink = new LinkStatement();
									geneAnnotLink.setNodeId(geneID);
									geneAnnotLink
											.setRelationId(GENE_GENOTYPE_REL_ID);
									geneAnnotLink.setTargetId(genotypeID);
									graph.addLinkStatement(geneNode,
											GENE_GENOTYPE_REL_ID, genotypeID);
								}
							}
						}
					}
				}
			} else {
				System.err.println("Uninitialized URL for genotypic data");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		obdsql.putGraph(graph);
	}

	/**
	 * Another helper method to deal with the prefix inconsistencies in ZFIN
	 * Gene to Genotype files lack the ZFIN prefix Genotype to Phenotype files
	 * have the prefix
	 * 
	 * @param string
	 * @return
	 */
	private String normalizetoZfin(String string) {
		return "ZFIN:" + string;
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
