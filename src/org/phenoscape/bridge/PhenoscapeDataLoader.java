package org.phenoscape.bridge;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.xmlbeans.XmlException;
import org.obd.model.Graph;
import org.obd.query.Shard;
import org.obd.query.impl.AbstractSQLShard;
import org.obd.query.impl.OBDSQLShard;
import org.phenoscape.io.NeXMLReader;
import org.phenoscape.model.DataSet;
import org.phenoscape.model.OntologyController;

public class PhenoscapeDataLoader {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// String dataDir = args[0];
		PhenoscapeDataLoader pdl = new PhenoscapeDataLoader();
		try {
			pdl.loadData(args[0], args[1]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadData(String dataDir, String dbConn) throws XmlException, IOException,
			SQLException, ClassNotFoundException {
		OntologyController oc = new OntologyController();
		OBDModelBridge bridge = new OBDModelBridge();
		NeXMLReader reader;
		DataSet ds;
		Graph g;
		// String basePath = "/home/cartik/data/";
		File baseDataDir = new File(dataDir);
		int i = 0;
		// OBOSessionShard s = new OBOSessionShard();
		File connParamFile = new File(dbConn);
		BufferedReader br = new BufferedReader(new FileReader(connParamFile));
		String[] connParams = new String[3];
		String param;
		int j = 0, t = 0;
		while ((param = br.readLine()) != null) {
			connParams[j++] = param;
		}

		Shard obdsql = new OBDSQLShard();
		((AbstractSQLShard) obdsql).connect(connParams[0], connParams[1],
				connParams[2]);
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
						reader = new NeXMLReader(dataFile, oc.getOBOSession());
						ds = reader.getDataSet();
						bridge.translate(ds, dataFile);
						g = bridge.getGraph();
						obdsql.putGraph(g);
						// s.putGraph(g);
						t += g.getStatements().size();
						System.out.println(g.getStatements().size() + " records added");
						System.out.println(i + ". Finished loading "
								+ dataFile.getAbsolutePath());
					}
				}
			}
		}
		bridge.problemLog.flush();
		bridge.problemLog.close();
		System.out.println(t + " total statements from " + i + " files loaded. Done!");
	}
}
