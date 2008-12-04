package org.phenoscape.bridge;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collection;

import org.apache.log4j.net.SMTPAppender;
import org.obd.model.Node;
import org.obd.model.Statement;
import org.obd.query.Shard;
import org.obd.query.ComparisonQueryTerm.Operator;
import org.obd.query.impl.AbstractSQLShard;
import org.obd.query.impl.OBDSQLShard;
import org.obo.dataadapter.OBDSQLDatabaseAdapter;

public class ZfinXrefModifier {

	private Shard obdsql;
	private final String HAS_DBXREF = "oboInOwl:hasDbXref"; 
	private OBDSQLDatabaseAdapter adapter;
	
	public ZfinXrefModifier(){
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
			adapter = new OBDSQLDatabaseAdapter();
			
			obdsql = new OBDSQLShard();
			((AbstractSQLShard) obdsql).connect(connParams[0], connParams[1],
					connParams[2]);
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void addIsALinks(Shard obdsql){
		for(Statement stmt :  obdsql.getStatements(null, HAS_DBXREF, null, null, false, false)){
			if(stmt.getNodeId().contains("TAO:") && stmt.getTargetId().contains("ZFA:")){ 
				System.out.println(stmt);
			}
		}
		/*
		if(lo.toString().startsWith("ZFA:") && x.toString().startsWith("TAO:")){ //if ZFA xref TAO
			callSqlFunc("store_link_si", iid, "OBO_REL:is_a", x.getID(), "", false);
		}
			if(x.toString().startsWith("ZFA:") && lo.getID().startsWith("TAO:")){	//if TAO xref ZFA
			callSqlFunc("store_node_dbxref_isa_i", iid, x.toString());
		}
		*/	
	}
	
	public static void main(String[] args) {
		ZfinXrefModifier zxm = new ZfinXrefModifier();
		zxm.addIsALinks(zxm.obdsql);
	}

}
