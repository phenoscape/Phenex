package org.phenoscape.bridge;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.apache.xmlbeans.XmlException;
import org.junit.Ignore;
import org.junit.Test;
import org.obd.model.Graph;
import org.obd.model.LinkStatement;
import org.obd.model.Node;
import org.obd.query.AnnotationLinkQueryTerm;
import org.obd.query.BooleanQueryTerm;
import org.obd.query.LinkQueryTerm;
import org.obd.query.QueryTerm;
import org.obd.query.Shard;
import org.obd.query.BooleanQueryTerm.BooleanOperator;
import org.obd.query.impl.OBDSQLShard;
import org.phenoscape.io.NeXMLReader;
import org.phenoscape.model.DataSet;
import org.phenoscape.model.OntologyController;

public class OBDModelBridgeTest {

	@Test
	public void testLoad() throws XmlException, IOException, SQLException,
			ClassNotFoundException {
		OntologyController oc = new OntologyController();
		OBDModelBridge bridge = new OBDModelBridge();
		NeXMLReader reader;
		DataSet ds;
		Graph g;
		String basePath = "?";
		File baseDataDir = new File(basePath);
		int i = 0;
		//OBOSessionShard s = new OBOSessionShard();
		OBDSQLShard obdsql = new OBDSQLShard();
		obdsql.connect("?", "?",
				"?");
		List<File> baseDirs = Arrays.asList(baseDataDir.listFiles());
		Collections.sort(baseDirs);
		for (File baseDir : baseDirs) {
			// check to avoid .. and . directories
			if (baseDir.isDirectory() && !baseDir.getName().contains(".")) {
				List<File> files = Arrays.asList(baseDir.listFiles());
				Collections.sort(files);
				for (File dataFile : files) {
					// another check to avoid directories
					if (dataFile.isFile()) {
						System.out.println(++i + ". Started work with " + dataFile.getAbsolutePath());
						reader = new NeXMLReader(dataFile, oc.getOBOSession());
						ds = reader.getDataSet();
						bridge.translate(ds, dataFile);
						g = bridge.getGraph();
						obdsql.putGraph(g);
						//s.putGraph(g);
						System.out.println(i + ". Finished loading " + dataFile.getAbsolutePath());
					}
				}
			}
		}  
		System.out.println(i + " files loaded. Done!");
	}

	@Ignore
	@Test
	// ignoring this test because it requires special setup
	public void testOBDSave() throws XmlException, IOException, SQLException,
			ClassNotFoundException {
		OntologyController oc = new OntologyController();
		NeXMLReader reader = new NeXMLReader(new File(
				"?"), oc
				.getOBOSession());
		DataSet ds = reader.getDataSet();
		OBDModelBridge bridge = new OBDModelBridge();
		bridge.translate(ds, new File("?"));
		Graph g = bridge.getGraph();

		Shard shard = new OBDSQLShard();
		((OBDSQLShard) shard)
				.connect("?");
		shard.putGraph(g);
		/*
		 * <phen:bearer> <phen:typeref about="TAO:0000622"> <phen:qualifier
		 * relation="OBO_REL:part_of"> <phen:holds_in_relation_to> <phen:typeref
		 * about="TAO:0001114"/> </phen:holds_in_relation_to> </phen:qualifier>
		 * </phen:typeref> </phen:bearer>
		 */

		QueryTerm qt = new BooleanQueryTerm(BooleanOperator.AND,
				new LinkQueryTerm("TAO:0000622"), new LinkQueryTerm(
						"TAO:0001114"));
		Collection<Node> nodes = shard.getNodesByQuery(qt);
		System.out.println("Matching nodes: " + nodes.size());
		for (Node node : nodes) {
			System.out.println(node);
		}
		Assert.assertTrue(nodes.size() > 0);
		Collection<LinkStatement> annots = shard
				.getLinkStatementsByQuery(new AnnotationLinkQueryTerm(qt));
		for (LinkStatement a : annots) {
			System.out.println(a);
		}
		Assert.assertTrue(annots.size() > 0);
	}

}
