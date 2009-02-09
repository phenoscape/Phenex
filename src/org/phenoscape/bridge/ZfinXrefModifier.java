package org.phenoscape.bridge;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.obd.model.LinkStatement;
import org.obd.model.Statement;
import org.obd.query.Shard;
import org.obd.query.impl.AbstractSQLShard;
import org.obd.query.impl.OBDSQLShard;

public class ZfinXrefModifier {

	private Shard obdsql;
	private final String HAS_DBXREF = "oboInOwl:hasDbXref"; 
	private final String IS_A = "OBO_REL:is_a";
	
	public ZfinXrefModifier(String path){
		File connParamFile = new File(path);
		try {
			BufferedReader br = new BufferedReader(
					new FileReader(connParamFile));
			String[] connParams = new String[3];
			String param;
			int j = 0;
			while ((param = br.readLine()) != null) {
				connParams[j++] = param;
			}			
			obdsql = new OBDSQLShard();
			((AbstractSQLShard) obdsql).connect(connParams[0], connParams[1],
					connParams[2]);
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void addIsALinks(Shard obdsql){
		LinkStatement isaStmt;
		int i = 0, j = 0;
		for(Statement stmt :  obdsql.getStatements(null, HAS_DBXREF, null, null, false, false)){
			if(stmt.getNodeId().contains("TAO:") && stmt.getTargetId().contains("ZFA:")){
				isaStmt = new LinkStatement();
				isaStmt.setNodeId(stmt.getTargetId());
				isaStmt.setRelationId(IS_A);
				isaStmt.setTargetId(stmt.getNodeId());
				System.out.println("Inserting i" +  ++i + ". " + isaStmt);
				obdsql.putStatement(isaStmt);
			}
			else if(stmt.getNodeId().contains("ZFA:") && stmt.getTargetId().contains("TAO:")){
				isaStmt = new LinkStatement();
				isaStmt.setNodeId(stmt.getNodeId());
				isaStmt.setRelationId(IS_A);
				isaStmt.setTargetId(stmt.getTargetId());
				System.out.println("Inserting j" + ++j + ". " + isaStmt);
				obdsql.putStatement(isaStmt);
			}
		}
	}
	
	public static void main(String[] args) {
		ZfinXrefModifier zxm = new ZfinXrefModifier(args[0]);
		zxm.addIsALinks(zxm.obdsql);
	}

}
