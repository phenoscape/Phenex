package org.phenoscape.bridge;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;

import junit.framework.Assert;

import org.apache.xmlbeans.XmlException;
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
import org.obd.query.impl.OBOSessionShard;
import org.phenoscape.io.NeXMLReader;
import org.phenoscape.model.DataSet;
import org.phenoscape.model.OntologyController;


public class OBDModelBridgeTest {

	@Test
	public void testLoad() throws XmlException, IOException, SQLException, ClassNotFoundException {
		OntologyController oc = new OntologyController();
		NeXMLReader reader = new NeXMLReader(new File("test/testfiles/Fang2003-nexml.xml"), oc.getOBOSession());
		DataSet ds = reader.getDataSet();
		OBDModelBridge bridge = new OBDModelBridge();
		bridge.translate(ds);
		Graph g = bridge.getGraph();

		//OBDSQLShard obdsql = new OBDSQLShard();
		//obdsql.connect("jdbc:postgresql://localhost:5432/obdtest");
		//obdsql.putGraph(g);
		OBOSessionShard s = new OBOSessionShard();
		s.putGraph(g);
	}

	@Test
	public void testOBDSave() throws XmlException, IOException, SQLException, ClassNotFoundException {
		OntologyController oc = new OntologyController();
		NeXMLReader reader = new NeXMLReader(new File("test/testfiles/Fang2003-nexml.xml"), oc.getOBOSession());
		DataSet ds = reader.getDataSet();
		OBDModelBridge bridge = new OBDModelBridge();
		bridge.translate(ds);
		Graph g = bridge.getGraph();

		Shard shard = new OBDSQLShard();
		((OBDSQLShard)shard).connect("jdbc:postgresql://localhost:5432/obdtest");
		shard.putGraph(g);
		/*
	    <phen:bearer>
	    <phen:typeref about="TAO:0000622">
	      <phen:qualifier relation="OBO_REL:part_of">
	        <phen:holds_in_relation_to>
	          <phen:typeref about="TAO:0001114"/>
	        </phen:holds_in_relation_to>
	      </phen:qualifier>
	    </phen:typeref>
	  </phen:bearer>
		 */

		QueryTerm qt = new BooleanQueryTerm(BooleanOperator.AND, new LinkQueryTerm("TAO:0000622"),
				new LinkQueryTerm("TAO:0001114"));
		Collection<Node> nodes = shard.getNodesByQuery(qt);
		System.out.println("Matching nodes: "+nodes.size());
		for (Node node : nodes) {
			System.out.println(node);
		}
		Assert.assertTrue(nodes.size() > 0);
		Collection<LinkStatement> annots = shard.getLinkStatementsByQuery(new AnnotationLinkQueryTerm(qt));
		for (LinkStatement a : annots) {
			System.out.println(a);
		}
		Assert.assertTrue(annots.size() > 0);
	}


}
