package org.phenoscape.bridge;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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
import org.obd.query.impl.AbstractSQLShard;
import org.obd.query.impl.OBDSQLShard;
import org.phenoscape.io.nexml_1_0.NeXMLReader_1_0;
import org.phenoscape.model.DataSet;
import org.phenoscape.model.OntologyController;
import org.phenoscape.model.UserOntologyConfiguration;

public class OBDModelBridgeTest {

	@Test
	@Ignore
	public void testLoad() throws XmlException, IOException, SQLException,
			ClassNotFoundException {
		OntologyController oc = new OntologyController(new UserOntologyConfiguration()); //TODO use a custom config
		OBDModelBridge bridge = new OBDModelBridge();
		NeXMLReader_1_0 reader;
		DataSet ds;
		Graph g;
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("problemLog.txt")));
		String basePath = "/home/cartik/Desktop/PerlScripts/trunk/data";
		File baseDataDir = new File(basePath);
		int i = 0;
		File connParamFile = new File("testfiles/connectionParameters");
		BufferedReader br = new BufferedReader(new FileReader(connParamFile));
		String[] connParams = new String[3];
		String param;
		int j = 0;
		while ((param = br.readLine()) != null) {
			connParams[j++] = param;
		}

		Shard obdsql = new OBDSQLShard();
		((AbstractSQLShard) obdsql).connect(connParams[0], connParams[1],
				connParams[2]);
		// OBOSessionShard s = new OBOSessionShard();
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
						System.out.println(++i + ". Started work with "
								+ dataFile.getAbsolutePath());
						reader = new NeXMLReader_1_0(dataFile, oc.getOBOSession());
						ds = reader.getDataSet();
						bridge.translate(ds, dataFile, bw);
						g = bridge.getGraph();
						obdsql.putGraph(g);
						// s.putGraph(g);
						System.out.println(i + ". Finished loading "
								+ dataFile.getAbsolutePath());
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
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("problemLog.txt")));
		OntologyController oc = new OntologyController(new UserOntologyConfiguration()); //TODO use a custom config
		NeXMLReader_1_0 reader = new NeXMLReader_1_0(new File("?"), oc.getOBOSession());
		DataSet ds = reader.getDataSet();
		OBDModelBridge bridge = new OBDModelBridge();
		bridge.translate(ds, new File("?"), bw);
		Graph g = bridge.getGraph();

		Shard shard = new OBDSQLShard();
		((OBDSQLShard) shard).connect("?");
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
